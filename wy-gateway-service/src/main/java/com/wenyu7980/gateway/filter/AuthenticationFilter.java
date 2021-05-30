package com.wenyu7980.gateway.filter;

import com.wenyu7980.authentication.api.domain.Permission;
import com.wenyu7980.authentication.context.model.ContextInfo;
import com.wenyu7980.authentication.context.model.Request;
import com.wenyu7980.common.exceptions.AbstractException;
import com.wenyu7980.common.exceptions.ErrorResponseBody;
import com.wenyu7980.common.exceptions.code401.InsufficientException;
import com.wenyu7980.common.exceptions.code403.TokenExpiredException;
import com.wenyu7980.common.exceptions.code404.NotFoundException;
import com.wenyu7980.common.gson.adapter.GsonUtil;
import com.wenyu7980.gateway.common.component.PermissionComponent;
import com.wenyu7980.gateway.login.entity.TokenEntity;
import com.wenyu7980.gateway.login.service.TokenService;
import com.wenyu7980.gateway.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author wenyu
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    private static final String DOCS_PATH = "v3/api-docs";
    @Autowired
    private PermissionComponent permissionComponent;
    @Autowired
    private TokenService tokenService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = RequestUtils.getPathWithoutService(request);
        if (path.endsWith(DOCS_PATH)) {
            return chain.filter(exchange);
        }
        // 获取token
        Optional<String> token = RequestUtils.getHeader(request, "token");
        Optional<Permission> optionalPermission = this.permissionComponent.getPermissionFromRequest(request);
        if (!optionalPermission.isPresent()) {
            return exception(exchange, new NotFoundException("接口不存在"));
        }
        Permission permission = optionalPermission.get();
        if (!permission.getCheck()) {
            TokenEntity entity = token.flatMap(v -> tokenService.findOptionalById(v)).orElse(null);
            if (entity != null) {
                return chain.filter(exchange.mutate().request(buildRequest(request, entity, permission)).build());
            } else {
                return chain.filter(exchange.mutate().request(buildRequest(request, permission)).build());
            }
        }
        if (!token.isPresent()) {
            return exception(exchange, new TokenExpiredException("未携带token"));
        }
        Optional<TokenEntity> tokenEntity = tokenService.findOptionalById(token.get());
        if (!tokenEntity.isPresent()) {
            return exception(exchange, new TokenExpiredException("token无效"));
        }
        // 权限校验
        if (!tokenEntity.get().getRolePermissions().stream().anyMatch(
          p -> Objects.equals(p.getMethod(), permission.getMethod()) && Objects
            .equals(p.getPath(), permission.getPath()) && Objects
            .equals(p.getServiceName(), permission.getServiceName()))) {
            return exception(exchange, new InsufficientException("权限不足"));
        }
        return chain.filter(exchange.mutate().request(buildRequest(request, tokenEntity.get(), permission)).build());
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

    private Mono<Void> exception(ServerWebExchange exchange, AbstractException exception) {
        ServerHttpResponse response = exchange.getResponse();
        ErrorResponseBody body = new ErrorResponseBody();
        body.setCodeMessage(exception.getCode(), exception.getMessage());
        byte[] data = GsonUtil.gson().toJson(body).getBytes();
        DataBuffer buffer = response.bufferFactory().wrap(data);
        response.setStatusCode(HttpStatus.valueOf(exception.getStatus()));
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

    private ServerHttpRequest buildRequest(ServerHttpRequest httpRequest, TokenEntity entity, Permission permission) {
        ServerHttpRequest request = httpRequest.mutate().header("context", GsonUtil.gson().toJson(
          new ContextInfo(entity.getUserId(), entity.getDepartmentId(), entity.getRolePermissions(),
            new Request(permission.getServiceName(), permission.getMethod(), permission.getPath())))).build();
        return new ServerHttpRequestDecorator(request);
    }

    private ServerHttpRequest buildRequest(ServerHttpRequest httpRequest, Permission permission) {
        ServerHttpRequest request = httpRequest.mutate().header("context", GsonUtil.gson().toJson(
          new ContextInfo(null, null, new ArrayList<>(),
            new Request(permission.getServiceName(), permission.getMethod(), permission.getPath())))).build();
        return new ServerHttpRequestDecorator(request);
    }
}
