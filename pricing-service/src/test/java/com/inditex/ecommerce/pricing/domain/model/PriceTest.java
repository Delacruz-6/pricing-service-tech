package com.inditex.ecommerce.pricing.domain.model;

import com.inditex.ecommerce.pricing.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Price - dominio")
class PriceTest {

    @Test
    @DisplayName("Se crea bien si la fecha inicio es anterior a la de fin")
    void creacionCorrecta() {
        var inicio = LocalDateTime.of(2020, 6, 14, 0, 0);
        var fin = LocalDateTime.of(2020, 12, 31, 23, 59, 59);

        var precio = new Price(1L, inicio, fin, 1, 35455L, 0,
                new BigDecimal("35.50"), Currency.getInstance("EUR"));

        assertThat(precio.brandId()).isEqualTo(1L);
        assertThat(precio.productId()).isEqualTo(35455L);
        assertThat(precio.priceList()).isEqualTo(1);
        assertThat(precio.price()).isEqualByComparingTo("35.50");
        assertThat(precio.currency().getCurrencyCode()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("Falla si la fecha inicio es posterior a la de fin")
    void fechasInvertidas_lanzaExcepcion() {
        var inicio = LocalDateTime.of(2020, 6, 15, 0, 0);
        var fin = LocalDateTime.of(2020, 6, 14, 0, 0);

        assertThatThrownBy(() -> new Price(1L, inicio, fin, 1, 35455L, 0,
                new BigDecimal("35.50"), Currency.getInstance("EUR")))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("La fecha de inicio no puede ser posterior a la de fin.");
    }
}
