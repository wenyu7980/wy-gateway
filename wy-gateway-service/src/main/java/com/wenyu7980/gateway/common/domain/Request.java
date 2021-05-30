package com.wenyu7980.gateway.common.domain;

import com.wenyu7980.gateway.util.RequestUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.AntPathMatcher;

import java.util.Objects;

public class Request {
    private final static AntPathMatcher MATCHER = new AntPathMatcher();
    private String serviceName;
    private String method;
    private String path;

    public Request(ServerHttpRequest request) {
        this.serviceName = RequestUtils.getServiceName(request);
        this.method = RequestUtils.getMethod(request);
        this.path = RequestUtils.getPathWithoutService(request);
    }

    /**
     *
     * @param serviceName
     * @param method
     * @param path
     * @return
     */
    public boolean match(String serviceName, String method, String path) {
        return Objects.equals(serviceName, this.serviceName) && Objects.equals(method, this.method) && MATCHER
          .match(path, this.path);
    }
}
