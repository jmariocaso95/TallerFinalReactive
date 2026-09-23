package com.example.taller.controller;

import com.example.taller.model.Paquete;
import com.example.taller.repository.PaqueteRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Paquetes")
@RestController
@RequestMapping("/api/paquetes")
public class PaqueteController {
    @Autowired
    private PaqueteRepository paqueteRepository;

    @GetMapping
    public Flux<Paquete> findAll() {
        return paqueteRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Paquete> findById(@PathVariable Long id) {
        return paqueteRepository.findById(id);
    }

    @GetMapping("/vehiculo/{vehiculoId}")
    public Mono<Paquete> findByVehiculoId(@PathVariable Long vehiculoId) {
        return paqueteRepository.findByVehiculoId(vehiculoId);
    }

    @GetMapping("/despacho/{despachoId}")
    public Flux<Paquete> findByDespachoId(@PathVariable Long despachoId) {
        return paqueteRepository.findByDespachoId(despachoId);
    }

    @PostMapping
    public Mono<Paquete> save(@RequestBody Paquete paquete) {
        return paqueteRepository.save(paquete);
    }
}
