package com.inditex.ecommerce.pricing.application.exception;

import java.time.LocalDateTime;

public class PriceNotFoundException extends RuntimeException {

    public enum Reason {
        BRAND_NOT_FOUND,
        PRODUCT_NOT_FOUND,
        NO_PRICE_FOR_DATE
    }

    private final Reason reason;

    private PriceNotFoundException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }

    public static PriceNotFoundException brandNotFound(Long brandId) {
        return new PriceNotFoundException(
                Reason.BRAND_NOT_FOUND,
                "Cadena inexistente: brandId=%d".formatted(brandId));
    }

    public static PriceNotFoundException productNotFound(Long brandId, Long productId) {
        return new PriceNotFoundException(
                Reason.PRODUCT_NOT_FOUND,
                "Producto inexistente: productId=%d para brandId=%d".formatted(productId, brandId));
    }

    public static PriceNotFoundException noPriceForDate(Long brandId, Long productId, LocalDateTime applicationDate) {
        return new PriceNotFoundException(
                Reason.NO_PRICE_FOR_DATE,
                "No hay precio aplicable para la fecha indicada: brandId=%d, productId=%d, applicationDate=%s"
                        .formatted(brandId, productId, applicationDate));
    }
}
