package com.inditex.ecommerce.pricing.infrastructure.out.persistence.repository;

import com.inditex.ecommerce.pricing.infrastructure.out.persistence.entity.PriceEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = "/data.sql")
@DisplayName("PriceRepository - integración")
class PriceRepositoryIntegrationTest {

    private static final Long ID_CADENA = 1L;
    private static final Long ID_PRODUCTO = 35455L;

    @Autowired
    private PriceRepository repository;

    private PriceEntity buscarPrecio(LocalDateTime fecha) {
        List<PriceEntity> filas = repository.findTopApplicablePrice(
                ID_CADENA, ID_PRODUCTO, fecha, PageRequest.of(0, 1));
        assertThat(filas).hasSize(1);
        return filas.getFirst();
    }

    @Nested
    @DisplayName("Los 5 casos de la prueba técnica")
    class CasosPruebaTecnica {

        @Test
        @DisplayName("Test 1: 14/06 a las 10:00, tarifa 1")
        void test1() {
            var fila = buscarPrecio(LocalDateTime.of(2020, 6, 14, 10, 0));

            assertThat(fila.getPriceList()).isEqualTo(1);
            assertThat(fila.getPrice()).isEqualByComparingTo("35.50");
            assertThat(fila.getPriority()).isZero();
        }

        @Test
        @DisplayName("Test 2: 14/06 a las 16:00, tarifa 2")
        void test2() {
            var fila = buscarPrecio(LocalDateTime.of(2020, 6, 14, 16, 0));

            assertThat(fila.getPriceList()).isEqualTo(2);
            assertThat(fila.getPrice()).isEqualByComparingTo("25.45");
        }

        @Test
        @DisplayName("Test 3: 14/06 a las 21:00, tarifa 1")
        void test3() {
            var fila = buscarPrecio(LocalDateTime.of(2020, 6, 14, 21, 0));

            assertThat(fila.getPriceList()).isEqualTo(1);
            assertThat(fila.getPrice()).isEqualByComparingTo("35.50");
        }

        @Test
        @DisplayName("Test 4: 15/06 a las 10:00, tarifa 3")
        void test4() {
            var fila = buscarPrecio(LocalDateTime.of(2020, 6, 15, 10, 0));

            assertThat(fila.getPriceList()).isEqualTo(3);
            assertThat(fila.getPrice()).isEqualByComparingTo("30.50");
        }

        @Test
        @DisplayName("Test 5: 16/06 a las 21:00, tarifa 4")
        void test5() {
            var fila = buscarPrecio(LocalDateTime.of(2020, 6, 16, 21, 0));

            assertThat(fila.getPriceList()).isEqualTo(4);
            assertThat(fila.getPrice()).isEqualByComparingTo("38.95");
        }
    }

    @Nested
    @DisplayName("Prioridad y límites de fechas")
    class PrioridadYLimites {

        @Test
        @DisplayName("A las 15:00 del día 14 gana la tarifa con mayor prioridad")
        void solapamiento_ganaMayorPrioridad() {
            var fila = buscarPrecio(LocalDateTime.of(2020, 6, 14, 15, 0));

            assertThat(fila.getPriceList()).isEqualTo(2);
            assertThat(fila.getPriority()).isEqualTo(1);
        }

        @Test
        @DisplayName("A las 18:30 del día 14 sigue vigente la tarifa 2")
        void finInclusive_tarifa2() {
            var fila = buscarPrecio(LocalDateTime.of(2020, 6, 14, 18, 30));

            assertThat(fila.getPriceList()).isEqualTo(2);
        }

        @Test
        @DisplayName("A las 12:00 del día 15 aplica la tarifa base")
        void huecoEntrePromos_tarifaBase() {
            var fila = buscarPrecio(LocalDateTime.of(2020, 6, 15, 12, 0));

            assertThat(fila.getPriceList()).isEqualTo(1);
            assertThat(fila.getPrice()).isEqualByComparingTo("35.50");
        }

        @Test
        @DisplayName("Sin resultados para un producto que no existe")
        void productoInexistente_listaVacia() {
            List<PriceEntity> filas = repository.findTopApplicablePrice(
                    ID_CADENA, 99999L, LocalDateTime.of(2020, 6, 14, 10, 0), PageRequest.of(0, 1));

            assertThat(filas).isEmpty();
        }
    }
}
