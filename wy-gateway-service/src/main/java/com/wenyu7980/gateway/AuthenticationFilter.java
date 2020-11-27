package com.wenyu7980.gateway;

import com.wenyu7980.authentication.api.domain.Permission;
import com.wenyu7980.authentication.api.service.PermissionInternalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 *
 * @author wenyu
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    @Autowired
    private PermissionInternalService permissionInternalService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        AntPathMatcher matcher = new AntPathMatcher();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        String serviceName = route.getUri().getHost();
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethodValue();
        if ("/login".equals(path) && "POST".equals(method) && "wy-authentication".equals(serviceName)) {
            return chain.filter(exchange);
        }
        List<Permission> permissions = permissionInternalService.getList(false);
        for (Permission permission : permissions) {
            if (serviceName.equals(permission.getServiceName()) && matcher.match(permission.getPath(), path) && method
              .equals(permission.getMethod()) && !permission.getCheck()) {
                return chain.filter(exchange);
            }
        }
        ServerHttpResponse response = exchange.getResponse();
        DataBuffer buffer = response.bufferFactory().wrap("权限不足".getBytes(StandardCharsets.UTF_8));
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
