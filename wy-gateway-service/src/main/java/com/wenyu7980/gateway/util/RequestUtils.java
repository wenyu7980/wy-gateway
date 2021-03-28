package com.wenyu7980.gateway.util;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Optional;

/**
 *
 * @author wenyu
 */
public class RequestUtils {
    private RequestUtils() {
    }

    public static String getMethod(ServerHttpRequest request) {
        return request.getMethodValue();
    }

    public static String getPathWithoutService(ServerHttpRequest request) {
        return request.getPath().subPath(4).value();
    }

    public static String getServiceName(ServerHttpRequest request) {
        return request.getPath().subPath(3, 4).value();
    }

    public static Optional<String> getHeader(ServerHttpRequest request, String key) {
        if (request.getHeaders().containsKey(key)) {
            return request.getHeaders().get(key).stream().findFirst();
        }
        return Optional.empty();
    }

}
