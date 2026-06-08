package com.inditex.ecommerce.pricing.application.usecase;

import com.inditex.ecommerce.pricing.application.exception.PriceNotFoundException;
import com.inditex.ecommerce.pricing.application.port.persistence.PriceRepositoryPort;
import com.inditex.ecommerce.pricing.domain.model.Price;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetApplicablePriceUseCase")
class GetApplicablePriceUseCaseTest {

    private static final Long ID_CADENA = 1L;
    private static final Long ID_PRODUCTO = 35455L;

    @Mock
    private PriceRepositoryPort repository;

    @InjectMocks
    private GetApplicablePriceUseCase useCase;

    private Price tarifa(int lista, int prioridad, String importe,
                         LocalDateTime desde, LocalDateTime hasta) {
        return new Price(ID_CADENA, desde, hasta, lista, ID_PRODUCTO, prioridad,
                new BigDecimal(importe), Currency.getInstance("EUR"));
    }

    @Nested
    @DisplayName("Precio encontrado")
    class PrecioEncontrado {

        @Test
        @DisplayName("Test 1: 14/06 a las 10:00 devuelve tarifa 1")
        void test1() {
            var fecha = LocalDateTime.of(2020, 6, 14, 10, 0);
            var esperado = tarifa(1, 0, "35.50",
                    LocalDateTime.of(2020, 6, 14, 0, 0),
                    LocalDateTime.of(2020, 12, 31, 23, 59, 59));

            when(repository.findApplicablePrice(ID_CADENA, ID_PRODUCTO, fecha))
                    .thenReturn(Optional.of(esperado));

            var resultado = useCase.execute(ID_CADENA, ID_PRODUCTO, fecha);

            assertThat(resultado.priceList()).isEqualTo(1);
            assertThat(resultado.price()).isEqualByComparingTo("35.50");
            verify(repository).findApplicablePrice(ID_CADENA, ID_PRODUCTO, fecha);
        }

        @Test
        @DisplayName("Test 2: 14/06 a las 16:00 devuelve tarifa 2")
        void test2() {
            var fecha = LocalDateTime.of(2020, 6, 14, 16, 0);
            var esperado = tarifa(2, 1, "25.45",
                    LocalDateTime.of(2020, 6, 14, 15, 0),
                    LocalDateTime.of(2020, 6, 14, 18, 30));

            when(repository.findApplicablePrice(ID_CADENA, ID_PRODUCTO, fecha))
                    .thenReturn(Optional.of(esperado));

            var resultado = useCase.execute(ID_CADENA, ID_PRODUCTO, fecha);

            assertThat(resultado.priceList()).isEqualTo(2);
            assertThat(resultado.price()).isEqualByComparingTo("25.45");
        }

        @Test
        @DisplayName("Test 3: 14/06 a las 21:00 devuelve tarifa 1")
        void test3() {
            var fecha = LocalDateTime.of(2020, 6, 14, 21, 0);
            var esperado = tarifa(1, 0, "35.50",
                    LocalDateTime.of(2020, 6, 14, 0, 0),
                    LocalDateTime.of(2020, 12, 31, 23, 59, 59));

            when(repository.findApplicablePrice(ID_CADENA, ID_PRODUCTO, fecha))
                    .thenReturn(Optional.of(esperado));

            var resultado = useCase.execute(ID_CADENA, ID_PRODUCTO, fecha);

            assertThat(resultado.priceList()).isEqualTo(1);
            assertThat(resultado.price()).isEqualByComparingTo("35.50");
        }

        @Test
        @DisplayName("Test 4: 15/06 a las 10:00 devuelve tarifa 3")
        void test4() {
            var fecha = LocalDateTime.of(2020, 6, 15, 10, 0);
            var esperado = tarifa(3, 1, "30.50",
                    LocalDateTime.of(2020, 6, 15, 0, 0),
                    LocalDateTime.of(2020, 6, 15, 11, 0));

            when(repository.findApplicablePrice(ID_CADENA, ID_PRODUCTO, fecha))
                    .thenReturn(Optional.of(esperado));

            var resultado = useCase.execute(ID_CADENA, ID_PRODUCTO, fecha);

            assertThat(resultado.priceList()).isEqualTo(3);
            assertThat(resultado.price()).isEqualByComparingTo("30.50");
        }

        @Test
        @DisplayName("Test 5: 16/06 a las 21:00 devuelve tarifa 4")
        void test5() {
            var fecha = LocalDateTime.of(2020, 6, 16, 21, 0);
            var esperado = tarifa(4, 1, "38.95",
                    LocalDateTime.of(2020, 6, 15, 16, 0),
                    LocalDateTime.of(2020, 12, 31, 23, 59, 59));

            when(repository.findApplicablePrice(ID_CADENA, ID_PRODUCTO, fecha))
                    .thenReturn(Optional.of(esperado));

            var resultado = useCase.execute(ID_CADENA, ID_PRODUCTO, fecha);

            assertThat(resultado.priceList()).isEqualTo(4);
            assertThat(resultado.price()).isEqualByComparingTo("38.95");
        }
    }

    @Nested
    @DisplayName("Sin precio aplicable")
    class SinPrecio {

        @Test
        @DisplayName("Lanza PriceNotFoundException si la fecha no tiene tarifa")
        void lanzaExcepcion() {
            var fecha = LocalDateTime.of(2020, 1, 1, 0, 0);
            when(repository.findApplicablePrice(ID_CADENA, ID_PRODUCTO, fecha))
                    .thenReturn(Optional.empty());
            when(repository.existsByBrandId(ID_CADENA)).thenReturn(true);
            when(repository.existsByBrandIdAndProductId(ID_CADENA, ID_PRODUCTO)).thenReturn(true);

            assertThatThrownBy(() -> useCase.execute(ID_CADENA, ID_PRODUCTO, fecha))
                    .isInstanceOf(PriceNotFoundException.class)
                    .hasMessageContaining("No hay precio aplicable para la fecha indicada");
            verify(repository).findApplicablePrice(ID_CADENA, ID_PRODUCTO, fecha);
            verify(repository).existsByBrandId(ID_CADENA);
            verify(repository).existsByBrandIdAndProductId(ID_CADENA, ID_PRODUCTO);
        }

        @Test
        @DisplayName("Lanza PriceNotFoundException si la cadena no existe")
        void cadenaInexistente() {
            var fecha = LocalDateTime.of(2020, 6, 14, 10, 0);
            when(repository.findApplicablePrice(999L, ID_PRODUCTO, fecha))
                    .thenReturn(Optional.empty());
            when(repository.existsByBrandId(999L)).thenReturn(false);

            assertThatThrownBy(() -> useCase.execute(999L, ID_PRODUCTO, fecha))
                    .isInstanceOf(PriceNotFoundException.class)
                    .hasMessage("Cadena inexistente: brandId=999");
        }

        @Test
        @DisplayName("Lanza PriceNotFoundException si el producto no existe")
        void productoInexistente() {
            var fecha = LocalDateTime.of(2020, 6, 14, 10, 0);
            when(repository.findApplicablePrice(ID_CADENA, 99999L, fecha))
                    .thenReturn(Optional.empty());
            when(repository.existsByBrandId(ID_CADENA)).thenReturn(true);
            when(repository.existsByBrandIdAndProductId(ID_CADENA, 99999L)).thenReturn(false);

            assertThatThrownBy(() -> useCase.execute(ID_CADENA, 99999L, fecha))
                    .isInstanceOf(PriceNotFoundException.class)
                    .hasMessage("Producto inexistente: productId=99999 para brandId=1");
        }
    }

    @Nested
    @DisplayName("Delegación al repository")
    class Delegacion {

        @Test
        @DisplayName("Pasa los parámetros tal cual al puerto")
        void delegaAlrepository() {
            var fecha = LocalDateTime.of(2020, 1, 1, 0, 0);
            when(repository.findApplicablePrice(ID_CADENA, ID_PRODUCTO, fecha))
                    .thenReturn(Optional.empty());
            when(repository.existsByBrandId(ID_CADENA)).thenReturn(true);
            when(repository.existsByBrandIdAndProductId(ID_CADENA, ID_PRODUCTO)).thenReturn(true);

            assertThatThrownBy(() -> useCase.execute(ID_CADENA, ID_PRODUCTO, fecha))
                    .isInstanceOf(PriceNotFoundException.class);

            verify(repository).findApplicablePrice(ID_CADENA, ID_PRODUCTO, fecha);
            verify(repository).existsByBrandId(ID_CADENA);
            verify(repository).existsByBrandIdAndProductId(ID_CADENA, ID_PRODUCTO);
        }
    }
}
