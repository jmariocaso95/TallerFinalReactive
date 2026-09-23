package com.example.taller.external;

import com.example.taller.common.AppProperties;
import com.example.taller.common.ReactiveSupport;
import com.example.taller.dto.RiesgoResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Cliente de scoring de zona de riesgo. El servicio simulado puede colgarse: se acota con
 * timeout y, si se dispara, se usa un score por defecto en vez de fallar el despacho completo.
 */
@Component
public class RiesgoClient {

    private final WebClient webClient;
    private final AppProperties props;

    public RiesgoClient(WebClient externalWebClient, AppProperties props) {
        this.webClient = externalWebClient;
        this.props = props;
    }

    public Mono<Integer> scorePara(String ciudad) {
        return ReactiveSupport.traced("riesgo:" + ciudad, webClient.get()
                        .uri("/external/riesgo/{ciudad}", ciudad)
                        .retrieve()
                        .bodyToMono(RiesgoResponse.class)
                        .map(RiesgoResponse::score))
                .timeout(props.external().fraudTimeout())
                .onErrorResume(e -> Mono.just(props.defaultRiskScore()));
    }
}
