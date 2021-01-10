package com.wenyu7980.gateway.login.domain;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;

/**
 *
 * @author wenyu
 */
public class Login {
    @ApiModelProperty(value = "用户名", required = true)
    @NotEmpty
    private String username;
    @ApiModelProperty(value = "密码,使用publicKey加密", required = true)
    @NotEmpty
    private String password;
    @ApiModelProperty(value = "公钥code", required = true)
    @NotEmpty
    private String publicKeyCode;
    @ApiModelProperty(value = "随机数,使用publicKey加密", required = true)
    @NotEmpty
    private String randomCode;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPublicKeyCode() {
        return publicKeyCode;
    }

    public void setPublicKeyCode(String publicKeyCode) {
        this.publicKeyCode = publicKeyCode;
    }

    public String getRandomCode() {
        return randomCode;
    }

    public void setRandomCode(String randomCode) {
        this.randomCode = randomCode;
    }
}
