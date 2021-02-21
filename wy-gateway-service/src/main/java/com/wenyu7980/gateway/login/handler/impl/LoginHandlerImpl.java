package com.wenyu7980.gateway.login.handler.impl;

import com.wenyu7980.authentication.api.domain.LoginInternal;
import com.wenyu7980.authentication.api.domain.LoginResultInternal;
import com.wenyu7980.authentication.api.service.LoginInternalService;
import com.wenyu7980.gateway.login.domain.Login;
import com.wenyu7980.gateway.login.domain.LoginResult;
import com.wenyu7980.gateway.login.entity.TokenEntity;
import com.wenyu7980.gateway.login.handler.LoginHandler;
import com.wenyu7980.gateway.login.service.RsaKeyService;
import com.wenyu7980.gateway.login.service.TokenService;
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
    @Autowired
    private TokenService tokenService;

    @Override
    public Mono<LoginResult> login(Login login) throws IOException, GeneralSecurityException {
        LoginInternal loginInternal = new LoginInternal();
        loginInternal.setPassword(rsaKeyService.decode(login.getPublicKeyCode(), login.getPassword()));
        loginInternal.setUsername(login.getUsername());
        // 调用AuthenticationService
        LoginResultInternal result = loginInternalService.login(loginInternal);
        LoginResult loginResult = new LoginResult();
        loginResult.setToken(result.getToken());
        loginResult.setUserId(result.getUserId());
        tokenService.save(
          new TokenEntity(result.getToken(), rsaKeyService.decode(login.getPublicKeyCode(), login.getRandomCode()),
            result.getUserId(), 24 * 60 * 60L));
        return Mono.just(loginResult);
    }
}
