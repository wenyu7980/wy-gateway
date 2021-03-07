package com.wenyu7980.gateway;

import com.wenyu7980.authentication.api.domain.PermissionInternal;
import com.wenyu7980.common.context.domain.ContextInfo;
import com.wenyu7980.common.context.domain.ContextRequest;
import com.wenyu7980.common.gson.adapter.GsonUtil;
import com.wenyu7980.gateway.filter.component.FilterComponent;
import com.wenyu7980.gateway.login.entity.TokenEntity;
import com.wenyu7980.gateway.login.service.TokenService;
import com.wenyu7980.gateway.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Optional;

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
        ServerHttpRequest request = exchange.getRequest();
        // 获取token
        Optional<String> optional = RequestUtils.getHeader(request, "token");
        String serviceName = RequestUtils.getServiceName(request);
        String path = RequestUtils.getPathWithoutService(request);
        String method = RequestUtils.getMethod(request);
        PermissionInternal permission = filterComponent.getPermission(serviceName, method, path)
          .orElseThrow(() -> new RuntimeException("接口不存在"));
        if (!permission.getCheck()) {
            return chain.filter(exchange.mutate().request(buildRequest(request,
              optional.isPresent() ? tokenService.findOptionalById(optional.get()).orElse(null) : null, serviceName,
              permission.getPath())).build());
        }
        if (!optional.isPresent()) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }
        Optional<TokenEntity> optionalTokenEntity = tokenService.findOptionalById(optional.get());
        if (!optionalTokenEntity.isPresent()) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }
        TokenEntity tokenEntity = optionalTokenEntity.get();
        // 权限校验
        return chain.filter(
          exchange.mutate().request(buildRequest(request, tokenEntity, serviceName, permission.getPath())).build());
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

    private ServerHttpRequest buildRequest(ServerHttpRequest httpRequest, TokenEntity entity, String serviceName,
      String path) {
        ServerHttpRequest request = httpRequest.mutate().header("context", GsonUtil.gson().toJson(
          new ContextInfo(entity.getUserId(), new HashSet<>(), new HashSet<>(),
            new ContextRequest(serviceName, httpRequest.getMethodValue(), path)))).build();
        return new ServerHttpRequestDecorator(request);
    }

}
