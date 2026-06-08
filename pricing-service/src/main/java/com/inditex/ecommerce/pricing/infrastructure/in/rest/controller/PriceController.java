package com.inditex.ecommerce.pricing.infrastructure.in.rest.controller;

import com.inditex.ecommerce.pricing.application.mapper.PriceMapper;
import com.inditex.ecommerce.pricing.application.usecase.GetApplicablePriceUseCase;
import com.inditex.ecommerce.pricing.infrastructure.in.rest.dto.ErrorResponse;
import com.inditex.ecommerce.pricing.infrastructure.in.rest.dto.PriceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
            description = """
                    Consulta el precio aplicable de un producto para una cadena y fecha de aplicacion dadas. \
                    Si existen multiples precios aplicables, se devuelve el de mayor prioridad.

                    Errores posibles: 400 (parametros invalidos), 404 (precio no encontrado), 500 (error interno).

                    Casos de prueba (productId=35455, brandId=1):
                    - Test 1: 2020-06-14T10:00:00 -> tarifa 1, 35.50 EUR
                    - Test 2: 2020-06-14T16:00:00 -> tarifa 2, 25.45 EUR
                    - Test 3: 2020-06-14T21:00:00 -> tarifa 1, 35.50 EUR
                    - Test 4: 2020-06-15T10:00:00 -> tarifa 3, 30.50 EUR
                    - Test 5: 2020-06-16T21:00:00 -> tarifa 4, 38.95 EUR""",
            operationId = "getApplicablePrice"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Precio aplicable encontrado exitosamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PriceResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Test 1 - 14/06 10:00",
                                            summary = "Tarifa 1, 35.50 EUR",
                                            value = """
                                                    {
                                                      "productId": 35455,
                                                      "brandId": 1,
                                                      "priceList": 1,
                                                      "startDate": "2020-06-14T00:00:00",
                                                      "endDate": "2020-12-31T23:59:59",
                                                      "price": 35.50,
                                                      "currency": "EUR"
                                                    }"""
                                    ),
                                    @ExampleObject(
                                            name = "Test 2 - 14/06 16:00",
                                            summary = "Tarifa 2, 25.45 EUR",
                                            value = """
                                                    {
                                                      "productId": 35455,
                                                      "brandId": 1,
                                                      "priceList": 2,
                                                      "startDate": "2020-06-14T15:00:00",
                                                      "endDate": "2020-06-14T18:30:00",
                                                      "price": 25.45,
                                                      "currency": "EUR"
                                                    }"""
                                    ),
                                    @ExampleObject(
                                            name = "Test 3 - 14/06 21:00",
                                            summary = "Tarifa 1, 35.50 EUR",
                                            value = """
                                                    {
                                                      "productId": 35455,
                                                      "brandId": 1,
                                                      "priceList": 1,
                                                      "startDate": "2020-06-14T00:00:00",
                                                      "endDate": "2020-12-31T23:59:59",
                                                      "price": 35.50,
                                                      "currency": "EUR"
                                                    }"""
                                    ),
                                    @ExampleObject(
                                            name = "Test 4 - 15/06 10:00",
                                            summary = "Tarifa 3, 30.50 EUR",
                                            value = """
                                                    {
                                                      "productId": 35455,
                                                      "brandId": 1,
                                                      "priceList": 3,
                                                      "startDate": "2020-06-15T00:00:00",
                                                      "endDate": "2020-06-15T11:00:00",
                                                      "price": 30.50,
                                                      "currency": "EUR"
                                                    }"""
                                    ),
                                    @ExampleObject(
                                            name = "Test 5 - 16/06 21:00",
                                            summary = "Tarifa 4, 38.95 EUR",
                                            value = """
                                                    {
                                                      "productId": 35455,
                                                      "brandId": 1,
                                                      "priceList": 4,
                                                      "startDate": "2020-06-15T16:00:00",
                                                      "endDate": "2020-12-31T23:59:59",
                                                      "price": 38.95,
                                                      "currency": "EUR"
                                                    }"""
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parametros de entrada invalidos o mal formateados",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "400 - Fecha invalida",
                                            summary = "Formato de fecha no reconocido",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Valor invalido para el parametro 'applicationDate': no-es-una-fecha",
                                                      "timestamp": "2020-06-14T10:00:00"
                                                    }"""
                                    ),
                                    @ExampleObject(
                                            name = "400 - Fecha ausente",
                                            summary = "Falta el parametro obligatorio applicationDate",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Parametro obligatorio ausente: applicationDate",
                                                      "timestamp": "2020-06-14T10:00:00"
                                                    }"""
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontro ningun precio aplicable para los parametros proporcionados",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "404 - Producto inexistente",
                                            summary = "El producto no tiene precio para la fecha indicada",
                                            value = """
                                                    {
                                                      "status": 404,
                                                      "error": "Not Found",
                                                      "message": "Producto inexistente: productId=99999 para brandId=1",
                                                      "timestamp": "2020-06-14T10:00:00"
                                                    }"""
                                    ),
                                    @ExampleObject(
                                            name = "404 - Cadena inexistente",
                                            summary = "La cadena no tiene precio para el producto y fecha indicados",
                                            value = """
                                                    {
                                                      "status": 404,
                                                      "error": "Not Found",
                                                      "message": "Cadena inexistente: brandId=999",
                                                      "timestamp": "2020-06-14T10:00:00"
                                                    }"""
                                    ),
                                    @ExampleObject(
                                            name = "404 - Fecha sin precio",
                                            summary = "No hay tarifa vigente para esa fecha",
                                            value = """
                                                    {
                                                      "status": 404,
                                                      "error": "Not Found",
                                                      "message": "No hay precio aplicable para la fecha indicada: brandId=1, productId=35455, applicationDate=2020-01-01T00:00",
                                                      "timestamp": "2020-01-01T00:00:00"
                                                    }"""
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<PriceResponse> getPrice(
            @Parameter(
                    description = "Fecha y hora de aplicacion en formato ISO 8601",
                    required = true,
                    examples = {
                            @ExampleObject(name = "Test 1 - 14/06 10:00", summary = "Tarifa 1, 35.50 EUR", value = "2020-06-14T10:00:00"),
                            @ExampleObject(name = "Test 2 - 14/06 16:00", summary = "Tarifa 2, 25.45 EUR", value = "2020-06-14T16:00:00"),
                            @ExampleObject(name = "Test 3 - 14/06 21:00", summary = "Tarifa 1, 35.50 EUR", value = "2020-06-14T21:00:00"),
                            @ExampleObject(name = "Test 4 - 15/06 10:00", summary = "Tarifa 3, 30.50 EUR", value = "2020-06-15T10:00:00"),
                            @ExampleObject(name = "Test 5 - 16/06 21:00", summary = "Tarifa 4, 38.95 EUR", value = "2020-06-16T21:00:00")
                    }
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate,
            @Parameter(
                    description = "Identificador del producto",
                    required = true,
                    examples = {
                            @ExampleObject(name = "Test 1 - 14/06 10:00", value = "35455"),
                            @ExampleObject(name = "Test 2 - 14/06 16:00", value = "35455"),
                            @ExampleObject(name = "Test 3 - 14/06 21:00", value = "35455"),
                            @ExampleObject(name = "Test 4 - 15/06 10:00", value = "35455"),
                            @ExampleObject(name = "Test 5 - 16/06 21:00", value = "35455")
                    }
            )
            @RequestParam Long productId,
            @Parameter(
                    description = "Identificador de la cadena/marca",
                    required = true,
                    examples = {
                            @ExampleObject(name = "Test 1 - 14/06 10:00", value = "1"),
                            @ExampleObject(name = "Test 2 - 14/06 16:00", value = "1"),
                            @ExampleObject(name = "Test 3 - 14/06 21:00", value = "1"),
                            @ExampleObject(name = "Test 4 - 15/06 10:00", value = "1"),
                            @ExampleObject(name = "Test 5 - 16/06 21:00", value = "1")
                    }
            )
            @RequestParam Long brandId) {
        var price = useCase.execute(brandId, productId, applicationDate);
        return ResponseEntity.ok(mapper.toResponse(price));
    }
}
