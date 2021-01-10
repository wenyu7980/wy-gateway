package com.wenyu7980.gateway.login.service.impl;

import com.wenyu7980.gateway.login.service.RsaKeyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author wenyu
 */
@Configuration
public class RsaKeyServiceDevConfig {
    @Bean
    @ConditionalOnMissingBean
    public RsaKeyService rsaKeyService() {
        return new RsaKeyService() {
            @Override
            public String getPublicKey(String code) throws NoSuchAlgorithmException {
                return "";
            }

            @Override
            public String decode(String code, String data) throws IOException, GeneralSecurityException {
                return data;
            }

            @Override
            public String encode(String publicKey, String data)
              throws GeneralSecurityException, UnsupportedEncodingException {
                return data;
            }
        };
    }
}
