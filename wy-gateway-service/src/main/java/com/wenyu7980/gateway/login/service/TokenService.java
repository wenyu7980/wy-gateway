package com.wenyu7980.gateway.login.service;

import com.wenyu7980.gateway.login.entity.TokenEntity;

import java.util.Optional;

/**
 *
 * @author wenyu
 */
public interface TokenService {

    /**
     * 保存tokenEntity
     * @param tokenEntity
     */
    void save(TokenEntity tokenEntity);

    /**
     * 查询
     * @param token
     * @return
     */
    Optional<TokenEntity> findOptionalById(String token);
}
