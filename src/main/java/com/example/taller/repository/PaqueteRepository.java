package com.example.taller.repository;

import com.example.taller.model.Paquete;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PaqueteRepository extends R2dbcRepository<Paquete, Long> {

    /** Un despacho puede tener varios paquetes. */
    Flux<Paquete> findByDespachoId(Long despachoId);

    Mono<Paquete> findByVehiculoId(Long vehiculoId);
}
