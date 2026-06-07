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
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## Arranque

Requisito: JDK 25.

```bash
cd pricing-service
mvn spring-boot:run
```

Perfil activo por defecto: `local` (puerto 8080).

---

## Colección API (Postman)

En la raíz del repositorio está `pricing-service-tech-collection.json`, una colección Postman con el endpoint `GET /api/prices` y ejemplos de respuesta (200, 400, 404 y 500).

### Importar

1. Abre Postman.
2. **Import** → arrastra o selecciona `pricing-service-tech-collection.json`.
3. La colección se llama **PRICING-SERVICE**.

### Configurar

La colección define la variable `baseUrl` con valor `http://localhost:8080`. Si el servicio corre en otro host o puerto, edítala en la pestaña **Variables** de la colección.

### Ejecutar

1. Arranca el servicio (ver [Arranque](#arranque)).
2. Abre la petición **api → prices → Obtener precio aplicable**.
3. Pulsa **Send**.

Parámetros por defecto de la petición:

| Parámetro | Valor | Descripción |
|-----------|-------|-------------|
| `applicationDate` | `2020-06-14T10:00:00` | Fecha y hora en ISO 8601 |
| `productId` | `35455` | ID del producto |
| `brandId` | `1` | ID de la cadena (1 = ZARA) |

Para reproducir los cinco casos del enunciado, cambia `applicationDate` a:

- `2020-06-14T10:00:00` → precio 35.50 (lista 1)
- `2020-06-14T16:00:00` → precio 25.45 (lista 2)
- `2020-06-14T21:00:00` → precio 35.50 (lista 1)
- `2020-06-15T10:00:00` → precio 30.50 (lista 3)
- `2020-06-16T21:00:00` → precio 38.95 (lista 4)

También puedes usar **Collection Runner** para lanzar la petición varias veces con distintos valores de query.

---

## Tests

```bash
cd pricing-service
mvn test
```

En test se desactiva Flyway y se usa `data.sql` con `ddl-auto: create-drop` para aislar cada ejecución.

Cubre los cinco casos del enunciado (14, 15 y 16 de junio de 2020, producto 35455) más escenarios negativos (404, parámetros inválidos).
