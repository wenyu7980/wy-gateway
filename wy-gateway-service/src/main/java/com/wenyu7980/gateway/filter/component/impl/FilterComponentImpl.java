package com.wenyu7980.gateway.filter.component.impl;

import com.wenyu7980.authentication.api.domain.Permission;
import com.wenyu7980.authentication.api.service.PermissionInternalService;
import com.wenyu7980.gateway.filter.component.FilterComponent;
import com.wenyu7980.gateway.filter.service.FilterCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author wenyu
 */
@Component
public class FilterComponentImpl implements FilterComponent {
    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    @Autowired
    private PermissionInternalService permissionInternalService;
    @Autowired
    private FilterCacheService filterCacheService;

    @Override
    public Optional<Permission> getPermission(String serviceName, String method, String path) {
        List<Permission> permissions = filterCacheService
          .getPermissions(() -> this.sorted(this.permissionInternalService.getList(null)));
        return permissions.stream().filter(
          v -> Objects.equals(serviceName, v.getServiceName()) && Objects.equals(method, v.getMethod()) && MATCHER
            .match(v.getPath(), path)).findFirst();
    }

    private List<Permission> sorted(List<Permission> permissions) {
        return permissions.stream().sorted((v1, v2) -> {
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
