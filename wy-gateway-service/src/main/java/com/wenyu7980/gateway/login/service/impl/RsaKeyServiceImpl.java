package com.wenyu7980.gateway.login.service.impl;

import com.wenyu7980.common.exceptions.code403.LoginFailException;
import com.wenyu7980.gateway.login.service.RsaKeyService;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author wenyu
 */
@Component
@ConditionalOnProperty(prefix = "application.rsa", name = "open", havingValue = "true")
public class RsaKeyServiceImpl implements RsaKeyService {
    public static final String RSA_ALGORITHM = "RSA";
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Value("${application.rsa.timeout:10}")
    private Integer timeout;

    @Override
    public String getPublicKey(String code) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        KeyPair keyPair = generator.generateKeyPair();
        redisTemplate.opsForValue()
          .set(code, Base64.encodeBase64String(keyPair.getPrivate().getEncoded()), timeout, TimeUnit.MINUTES);
        return Base64.encodeBase64String(keyPair.getPublic().getEncoded());
    }

    @Override
    public String decode(String code, String data) throws IOException, GeneralSecurityException {
        byte[] inputByte = Base64.decodeBase64(data.getBytes("UTF-8"));
        String privateKey = redisTemplate.opsForValue().get(code);
        if (privateKey == null) {
            throw new LoginFailException("登录失败");
        }
        byte[] bytes = Base64.decodeBase64(privateKey);
        PrivateKey rsa = KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(bytes));
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, rsa);
        return new String(cipher.doFinal(inputByte));
    }

    @Override
    public String encode(String publicKey, String data) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher encryptCipher = Cipher.getInstance(RSA_ALGORITHM);
        byte[] bytes = Base64.decodeBase64(publicKey);
        PublicKey key = KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(new X509EncodedKeySpec(bytes));
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherText = encryptCipher.doFinal(data.getBytes("UTF-8"));
        return Base64.encodeBase64String(cipherText);
    }
}
