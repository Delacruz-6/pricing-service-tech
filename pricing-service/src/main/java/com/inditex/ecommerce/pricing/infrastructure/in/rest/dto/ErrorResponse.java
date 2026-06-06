package com.inditex.ecommerce.pricing.infrastructure.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Respuesta de error estandar de la API")
public record ErrorResponse(

        @Schema(description = "Codigo HTTP del error", example = "404")
        int status,

        @Schema(description = "Tipo de error HTTP", example = "Not Found")
        String error,

        @Schema(description = "Mensaje descriptivo del error", example = "No se encontro precio aplicable para brandId=1, productId=35455, applicationDate=2020-01-01T00:00")
        String message,

        @Schema(description = "Fecha y hora en que se produjo el error")
        LocalDateTime timestamp
) {}
