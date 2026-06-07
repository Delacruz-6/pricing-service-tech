package com.inditex.ecommerce.pricing.infrastructure.in.rest.controller;

import com.inditex.ecommerce.pricing.application.exception.PriceNotFoundException;
import com.inditex.ecommerce.pricing.application.mapper.PriceMapper;
import com.inditex.ecommerce.pricing.application.usecase.GetApplicablePriceUseCase;
import com.inditex.ecommerce.pricing.domain.model.Price;
import com.inditex.ecommerce.pricing.infrastructure.in.rest.dto.PriceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceController.class)
@DisplayName("PriceController - unitarios")
class PriceControllerTest {

    private static final Long ID_CADENA = 1L;
    private static final Long ID_PRODUCTO = 35455L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetApplicablePriceUseCase useCase;

    @MockitoBean
    private PriceMapper mapper;

    private Price precioDeEjemplo(int tarifa, int prioridad, BigDecimal importe,
                                  LocalDateTime inicio, LocalDateTime fin) {
        return new Price(ID_CADENA, inicio, fin, tarifa, ID_PRODUCTO, prioridad, importe,
                Currency.getInstance("EUR"));
    }

    @Nested
    @DisplayName("Peticiones correctas")
    class PeticionesCorrectas {

        @Test
        @DisplayName("Test 1: tarifa 1 a 35,50 €")
        void test1_tarifa1() throws Exception {
            var fecha = LocalDateTime.of(2020, 6, 14, 10, 0);
            var precio = precioDeEjemplo(1, 0, new BigDecimal("35.50"),
                    LocalDateTime.of(2020, 6, 14, 0, 0),
                    LocalDateTime.of(2020, 12, 31, 23, 59, 59));
            var respuesta = new PriceResponse(ID_PRODUCTO, ID_CADENA, 1,
                    LocalDateTime.of(2020, 6, 14, 0, 0),
                    LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                    new BigDecimal("35.50"), "EUR");

            when(useCase.execute(eq(ID_CADENA), eq(ID_PRODUCTO), eq(fecha))).thenReturn(precio);
            when(mapper.toResponse(precio)).thenReturn(respuesta);

            mockMvc.perform(get("/api/prices")
                            .param("applicationDate", "2020-06-14T10:00:00")
                            .param("productId", "35455")
                            .param("brandId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceList").value(1))
                    .andExpect(jsonPath("$.price").value(35.50))
                    .andExpect(jsonPath("$.currency").value("EUR"));
        }

        @Test
        @DisplayName("Test 2: tarifa 2 a 25,45 €")
        void test2_tarifa2() throws Exception {
            var fecha = LocalDateTime.of(2020, 6, 14, 16, 0);
            var precio = precioDeEjemplo(2, 1, new BigDecimal("25.45"),
                    LocalDateTime.of(2020, 6, 14, 15, 0),
                    LocalDateTime.of(2020, 6, 14, 18, 30));
            var respuesta = new PriceResponse(ID_PRODUCTO, ID_CADENA, 2,
                    LocalDateTime.of(2020, 6, 14, 15, 0),
                    LocalDateTime.of(2020, 6, 14, 18, 30),
                    new BigDecimal("25.45"), "EUR");

            when(useCase.execute(eq(ID_CADENA), eq(ID_PRODUCTO), eq(fecha))).thenReturn(precio);
            when(mapper.toResponse(precio)).thenReturn(respuesta);

            mockMvc.perform(get("/api/prices")
                            .param("applicationDate", "2020-06-14T16:00:00")
                            .param("productId", "35455")
                            .param("brandId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceList").value(2))
                    .andExpect(jsonPath("$.price").value(25.45));
        }

        @Test
        @DisplayName("Test 3: fuera del tramo promocional, vuelve tarifa 1")
        void test3_tarifa1TrasPromo() throws Exception {
            var fecha = LocalDateTime.of(2020, 6, 14, 21, 0);
            var precio = precioDeEjemplo(1, 0, new BigDecimal("35.50"),
                    LocalDateTime.of(2020, 6, 14, 0, 0),
                    LocalDateTime.of(2020, 12, 31, 23, 59, 59));
            var respuesta = new PriceResponse(ID_PRODUCTO, ID_CADENA, 1,
                    LocalDateTime.of(2020, 6, 14, 0, 0),
                    LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                    new BigDecimal("35.50"), "EUR");

            when(useCase.execute(eq(ID_CADENA), eq(ID_PRODUCTO), eq(fecha))).thenReturn(precio);
            when(mapper.toResponse(precio)).thenReturn(respuesta);

            mockMvc.perform(get("/api/prices")
                            .param("applicationDate", "2020-06-14T21:00:00")
                            .param("productId", "35455")
                            .param("brandId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceList").value(1))
                    .andExpect(jsonPath("$.price").value(35.50));
        }

        @Test
        @DisplayName("Test 4: tarifa 3 a 30,50 €")
        void test4_tarifa3() throws Exception {
            var fecha = LocalDateTime.of(2020, 6, 15, 10, 0);
            var precio = precioDeEjemplo(3, 1, new BigDecimal("30.50"),
                    LocalDateTime.of(2020, 6, 15, 0, 0),
                    LocalDateTime.of(2020, 6, 15, 11, 0));
            var respuesta = new PriceResponse(ID_PRODUCTO, ID_CADENA, 3,
                    LocalDateTime.of(2020, 6, 15, 0, 0),
                    LocalDateTime.of(2020, 6, 15, 11, 0),
                    new BigDecimal("30.50"), "EUR");

            when(useCase.execute(eq(ID_CADENA), eq(ID_PRODUCTO), eq(fecha))).thenReturn(precio);
            when(mapper.toResponse(precio)).thenReturn(respuesta);

            mockMvc.perform(get("/api/prices")
                            .param("applicationDate", "2020-06-15T10:00:00")
                            .param("productId", "35455")
                            .param("brandId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceList").value(3))
                    .andExpect(jsonPath("$.price").value(30.50));
        }

        @Test
        @DisplayName("Test 5: tarifa 4 a 38,95 €")
        void test5_tarifa4() throws Exception {
            var fecha = LocalDateTime.of(2020, 6, 16, 21, 0);
            var precio = precioDeEjemplo(4, 1, new BigDecimal("38.95"),
                    LocalDateTime.of(2020, 6, 15, 16, 0),
                    LocalDateTime.of(2020, 12, 31, 23, 59, 59));
            var respuesta = new PriceResponse(ID_PRODUCTO, ID_CADENA, 4,
                    LocalDateTime.of(2020, 6, 15, 16, 0),
                    LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                    new BigDecimal("38.95"), "EUR");

            when(useCase.execute(eq(ID_CADENA), eq(ID_PRODUCTO), eq(fecha))).thenReturn(precio);
            when(mapper.toResponse(precio)).thenReturn(respuesta);

            mockMvc.perform(get("/api/prices")
                            .param("applicationDate", "2020-06-16T21:00:00")
                            .param("productId", "35455")
                            .param("brandId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceList").value(4))
                    .andExpect(jsonPath("$.price").value(38.95));
        }
    }

    @Nested
    @DisplayName("Errores de validación")
    class ErroresValidacion {

        @Test
        @DisplayName("404 cuando el caso de uso no encuentra precio")
        void sinPrecio_devuelve404() throws Exception {
            var fecha = LocalDateTime.of(2020, 6, 14, 10, 0);
            when(useCase.execute(eq(ID_CADENA), eq(ID_PRODUCTO), eq(fecha)))
                    .thenThrow(new PriceNotFoundException(ID_CADENA, ID_PRODUCTO, fecha));

            mockMvc.perform(get("/api/prices")
                            .param("applicationDate", "2020-06-14T10:00:00")
                            .param("productId", "35455")
                            .param("brandId", "1"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("400 sin fecha de aplicación")
        void faltaFecha() throws Exception {
            mockMvc.perform(get("/api/prices")
                            .param("productId", "35455")
                            .param("brandId", "1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("400 con fecha mal formateada")
        void fechaMalFormateada() throws Exception {
            mockMvc.perform(get("/api/prices")
                            .param("applicationDate", "ayer por la tarde")
                            .param("productId", "35455")
                            .param("brandId", "1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("400 sin productId")
        void faltaProducto() throws Exception {
            mockMvc.perform(get("/api/prices")
                            .param("applicationDate", "2020-06-14T10:00:00")
                            .param("brandId", "1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("400 sin brandId")
        void faltaCadena() throws Exception {
            mockMvc.perform(get("/api/prices")
                            .param("applicationDate", "2020-06-14T10:00:00")
                            .param("productId", "35455"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }
}
