package com.wenyu7980.gateway.login.controller;

import com.wenyu7980.gateway.login.domain.Login;
import com.wenyu7980.gateway.login.domain.LoginResult;
import com.wenyu7980.gateway.login.handler.LoginHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "登录/退出")
@RestController
@RequestMapping()
public class LoginController {
    @Autowired
    private LoginHandler loginHandler;

    @ApiOperation("登录")
    @PostMapping("login")
    public Mono<LoginResult> login(@RequestBody @Valid Login login) throws IOException, GeneralSecurityException {
        return loginHandler.login(login);
    }
}
