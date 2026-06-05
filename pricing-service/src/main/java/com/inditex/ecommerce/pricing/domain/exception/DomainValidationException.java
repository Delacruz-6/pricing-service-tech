package com.inditex.ecommerce.pricing.domain.exception;

public class DomainValidationException extends RuntimeException {

    /**
     * Excepción de dominio
     * @param message
     */
    public DomainValidationException(String message) {
        super(message);
    }
}
