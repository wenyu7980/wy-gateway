package com.wenyu7980.gateway.filter.component;

import com.wenyu7980.authentication.api.domain.Permission;

import java.util.Optional;

/**
 *
 * @author wenyu
 */
public interface FilterComponent {
    /**
     *
     * @param serviceName
     * @param method
     * @param path
     * @return
     */
    Optional<Permission> getPermission(String serviceName, String method, String path);
}
