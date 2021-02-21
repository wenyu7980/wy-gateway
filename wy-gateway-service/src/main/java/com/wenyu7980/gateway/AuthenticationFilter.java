package com.wenyu7980.gateway;

import com.wenyu7980.authentication.api.domain.Permission;
import com.wenyu7980.common.authentication.util.AuthenticationInfo;
import com.wenyu7980.common.authentication.util.AuthenticationRequest;
import com.wenyu7980.common.gson.adapter.GsonUtil;
import com.wenyu7980.gateway.filter.component.FilterComponent;
import com.wenyu7980.gateway.login.entity.TokenEntity;
import com.wenyu7980.gateway.login.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 *
 * @author wenyu
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    @Autowired
    private FilterComponent filterComponent;
    @Autowired
    private TokenService tokenService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取token
        Optional<String> optional = exchange.getRequest().getHeaders().get("token").stream().findFirst();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        String serviceName = route.getUri().getHost();
        String path = exchange.getRequest().getPath().subPath(4).value();
        String method = exchange.getRequest().getMethodValue();
        Permission permission = filterComponent.getPermission(serviceName, method, path)
          .orElseThrow(() -> new RuntimeException("不能存在"));
        if (!permission.getCheck()) {
            String token = optional.get();
            Optional<TokenEntity> optionalTokenEntity = tokenService.findOptionalById(token);
            if (optionalTokenEntity.isPresent()) {
                return chain.filter(
                  exchange.mutate().request(buildRequest(exchange.getRequest(), optionalTokenEntity.get())).build());
            }
            return chain.filter(exchange.mutate().build());
        }
        if (!optional.isPresent()) {
            ServerHttpResponse response = exchange.getResponse();
            DataBuffer buffer = response.bufferFactory().wrap("未登录".getBytes(StandardCharsets.UTF_8));
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }
        String token = optional.get();
        Optional<TokenEntity> optionalTokenEntity = tokenService.findOptionalById(token);
        if (!optionalTokenEntity.isPresent()) {
            ServerHttpResponse response = exchange.getResponse();
            DataBuffer buffer = response.bufferFactory().wrap("未登录".getBytes(StandardCharsets.UTF_8));
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }
        TokenEntity tokenEntity = optionalTokenEntity.get();
        // 权限校验
        return chain.filter(exchange.mutate().build());
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

    private ServerHttpRequest buildRequest(ServerHttpRequest httpRequest, TokenEntity entity) {
        ServerHttpRequest request = httpRequest.mutate().header("authInfo",
          GsonUtil.gson().toJson(new AuthenticationInfo(entity.getUserId(), new HashSet<>(), new HashSet<>(),
            // TODO,path要使用权限中的path（包含有通配符）
            new AuthenticationRequest("serviceName", "method", "path")))).build();
        return new ServerHttpRequestDecorator(request);
    }

}
