package com.example.taller.external;

import com.example.taller.common.ReactiveSupport;
import com.example.taller.dto.ClimaResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cliente de clima/ventana de entrega. El servicio simulado es lento, así que la respuesta
 * se cachea 10 minutos por ciudad para no pagar la latencia en cada solicitud.
 */
@Component
public class ClimaClient {

    private static final Duration TTL_CACHE = Duration.ofMinutes(10);

    private final WebClient webClient;
    private final ConcurrentHashMap<String, Mono<ClimaResponse>> cachePorCiudad = new ConcurrentHashMap<>();

    public ClimaClient(WebClient externalWebClient) {
        this.webClient = externalWebClient;
    }

    public Mono<ClimaResponse> climaPara(String ciudad) {
        return cachePorCiudad.computeIfAbsent(ciudad, this::consultar);
    }

    private Mono<ClimaResponse> consultar(String ciudad) {
        return ReactiveSupport.traced("clima:" + ciudad, webClient.get()
                        .uri("/external/clima/{ciudad}", ciudad)
                        .retrieve()
                        .bodyToMono(ClimaResponse.class))
                .cache(TTL_CACHE);
    }
}
