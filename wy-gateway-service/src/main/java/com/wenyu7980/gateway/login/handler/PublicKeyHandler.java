package com.wenyu7980.gateway.login.handler;

import com.wenyu7980.gateway.login.domain.PublicKey;
import com.wenyu7980.gateway.login.domain.PublicRasEncrypt;
import com.wenyu7980.gateway.login.domain.PublicRasEncryptResult;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author wenyu
 */
public interface PublicKeyHandler {
    /**
     * 获取公钥
     * @return
     * @throws NoSuchAlgorithmException
     */
    Mono<PublicKey> getPublicKey() throws NoSuchAlgorithmException;

    /**
     * 公钥加密
     * @param encrypt
     * @return
     * @throws GeneralSecurityException
     * @throws UnsupportedEncodingException
     */
    Mono<PublicRasEncryptResult> encrypt(PublicRasEncrypt encrypt)
      throws GeneralSecurityException, UnsupportedEncodingException;
}
