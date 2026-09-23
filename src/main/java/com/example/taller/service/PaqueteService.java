package com.example.taller.service;

import com.example.taller.model.Paquete;
import com.example.taller.repository.PaqueteRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PaqueteService {
    private PaqueteRepository paqueteRepository;

    public PaqueteService(PaqueteRepository paqueteRepository) {
        this.paqueteRepository = paqueteRepository;
    }

    public Flux<Paquete> getAllPaquetes() {
        return paqueteRepository.findAll();
    }

    public Mono<Paquete> savePaquete(Paquete paquete) {
        var vpaquete = Mono.just(paquete);
        return paqueteRepository.save(paquete);
    }

    public Mono<Paquete> findPaquetesById(Long id) {
        return paqueteRepository.findByVehiculoId(id);
    }
    public Flux<Paquete> findPaquetesByDespachoId(Long id) {
        return paqueteRepository.findByDespachoId(id);
    }
    public Mono<Paquete> findVehiculoById(Long id) {
        return paqueteRepository.findByVehiculoId(id);
    }

}
