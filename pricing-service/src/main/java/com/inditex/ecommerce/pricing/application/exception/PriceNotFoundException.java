package com.inditex.ecommerce.pricing.application.exception;

import java.time.LocalDateTime;

public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(Long brandId, Long productId, LocalDateTime applicationDate) {
        super(String.format(
                "No se encontro precio aplicable para brandId=%d, productId=%d, applicationDate=%s",
                brandId, productId, applicationDate));
    }
}
