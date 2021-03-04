package com.wenyu7980.gateway.filter.service;

import com.wenyu7980.authentication.api.domain.PermissionInternal;

import java.util.List;
import java.util.function.Supplier;

/**
 *
 * @author wenyu
 */
public interface FilterCacheService {
    /**
     * 获取
     * @param supplier
     * @return
     */
    List<PermissionInternal> getPermissions(Supplier<List<PermissionInternal>> supplier);
}
