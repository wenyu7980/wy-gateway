package com.wenyu7980.gateway.login.controller;

import com.wenyu7980.gateway.login.domain.PublicKey;
import com.wenyu7980.gateway.login.domain.PublicRasEncrypt;
import com.wenyu7980.gateway.login.domain.PublicRasEncryptResult;
import com.wenyu7980.gateway.login.handler.PublicKeyHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author wenyu
 */
@Api(tags = "rsa")
@RestController
@RequestMapping("login")
public class RsaController {
    @Autowired
    private PublicKeyHandler publicKeyHandler;

    @ApiOperation("获取公钥")
    @GetMapping("publicKey")
    public Mono<PublicKey> getPublicKey() throws NoSuchAlgorithmException {
        return publicKeyHandler.getPublicKey();
    }

    @ApiOperation(value = "公钥加密", hidden = true)
    @PostMapping("publicKey/encrypt")
    public Mono<PublicRasEncryptResult> encrypt(@RequestBody @Valid PublicRasEncrypt encrypt)
      throws GeneralSecurityException, UnsupportedEncodingException {
        return publicKeyHandler.encrypt(encrypt);
    }
}
