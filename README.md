# Servicio reactivo: vehículo, paquete, despacho

Spring Boot 4.1.1 · Java 17 · WebFlux · R2DBC PostgreSQL.

## Cómo levantar

1. Levantar PostgreSQL:
   - `docker compose up -d`
2. Iniciar app:
   - Windows: `.\gradlew.bat bootRun`
   - Linux/macOS: `./gradlew bootRun`

## Cómo probar

Sin intervención manual:

- Windows: `.\gradlew.bat test`
- Linux/macOS: `./gradlew test`

## Tabla: elemento reactivo -> archivo:línea

| Elemento reactivo | Archivo:línea |
|---|---|
| `Mono.zip` (externos en paralelo) | `service/ServiciosExternosClient.java:44` |
| `retryWhen(backoff)` | `service/ServiciosExternosClient.java:28` |
| `timeout` antifraude | `service/ServiciosExternosClient.java:39` |
| `cache(Duration)` clima | `service/ServiciosExternosClient.java:34` |
| `contextWrite` traza | `common/CorrelationWebFilter.java:33` |
| `Sinks.many().multicast()` | `service/EventBus.java:14` |
| `onBackpressureLatest` tablero | `service/TableroService.java:14` |
| `publish().refCount(1)` tablero hot compartido | `service/TableroService.java:15-16` |
| `limitRate(100)` reporte stream | `service/ReporteService.java:40` |
| `concatMap` saga (reserva/compensación) | `service/ReservaSaga.java:26,44` |
| `tx.transactional(flow)` | `service/DespachoFlowService.java:78,97` |
| `DatabaseClient` + `UPDATE ... RETURNING` cupo atómico | `service/CupoService.java:19-24` |
| `takeUntil(EventoDespacho::esTerminal)` SSE por despacho | `controller/DespachoController.java:112` |
| `Flux.interval` heartbeat SSE despacho | `controller/DespachoController.java:117` |
| `doOnCancel` / `doFinally` SSE despacho | `controller/DespachoController.java:122-123` |
| `application/x-ndjson` entrada | `controller/VehiculoController.java:77` |
| `application/x-ndjson` salida | `controller/ReporteController.java:26` |
