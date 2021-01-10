package com.wenyu7980.gateway.login.handler.impl;

import com.wenyu7980.gateway.login.domain.PublicKey;
import com.wenyu7980.gateway.login.domain.PublicRasEncrypt;
import com.wenyu7980.gateway.login.domain.PublicRasEncryptResult;
import com.wenyu7980.gateway.login.handler.PublicKeyHandler;
import com.wenyu7980.gateway.login.service.RsaKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 *
 * @author wenyu
 */
@Component
public class PublicKeyHandlerImpl implements PublicKeyHandler {
    @Autowired
    private RsaKeyService rsaKeyService;

    @Override
    public Mono<PublicKey> getPublicKey() throws NoSuchAlgorithmException {
        String uuid = UUID.randomUUID().toString();
        PublicKey key = new PublicKey();
        key.setPublicKey(rsaKeyService.getPublicKey(uuid));
        key.setPublicKeyCode(uuid);
        return Mono.just(key);
    }

    @Override
    public Mono<PublicRasEncryptResult> encrypt(PublicRasEncrypt encrypt)
      throws GeneralSecurityException, UnsupportedEncodingException {
        PublicRasEncryptResult rasEncryptResult = new PublicRasEncryptResult();
        rasEncryptResult.setData(rsaKeyService.encode(encrypt.getPublicKey(), encrypt.getData()));
        return Mono.just(rasEncryptResult);
    }
}
