package com.wenyu7980.gateway.common.component;

import com.wenyu7980.authentication.api.domain.Permission;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Optional;

public interface PermissionComponent {
    /**
     * 获取
     * @param request
     * @return
     */
    Optional<Permission> getPermissionFromRequest(ServerHttpRequest request);
}
