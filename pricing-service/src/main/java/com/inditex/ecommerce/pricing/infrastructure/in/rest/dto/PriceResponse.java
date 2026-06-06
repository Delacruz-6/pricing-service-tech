package com.inditex.ecommerce.pricing.infrastructure.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Respuesta con el precio aplicable de un producto")
public record PriceResponse(

        @Schema(
                description = "Identificador unico del producto",
                example = "35455",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long productId,

        @Schema(
                description = "Identificador unico de la cadena/marca (1 = ZARA, 2 = PULL&BEAR, etc.)",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long brandId,

        @Schema(
                description = "Identificador de la tarifa o lista de precios aplicable",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Integer priceList,

        @Schema(
                description = "Fecha y hora de inicio de vigencia del precio",
                example = "2020-06-14T00:00:00",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        LocalDateTime startDate,

        @Schema(
                description = "Fecha y hora de fin de vigencia del precio",
                example = "2020-12-31T23:59:59",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        LocalDateTime endDate,

        @Schema(
                description = "Precio final aplicable al producto",
                example = "35.50",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        BigDecimal price,

        @Schema(
                description = "Codigo de la moneda segun ISO 4217",
                example = "EUR",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String currency
) {}