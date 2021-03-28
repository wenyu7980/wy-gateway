package com.wenyu7980.gateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.WebFilter;

/**
 *
 * @author wenyu
 */
@Configuration
public class GatewayConfiguration {
    @Bean
    public WebFilter contextPathWebFilter() {
        String contextPath = "/api";
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if (request.getURI().getPath().startsWith(contextPath)) {
                return chain
                  .filter(exchange.mutate().request(request.mutate().contextPath(contextPath).build()).build());
            }
            return chain.filter(exchange);
        };
    }

}
