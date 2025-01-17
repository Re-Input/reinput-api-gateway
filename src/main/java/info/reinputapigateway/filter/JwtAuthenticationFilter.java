package info.reinputapigateway.filter;


import com.netflix.discovery.converters.Auto;
import info.reinputapigateway.util.JwtUtil;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
@Slf4j
public class JwtAuthenticationFilter  extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {
    @Autowired
    private JwtUtil jwtUtil;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = resolveToken(exchange);

            if (token != null && jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.extractUserId(token);
                exchange = addUserIdHeader(exchange, userId);
            }

            return chain.filter(exchange);
        };
    }

    private String resolveToken(ServerWebExchange exchange) {
        String authorizationHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    private ServerWebExchange addUserIdHeader(ServerWebExchange exchange, Long userId) {
        return exchange.mutate()
                .request(r -> r.headers(headers -> headers.add("X-User-Id", userId.toString())))
                .build();
    }

    public static class Config{
        // Put configuration properties here
    }
}
