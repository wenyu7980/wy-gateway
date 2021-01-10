package com.wenyu7980.gateway.login.controller;

import com.wenyu7980.gateway.login.domain.Login;
import com.wenyu7980.gateway.login.domain.LoginResult;
import com.wenyu7980.gateway.login.handler.LoginHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 *
 * @author wenyu
 */
@RestController
@RequestMapping("api")
public class LoginController {
    @Autowired
    private LoginHandler loginHandler;

    @PostMapping("login")
    public Mono<LoginResult> login(@RequestBody @Valid Login login) throws IOException, GeneralSecurityException {
        return loginHandler.login(login);
    }
}
