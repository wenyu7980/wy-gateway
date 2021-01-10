package com.wenyu7980.gateway.login.handler.impl;

import com.wenyu7980.authentication.api.domain.LoginInternal;
import com.wenyu7980.authentication.api.domain.LoginResultInternal;
import com.wenyu7980.authentication.api.service.LoginInternalService;
import com.wenyu7980.gateway.login.domain.Login;
import com.wenyu7980.gateway.login.domain.LoginResult;
import com.wenyu7980.gateway.login.handler.LoginHandler;
import com.wenyu7980.gateway.login.service.RsaKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 *
 * @author wenyu
 */
@Component
public class LoginHandlerImpl implements LoginHandler {
    @Autowired
    private LoginInternalService loginInternalService;
    @Autowired
    private RsaKeyService rsaKeyService;

    @Override
    public Mono<LoginResult> login(Login login) throws IOException, GeneralSecurityException {
        LoginInternal loginInternal = new LoginInternal();
        loginInternal.setPassword(rsaKeyService.decode(login.getPublicKeyCode(), login.getPassword()));
        loginInternal.setUsername(login.getUsername());
        LoginResultInternal result = loginInternalService.login(loginInternal);
        LoginResult loginResult = new LoginResult();
        loginResult.setToken(result.getToken());
        return Mono.just(loginResult);
    }
}
