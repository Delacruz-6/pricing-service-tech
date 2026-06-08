# Pricing Service

Servicio REST que devuelve el precio aplicable a un producto para una cadena y fecha concretas. Si hay varias tarifas vigentes a la vez, se devuelve la de mayor prioridad.

Prueba técnica Inditex — implementación con arquitectura hexagonal.

---

## Stack

Java 25 · Spring Boot 4 · Spring Data JPA · H2 · Flyway · MapStruct · Lombok · Springdoc OpenAPI · Actuator

---

## Arquitectura

Hexagonal (Ports & Adapters). El dominio y los casos de uso no dependen de Spring ni de JPA; la infraestructura implementa los puertos.

```
com.inditex.ecommerce.pricing
├── domain/              → Price, DomainValidationException
├── application/         → GetApplicablePriceUseCase, PriceRepositoryPort, mappers
└── infrastructure/
    ├── in/rest/         → PriceController, DTOs
    ├── out/             → PricePersistenceAdapter, JPA, entidades
    ├── aop/             → LoggingAspect (trazas de entrada/salida)
    ├── config/          → Swagger
    └── exception/       → GlobalExceptionHandler
```

### Decisiones de diseño

- **Flyway** para versionar el esquema (`V1` schema, `V2` datos, `V3` índice compuesto). Me pareció más limpio que un `import.sql` suelto si el servicio crece.
- **Puerto `PriceRepositoryPort`** para que el caso de uso no conozca JPA. El adaptador traduce entidades ↔ dominio con MapStruct.
- **`PageRequest.of(0, 1)`** en la query: JPQL no admite `LIMIT 1` de forma portable; ordeno por `priority DESC` y pido solo el primero.
- **Tests por capa**: unitarios del use case, MockMvc del controlador y repositorio contra H2. Los cinco escenarios del enunciado están cubiertos en integración.
- **Perfiles** `local` / `dev` / `prod` solo para la URL base de Swagger; la lógica es la misma en todos.

---

## Endpoint

```
GET /api/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1
```

Respuesta `200`:

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 2,
  "startDate": "2020-06-14T15:00:00",
  "endDate": "2020-06-14T18:30:00",
  "price": 25.45,
  "currency": "EUR"
}
```

Sin tarifa aplicable → `404` con cuerpo estructurado (`ErrorResponse`).

---

## Base de datos

H2 en memoria, inicializada con migraciones Flyway en `src/main/resources/db/migration/`:

| Migración | Contenido |
|-----------|-----------|
| `V1__init_schema.sql` | Tabla `PRICES` |
| `V2__init_data.sql` | Datos del enunciado |
| `V3__add_prices_index.sql` | Índice `(brand_id, product_id, start_date, end_date)` |

- **JDBC URL:** `jdbc:h2:mem:pricedb`
- **Consola H2:** `http://localhost:8080/h2-console` (usuario `sa`, sin contraseña)

---

## Arranque

Requisito: JDK 25.

```bash
cd pricing-service
mvn spring-boot:run
```

Perfil activo por defecto: `local` (puerto 8080).

---

## Colección API

Con el servicio en marcha, la colección está disponible en:

```
http://localhost:8080/pricing-service-tech-collection.json
```

El fichero fuente vive en `pricing-service/src/main/resources/static/pricing-service-tech-collection.json` e incluye los cinco casos del enunciado y los escenarios de error (400 y 404).

### Importar

1. Arranca el servicio (ver [Arranque](#arranque)).
2. En Postman o Bruno: **Import** → pega la URL anterior o importa el fichero del proyecto.
3. La colección se llama **PRICING-SERVICE - Casos de prueba**.

### Estructura

| Carpeta | Contenido |
|---------|-----------|
| **Casos exitosos (enunciado)** | Tests 1–5 con la fecha y el precio esperado |
| **Casos de error** | Producto/cadena inexistente, fecha sin precio, parámetros inválidos |

### Configurar

La colección define `baseUrl = http://localhost:8080`. Cámbiala en **Variables** si usas otro host o puerto.

### Documentación OpenAPI

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## Tests

```bash
cd pricing-service
mvn test
```

En test se desactiva Flyway y se usa `data.sql` con `ddl-auto: create-drop` para aislar cada ejecución.

Cubre los cinco casos del enunciado (14, 15 y 16 de junio de 2020, producto 35455) más escenarios negativos (404, parámetros inválidos).
