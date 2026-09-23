package com.example.taller.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Metadatos del contrato OpenAPI. El documento se sirve en /v3/api-docs (JSON) y /v3/api-docs.yaml,
 * y se exporta a docs/openapi.{json,yaml} con `./gradlew exportOpenApi` para importarlo en Postman.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI demoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Despacho reactivo sobre R2DBC")
                        .version("1.0.0")
                        .description("""
                                CRUD reactivo para vehículos, paquetes y despachos.
                                Proyecto usa Spring WebFlux + R2DBC, sin operaciones bloqueantes.

                                Entidades:
                                - Vehículo: placa, cupo, ciudad, estado.
                                - Paquete: despacho asociado, vehículo asignado, peso y valor.
                                - Despacho: ciudad, estado, peso total, score de riesgo.

                                Errores devuelven: { codigo, mensaje, trazaId, instante }.""")
                        .contact(new Contact().name("Equipo demo"))
                        .license(new License().name("Uso interno")))
                .servers(List.of(new Server().url("http://localhost:8081").description("Local")))
                .tags(List.of(
                        new Tag().name("Vehiculos").description("CRUD reactivo de vehículos"),
                        new Tag().name("Paquetes").description("CRUD reactivo de paquetes"),
                        new Tag().name("Despachos").description("CRUD reactivo de despachos")));
    }

    /** El CorrelationWebFilter lee este header en cualquier endpoint y lo devuelve en la respuesta. */
    @Bean
    public OperationCustomizer correlationIdHeader() {
        return (operation, handlerMethod) -> operation.addParametersItem(new HeaderParameter()
                .name("X-Traza-Id")
                .description("Traza propagada por Reactor Context hasta logs, eventos SSE y cuerpo de error. "
                        + "Si no se envía, la app genera uno y lo devuelve en la respuesta.")
                .required(false)
                .example("demo-1")
                .schema(new StringSchema()));
    }
}
