package com.example.taller.controller;

import com.example.taller.model.Paquete;
import com.example.taller.repository.PaqueteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

class PaqueteControllerTest {

    private PaqueteRepository paqueteRepository;
    private PaqueteController controller;

    @BeforeEach
    void setUp() {
        paqueteRepository = mock(PaqueteRepository.class);
        controller = new PaqueteController();
        // El controller usa @Autowired en el campo; lo asignamos vía reflexión para el test unitario.
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "paqueteRepository", paqueteRepository);
    }

    @Test
    @DisplayName("findAll delega en el repositorio")
    void findAll_delegaEnRepositorio() {
        Paquete p = new Paquete(1L, 1L, 10, 100.0, "caja");
        when(paqueteRepository.findAll()).thenReturn(Flux.just(p));

        StepVerifier.create(controller.findAll())
                .assertNext(res -> assertThat(res.getDescripcion()).isEqualTo("caja"))
                .verifyComplete();
    }

    @Test
    @DisplayName("findById delega en el repositorio")
    void findById_delegaEnRepositorio() {
        Paquete p = new Paquete(1L, 1L, 10, 100.0, "caja");
        p.setId(5L);
        when(paqueteRepository.findById(5L)).thenReturn(Mono.just(p));

        StepVerifier.create(controller.findById(5L))
                .assertNext(res -> assertThat(res.getId()).isEqualTo(5L))
                .verifyComplete();
    }

    @Test
    @DisplayName("findByVehiculoId delega en el repositorio")
    void findByVehiculoId_delegaEnRepositorio() {
        Paquete p = new Paquete(1L, 2L, 10, 100.0, "caja");
        when(paqueteRepository.findByVehiculoId(2L)).thenReturn(Mono.just(p));

        StepVerifier.create(controller.findByVehiculoId(2L))
                .assertNext(res -> assertThat(res.getVehiculoId()).isEqualTo(2L))
                .verifyComplete();
    }

    @Test
    @DisplayName("findByDespachoId delega en el repositorio")
    void findByDespachoId_delegaEnRepositorio() {
        Paquete p = new Paquete(3L, 2L, 10, 100.0, "caja");
        when(paqueteRepository.findByDespachoId(3L)).thenReturn(Flux.just(p));

        StepVerifier.create(controller.findByDespachoId(3L))
                .assertNext(res -> assertThat(res.getDespachoId()).isEqualTo(3L))
                .verifyComplete();
    }

    @Test
    @DisplayName("save delega en el repositorio")
    void save_delegaEnRepositorio() {
        Paquete p = new Paquete(1L, 1L, 10, 100.0, "caja");
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(Mono.just(p));

        StepVerifier.create(controller.save(p))
                .assertNext(res -> assertThat(res.getDescripcion()).isEqualTo("caja"))
                .verifyComplete();
    }
}
