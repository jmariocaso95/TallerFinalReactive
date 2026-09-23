package com.example.taller.repository;

import com.example.taller.model.Despacho;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
public interface DespachoRepository extends R2dbcRepository<Despacho, Long> {

    Mono<Despacho> findByIdemKey(String idemKey);

    Flux<Despacho> findByCiudad(String ciudad);

    Flux<Despacho> findByCiudadAndClienteId(String ciudad, Long clienteId);

    Flux<Despacho> findByEstado(String estado);

    Flux<Despacho> findByClienteId(Long clienteId);

    Flux<Despacho> findByTrazaId(String trazaId);

    /** Usado por el job de expiración: despachos ASIGNADOs cuya reserva ya venció. */
    Flux<Despacho> findByEstadoAndExpiraEnBefore(String estado, Instant instante);
}
