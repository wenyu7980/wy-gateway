package com.wenyu7980.gateway.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wenyu7980.authentication.api.domain.PermissionInternal;
import com.wenyu7980.authentication.api.service.PermissionInternalService;
import com.wenyu7980.common.context.domain.ContextInfo;
import com.wenyu7980.common.context.domain.ContextRequest;
import com.wenyu7980.common.exceptions.code403.TokenExpiredException;
import com.wenyu7980.common.exceptions.code404.NotFoundException;
import com.wenyu7980.common.gson.adapter.GsonUtil;
import com.wenyu7980.gateway.login.entity.TokenEntity;
import com.wenyu7980.gateway.login.service.TokenService;
import com.wenyu7980.gateway.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author wenyu
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    private static final String DOCS_PATH = "v3/api-docs";
    private final static AntPathMatcher MATCHER = new AntPathMatcher();
    @Autowired
    private WebClient.Builder builder;
    @Autowired
    private PermissionInternalService permissionInternalService;
    @Autowired
    private TokenService tokenService;
    private final Cache<String, List<PermissionInternal>> CACHE;

    private AuthenticationFilter() {
        this.CACHE = Caffeine.newBuilder().build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = RequestUtils.getPathWithoutService(request);
        if (path.endsWith(DOCS_PATH)) {
            return chain.filter(exchange);
        }
        String serviceName = RequestUtils.getServiceName(request);
        String method = RequestUtils.getMethod(request);
        // 获取token
        Optional<String> optional = RequestUtils.getHeader(request, "token");
        PermissionInternal permission = this.getPermission(serviceName, method, path)
          .orElseThrow(() -> new NotFoundException("接口不存在"));
        if (!permission.getCheck()) {
            return chain.filter(exchange.mutate().request(
              buildRequest(request, optional.flatMap(v -> tokenService.findOptionalById(v)).orElse(null), serviceName,
                permission.getPath())).build());
        }
        TokenEntity tokenEntity = tokenService
          .findOptionalById(optional.orElseThrow(() -> new TokenExpiredException("未携带token")))
          .orElseThrow(() -> new TokenExpiredException("token无效"));
        // 权限校验
        return chain.filter(
          exchange.mutate().request(buildRequest(request, tokenEntity, serviceName, permission.getPath())).build());
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

    @Scheduled(fixedDelay = 60 * 1000)
    private void refresh() {
        this.CACHE.put("PERMISSIONS", this.getPermissions());
    }

    private ServerHttpRequest buildRequest(ServerHttpRequest httpRequest, TokenEntity entity, String serviceName,
      String path) {
        ServerHttpRequest request = httpRequest.mutate().header("context", GsonUtil.gson().toJson(
          new ContextInfo(entity.getUserId(), new HashSet<>(), new HashSet<>(),
            new ContextRequest(serviceName, httpRequest.getMethodValue(), path)))).build();
        return new ServerHttpRequestDecorator(request);
    }

    private Optional<PermissionInternal> getPermission(String serviceName, String method, String path) {
        return this.CACHE.get("PERMISSIONS", key -> new ArrayList<>()).stream().filter(
          v -> Objects.equals(serviceName, v.getServiceName()) && Objects.equals(method, v.getMethod()) && MATCHER
            .match(v.getPath(), path)).findFirst();
    }

    private List<PermissionInternal> getPermissions() {
        return permissionInternalService.getList().stream().sorted((v1, v2) -> {
            int serviceName = v1.getServiceName().compareTo(v2.getServiceName());
            if (serviceName != 0) {
                return serviceName;
            }
            int method = v1.getMethod().compareTo(v2.getMethod());
            if (method != 0) {
                return method;
            }
            if (MATCHER.match(v1.getPath(), v2.getPath())) {
                return 1;
            }
            return -1;
        }).collect(Collectors.toList());
    }
}
