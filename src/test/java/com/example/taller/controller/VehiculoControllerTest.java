package com.example.taller.controller;

import com.example.taller.model.Vehiculo;
import com.example.taller.repository.VehiculoRepository;
import com.example.taller.service.VehiculoBulkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
@AutoConfigureWebTestClient(timeout = "30000")
public class VehiculoControllerTest {

    @Autowired
    private WebTestClient client;

    private VehiculoRepository vehiculoRepository;
    private VehiculoController controller;

    @BeforeEach
    void setUp() {
        vehiculoRepository = mock(VehiculoRepository.class);
        VehiculoBulkService service = mock(VehiculoBulkService.class);
        controller = new VehiculoController(service, vehiculoRepository);
    }

    @Test
    void create_debeRetornarCreated() {
        Vehiculo input = new Vehiculo(null, "ABC123", 1000.0, 1000.0, "BOG", "ACTIVO");
        Vehiculo saved = new Vehiculo(1L, "ABC123", 1000.0, 1000.0, "BOG", "ACTIVO");
        when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(controller.crearNuevo(input))
                .assertNext(res -> {
                    assertThat(res.getCiudad()).isEqualTo(input.getCiudad());
                    assertThat(res.getId()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("GET /api/vehiculo devuelve el catalogo de la demo")
    void listado_devuelveElCatalogo() {
        List<Vehiculo> vehiculos = client.get().uri("/api/vehiculos")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Vehiculo.class)
                .returnResult()
                .getResponseBody();

        assertThat(vehiculos).isNotNull().hasSize(3);
    }

    @Test
    @DisplayName("GET /api/vehiculos/{id} inexistente devuelve 404")
    void porId_inexistente_devuelve404() {
        client.get().uri("/api/vehiculos/9999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("el /stream emite Server-Sent Events, no un JSON de una sola vez")
    void stream_emiteEventos() {
        client.get().uri("/api/vehiculos/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);
    }
    @Test
    @DisplayName("el consumidor puede tomar 2 elementos y cortar el flujo")
    void stream_permiteCortarElFlujo() {
        List<Vehiculo> primeros = client.get().uri("/api/vehiculos/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Vehiculo.class)
                .getResponseBody()
                .take(3)
                .collectList()
                .block(Duration.ofSeconds(20));

        assertThat(primeros).hasSize(3);
    }
}
