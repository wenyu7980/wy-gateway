package com.wenyu7980.gateway.login.domain;

import javax.validation.constraints.NotEmpty;

/**
 *
 * @author wenyu
 */
public class PublicRasEncrypt {
    @NotEmpty
    public String publicKey;
    @NotEmpty
    private String data;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
