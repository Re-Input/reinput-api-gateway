package info.reinputapigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final String TRACE_ID = "X-Trace-Id";
    private static final String START_TIME = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = UUID.randomUUID().toString();
        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .header(TRACE_ID, traceId)
                .build();

        log.info("[{}] Request: {} {}", traceId, request.getMethod(), request.getURI().getRawPath());

        return chain.filter(exchange.mutate().request(request).build())
                .then(Mono.fromRunnable(() -> {
                    Long startTime = exchange.getAttribute(START_TIME);
                    long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
                    log.info("[{}] Response: {} in {}ms",
                            traceId,
                            exchange.getResponse().getStatusCode(),
                            duration);
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
