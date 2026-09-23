package com.example.taller.service;

import com.example.taller.common.DomainExceptions.CupoInsuficienteException;
import com.example.taller.model.Vehiculo;
import com.example.taller.repository.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VehiculoReservaServiceTest {

    private VehiculoRepository vehiculoRepository;
    private VehiculoReservaService service;

    @BeforeEach
    void setUp() {
        vehiculoRepository = mock(VehiculoRepository.class);
        service = new VehiculoReservaService(vehiculoRepository);
    }

    @Test
    @DisplayName("reservarUno reserva en el primer candidato con cupo suficiente")
    void reservarUno_reservaEnPrimerCandidato() {
        Vehiculo v = new Vehiculo(1L, "ABC", 90.0, 10.0, "BOG", "ACTIVO");
        when(vehiculoRepository.findByCiudadAndEstadoOrderByCupoKgDesc("BOG", "ACTIVO")).thenReturn(Flux.just(v));
        when(vehiculoRepository.reservarCupo(1L, 10.0)).thenReturn(Mono.just(v));

        StepVerifier.create(service.reservarUno("BOG", 10.0))
                .expectNext(v)
                .verifyComplete();
    }

    @Test
    @DisplayName("reservarUno prueba el siguiente candidato si el primero no tiene cupo")
    void reservarUno_pruebaSiguienteCandidato() {
        Vehiculo v1 = new Vehiculo(1L, "ABC", 5.0, 0.0, "BOG", "ACTIVO");
        Vehiculo v2 = new Vehiculo(2L, "DEF", 90.0, 10.0, "BOG", "ACTIVO");
        when(vehiculoRepository.findByCiudadAndEstadoOrderByCupoKgDesc("BOG", "ACTIVO")).thenReturn(Flux.just(v1, v2));
        when(vehiculoRepository.reservarCupo(1L, 10.0)).thenReturn(Mono.empty());
        when(vehiculoRepository.reservarCupo(2L, 10.0)).thenReturn(Mono.just(v2));

        StepVerifier.create(service.reservarUno("BOG", 10.0))
                .expectNext(v2)
                .verifyComplete();
    }

    @Test
    @DisplayName("reservarUno falla con CupoInsuficienteException si ningún candidato alcanza")
    void reservarUno_fallaSinCandidatos() {
        when(vehiculoRepository.findByCiudadAndEstadoOrderByCupoKgDesc("BOG", "ACTIVO")).thenReturn(Flux.empty());

        StepVerifier.create(service.reservarUno("BOG", 10.0))
                .expectError(CupoInsuficienteException.class)
                .verify();
    }

    @Test
    @DisplayName("reservarParaPaquetes compensa las reservas ya hechas si un paquete falla a mitad")
    void reservarParaPaquetes_compensaSiFallaAMitad() {
        Vehiculo v = new Vehiculo(1L, "ABC", 90.0, 10.0, "BOG", "ACTIVO");
        when(vehiculoRepository.findByCiudadAndEstadoOrderByCupoKgDesc("BOG", "ACTIVO"))
                .thenReturn(Flux.just(v));
        when(vehiculoRepository.reservarCupo(1L, 10.0)).thenReturn(Mono.just(v));
        when(vehiculoRepository.reservarCupo(1L, 999.0)).thenReturn(Mono.empty());
        when(vehiculoRepository.liberarCupo(eq(1L), eq(10.0))).thenReturn(Mono.just(v));

        StepVerifier.create(service.reservarParaPaquetes("BOG", List.of(10, 999)))
                .expectError(CupoInsuficienteException.class)
                .verify();

        verify(vehiculoRepository, times(1)).liberarCupo(1L, 10.0);
    }

    @Test
    @DisplayName("compensar libera el cupo de cada reserva capturada")
    void compensar_liberaCadaReserva() {
        Vehiculo v = new Vehiculo(1L, "ABC", 90.0, 10.0, "BOG", "ACTIVO");
        when(vehiculoRepository.liberarCupo(1L, 10.0)).thenReturn(Mono.just(v));

        StepVerifier.create(service.compensar(List.of(new VehiculoReservaService.ReservaHecha(v, 10.0))))
                .verifyComplete();

        verify(vehiculoRepository, times(1)).liberarCupo(1L, 10.0);
    }

    @Test
    @DisplayName("confirmar delega en confirmarCupo del repositorio")
    void confirmar_delegaEnRepositorio() {
        Vehiculo v = new Vehiculo(1L, "ABC", 90.0, 0.0, "BOG", "ACTIVO");
        when(vehiculoRepository.confirmarCupo(1L, 10.0)).thenReturn(Mono.just(v));

        StepVerifier.create(service.confirmar(1L, 10.0))
                .verifyComplete();

        verify(vehiculoRepository, never()).liberarCupo(eq(1L), eq(10.0));
    }
}
