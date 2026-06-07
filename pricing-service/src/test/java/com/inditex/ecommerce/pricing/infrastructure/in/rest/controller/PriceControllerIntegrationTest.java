package com.inditex.ecommerce.pricing.infrastructure.in.rest.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("PriceController - integración")
class PriceControllerIntegrationTest {

    private static final String ENDPOINT = "/api/prices";
    private static final String PRODUCTO = "35455";
    private static final String CADENA_ZARA = "1";

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Consultas con datos de ejemplo")
    class ConsultasConDatosEjemplo {

        @Test
        @DisplayName("Test 1: 14/06/2020 a las 10:00, producto 35455, cadena ZARA")
        void test1_dia14_10h_tarifa1() throws Exception {
            mockMvc.perform(get(ENDPOINT)
                            .param("applicationDate", "2020-06-14T10:00:00")
                            .param("productId", PRODUCTO)
                            .param("brandId", CADENA_ZARA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(35455))
                    .andExpect(jsonPath("$.brandId").value(1))
                    .andExpect(jsonPath("$.priceList").value(1))
                    .andExpect(jsonPath("$.price").value(35.50))
                    .andExpect(jsonPath("$.startDate").value("2020-06-14T00:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2020-12-31T23:59:59"))
                    .andExpect(jsonPath("$.currency").value("EUR"));
        }

        @Test
        @DisplayName("Test 2: 14/06/2020 a las 16:00, producto 35455, cadena ZARA")
        void test2_dia14_16h_tarifa2() throws Exception {
            mockMvc.perform(get(ENDPOINT)
                            .param("applicationDate", "2020-06-14T16:00:00")
                            .param("productId", PRODUCTO)
                            .param("brandId", CADENA_ZARA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceList").value(2))
                    .andExpect(jsonPath("$.price").value(25.45))
                    .andExpect(jsonPath("$.startDate").value("2020-06-14T15:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2020-06-14T18:30:00"));
        }

        @Test
        @DisplayName("Test 3: 14/06/2020 a las 21:00, producto 35455, cadena ZARA")
        void test3_dia14_21h_vuelveTarifa1() throws Exception {
            mockMvc.perform(get(ENDPOINT)
                            .param("applicationDate", "2020-06-14T21:00:00")
                            .param("productId", PRODUCTO)
                            .param("brandId", CADENA_ZARA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceList").value(1))
                    .andExpect(jsonPath("$.price").value(35.50));
        }

        @Test
        @DisplayName("Test 4: 15/06/2020 a las 10:00, producto 35455, cadena ZARA")
        void test4_dia15_10h_tarifa3() throws Exception {
            mockMvc.perform(get(ENDPOINT)
                            .param("applicationDate", "2020-06-15T10:00:00")
                            .param("productId", PRODUCTO)
                            .param("brandId", CADENA_ZARA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceList").value(3))
                    .andExpect(jsonPath("$.price").value(30.50))
                    .andExpect(jsonPath("$.startDate").value("2020-06-15T00:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2020-06-15T11:00:00"));
        }

        @Test
        @DisplayName("Test 5: 16/06/2020 a las 21:00, producto 35455, cadena ZARA")
        void test5_dia16_21h_tarifa4() throws Exception {
            mockMvc.perform(get(ENDPOINT)
                            .param("applicationDate", "2020-06-16T21:00:00")
                            .param("productId", PRODUCTO)
                            .param("brandId", CADENA_ZARA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceList").value(4))
                    .andExpect(jsonPath("$.price").value(38.95))
                    .andExpect(jsonPath("$.startDate").value("2020-06-15T16:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2020-12-31T23:59:59"));
        }
    }

    @Nested
    @DisplayName("Respuestas de error")
    class RespuestasError {

        @Test
        @DisplayName("404 si el producto no tiene precio")
        void productoInexistente_devuelve404() throws Exception {
            mockMvc.perform(get(ENDPOINT)
                            .param("applicationDate", "2020-06-14T10:00:00")
                            .param("productId", "99999")
                            .param("brandId", CADENA_ZARA))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"));
        }

        @Test
        @DisplayName("400 si falta la fecha de aplicación")
        void sinFecha_devuelve400() throws Exception {
            mockMvc.perform(get(ENDPOINT)
                            .param("productId", PRODUCTO)
                            .param("brandId", CADENA_ZARA))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 si la fecha viene mal formateada")
        void fechaInvalida_devuelve400() throws Exception {
            mockMvc.perform(get(ENDPOINT)
                            .param("applicationDate", "no-es-una-fecha")
                            .param("productId", PRODUCTO)
                            .param("brandId", CADENA_ZARA))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("404 si la cadena no existe")
        void cadenaInexistente_devuelve404() throws Exception {
            mockMvc.perform(get(ENDPOINT)
                            .param("applicationDate", "2020-06-14T10:00:00")
                            .param("productId", PRODUCTO)
                            .param("brandId", "999"))
                    .andExpect(status().isNotFound());
        }
    }
}
