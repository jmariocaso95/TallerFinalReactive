package com.example.taller.common;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

/**
 * Lee X-Traza-Id (o X-Correlation-Id legacy), y lo pone en Reactor Context.
 * Cualquier operador aguas abajo lo recupera con Mono.deferContextual sin recibirlo como parámetro.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationWebFilter implements WebFilter {

    public static final String KEY = "trazaId";
    public static final String HEADER = "X-Traza-Id";
    public static final String LEGACY_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String cid = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HEADER))
                .or(() -> Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(LEGACY_HEADER)))
                .orElse(UUID.randomUUID().toString());
        exchange.getResponse().getHeaders().add(HEADER, cid);
        exchange.getResponse().getHeaders().add(LEGACY_HEADER, cid);
        return chain.filter(exchange).contextWrite(ctx -> ctx.put(KEY, cid));
    }
}
