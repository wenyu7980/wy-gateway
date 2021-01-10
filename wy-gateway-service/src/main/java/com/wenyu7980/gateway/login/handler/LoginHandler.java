package com.wenyu7980.gateway.login.handler;

import com.wenyu7980.gateway.login.domain.Login;
import com.wenyu7980.gateway.login.domain.LoginResult;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 *
 * @author wenyu
 */
public interface LoginHandler {
    /**
     * 登录
     * @param login
     * @return
     */
    Mono<LoginResult> login(Login login) throws IOException, GeneralSecurityException;
}
