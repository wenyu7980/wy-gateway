package com.wenyu7980.gateway.login.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author wenyu
 */
public interface RsaKeyService {
    /**
     * 获取公钥
     * @param code
     * @return
     * @throws NoSuchAlgorithmException
     */
    String getPublicKey(String code) throws NoSuchAlgorithmException;

    /**
     * 私钥解密
     * @param code
     * @param data
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    String decode(String code, String data) throws IOException, GeneralSecurityException;

    /**
     * 加密
     * @param publicKey
     * @param data
     * @return
     * @throws UnsupportedEncodingException
     * @throws GeneralSecurityException
     */
    String encode(String publicKey, String data) throws GeneralSecurityException, UnsupportedEncodingException;
}
