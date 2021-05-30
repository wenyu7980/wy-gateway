package com.wenyu7980.gateway.common.component.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wenyu7980.authentication.api.domain.Permission;
import com.wenyu7980.authentication.api.service.PermissionFacade;
import com.wenyu7980.gateway.common.component.PermissionComponent;
import com.wenyu7980.gateway.common.domain.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PermissionComponentImpl implements PermissionComponent {
    private final static AntPathMatcher MATCHER = new AntPathMatcher();
    private final Cache<String, List<Permission>> CACHE;
    @Autowired
    private PermissionFacade permissionFacade;

    public PermissionComponentImpl() {
        this.CACHE = Caffeine.newBuilder().build();
    }

    @Override
    public Optional<Permission> getPermissionFromRequest(ServerHttpRequest request) {
        Request req = new Request(request);
        return this.CACHE.get("PERMISSIONS", key -> new ArrayList<>()).stream()
          .filter(v -> req.match(v.getServiceName(), v.getMethod(), v.getPath())).findFirst();
    }

    @Scheduled(fixedDelay = 60 * 1000)
    private void refresh() {
        this.CACHE.put("PERMISSIONS", this.getPermissions());
    }

    private List<Permission> getPermissions() {
        return permissionFacade.getList().stream().sorted((v1, v2) -> {
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
