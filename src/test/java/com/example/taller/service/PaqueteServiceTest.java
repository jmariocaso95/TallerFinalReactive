package com.example.taller.service;

import com.example.taller.model.Paquete;
import com.example.taller.repository.PaqueteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaqueteServiceTest {

    private PaqueteRepository paqueteRepository;
    private PaqueteService paqueteService;

    @BeforeEach
    void setUp() {
        paqueteRepository = mock(PaqueteRepository.class);
        paqueteService = new PaqueteService(paqueteRepository);
    }

    @Test
    @DisplayName("getAllPaquetes delega en el repositorio")
    void getAllPaquetes_delegaEnRepositorio() {
        Paquete p = new Paquete(1L, 1L, 10, 100.0, "caja");
        when(paqueteRepository.findAll()).thenReturn(Flux.just(p));

        StepVerifier.create(paqueteService.getAllPaquetes())
                .assertNext(res -> assertThat(res.getDescripcion()).isEqualTo("caja"))
                .verifyComplete();
    }

    @Test
    @DisplayName("savePaquete delega en el repositorio")
    void savePaquete_delegaEnRepositorio() {
        Paquete p = new Paquete(1L, 1L, 10, 100.0, "caja");
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(Mono.just(p));

        StepVerifier.create(paqueteService.savePaquete(p))
                .assertNext(res -> assertThat(res.getDescripcion()).isEqualTo("caja"))
                .verifyComplete();
    }

    @Test
    @DisplayName("findPaquetesById delega en findByVehiculoId del repositorio")
    void findPaquetesById_delegaEnRepositorio() {
        Paquete p = new Paquete(1L, 2L, 10, 100.0, "caja");
        when(paqueteRepository.findByVehiculoId(2L)).thenReturn(Mono.just(p));

        StepVerifier.create(paqueteService.findPaquetesById(2L))
                .assertNext(res -> assertThat(res.getVehiculoId()).isEqualTo(2L))
                .verifyComplete();
    }

    @Test
    @DisplayName("findPaquetesByDespachoId delega en el repositorio")
    void findPaquetesByDespachoId_delegaEnRepositorio() {
        Paquete p = new Paquete(3L, 2L, 10, 100.0, "caja");
        when(paqueteRepository.findByDespachoId(3L)).thenReturn(Flux.just(p));

        StepVerifier.create(paqueteService.findPaquetesByDespachoId(3L))
                .assertNext(res -> assertThat(res.getDespachoId()).isEqualTo(3L))
                .verifyComplete();
    }

    @Test
    @DisplayName("findVehiculoById delega en el repositorio")
    void findVehiculoById_delegaEnRepositorio() {
        Paquete p = new Paquete(1L, 4L, 10, 100.0, "caja");
        when(paqueteRepository.findByVehiculoId(4L)).thenReturn(Mono.just(p));

        StepVerifier.create(paqueteService.findVehiculoById(4L))
                .assertNext(res -> assertThat(res.getVehiculoId()).isEqualTo(4L))
                .verifyComplete();
    }
}
