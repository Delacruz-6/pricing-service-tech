package com.inditex.ecommerce.pricing.domain.model;

import com.inditex.ecommerce.pricing.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

public record Price(
        Long brandId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer priceList,
        Long productId,
        Integer priority,
        BigDecimal price,
        Currency currency
) {
    public Price {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new DomainValidationException("La fecha de inicio no puede ser posterior a la de fin.");
        }
    }
}