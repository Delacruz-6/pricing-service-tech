package com.inditex.ecommerce.pricing.infrastructure.in.rest.controller;

import com.inditex.ecommerce.pricing.application.mapper.PriceMapper;
import com.inditex.ecommerce.pricing.application.usecase.GetApplicablePriceUseCase;
import com.inditex.ecommerce.pricing.infrastructure.in.rest.dto.ErrorResponse;
import com.inditex.ecommerce.pricing.infrastructure.in.rest.dto.PriceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
@Tag(
        name = "Prices",
        description = "API para consultar precios aplicables de productos de Inditex"
)
public class PriceController {

    private final GetApplicablePriceUseCase useCase;
    private final PriceMapper mapper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Obtener precio aplicable",
            description = "Consulta el precio aplicable de un producto para una cadena y fecha de aplicacion dadas. "
                    + "Si existen multiples precios aplicables, se devuelve el de mayor prioridad.",
            operationId = "getApplicablePrice"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Precio aplicable encontrado exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PriceResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parametros de entrada invalidos o mal formateados",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontro ningun precio aplicable para los parametros proporcionados",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<PriceResponse> getPrice(
            @Parameter(description = "Fecha y hora de aplicacion en formato ISO 8601", example = "2020-06-14T10:00:00", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate,
            @Parameter(description = "Identificador del producto", example = "35455", required = true)
            @RequestParam Long productId,
            @Parameter(description = "Identificador de la cadena/marca", example = "1", required = true)
            @RequestParam Long brandId) {
        var price = useCase.execute(brandId, productId, applicationDate);
        return ResponseEntity.ok(mapper.toResponse(price));
    }
}
