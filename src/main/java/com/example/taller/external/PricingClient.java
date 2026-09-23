package com.example.taller.external;

import com.example.taller.common.AppProperties;
import com.example.taller.common.ReactiveSupport;
import com.example.taller.dto.TarifaResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Cliente de tarifas por ciudad. El servicio simulado falla de forma intermitente:
 * se reintenta con backoff exponencial y, si se agotan los reintentos, se usa una tarifa
 * base de catálogo para no bloquear la creación del despacho.
 */
@Component
public class PricingClient {

    /** Catálogo de tarifas base, usado solo como fallback cuando el servicio externo no responde. */
    private static final Map<String, Double> CATALOGO_BASE = Map.of(
            "BOG", 55_000.0,
            "MDE", 48_000.0,
            "CLO", 46_000.0);
    private static final double TARIFA_BASE_DEFECTO = 50_000.0;

    private final WebClient webClient;
    private final AppProperties props;

    public PricingClient(WebClient externalWebClient, AppProperties props) {
        this.webClient = externalWebClient;
        this.props = props;
    }

    public Mono<Double> tarifaPara(String ciudad) {
        return ReactiveSupport.traced("tarifa:" + ciudad, webClient.get()
                        .uri("/external/tarifas/{ciudad}", ciudad)
                        .retrieve()
                        .bodyToMono(TarifaResponse.class)
                        .map(TarifaResponse::tarifa))
                .retryWhen(ReactiveSupport.backoff(3, java.time.Duration.ofMillis(200)))
                .timeout(props.external().pricingTimeout())
                .onErrorResume(e -> Mono.just(CATALOGO_BASE.getOrDefault(ciudad, TARIFA_BASE_DEFECTO)));
    }
}
