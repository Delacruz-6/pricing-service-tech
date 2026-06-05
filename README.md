# Pricing Service

Prueba técnica Inditex — servicio REST de consulta de precios aplicables.

## Enunciado

Dado un producto, una cadena y una fecha de aplicación, el servicio debe devolver el precio aplicable en ese momento. Si existen varias tarifas vigentes, se devuelve la de mayor prioridad.

### Endpoint

```
GET /api/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1
```

### Casos de prueba (producto 35455, cadena 1)

| Fecha aplicación       | Tarifa esperada | Precio  |
|------------------------|-----------------|---------|
| 2020-06-14T10:00:00    | 1               | 35.50   |
| 2020-06-14T16:00:00    | 2               | 25.45   |
| 2020-06-14T21:00:00    | 1               | 35.50   |
| 2020-06-15T10:00:00    | 3               | 30.50   |
| 2020-06-16T21:00:00    | 4               | 38.95   |

## Requisitos

- Java 25, Spring Boot
- Base de datos H2 en memoria
- Tests unitarios e integración
