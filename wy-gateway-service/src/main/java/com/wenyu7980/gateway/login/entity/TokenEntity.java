package com.wenyu7980.gateway.login.entity;

/**
 *
 * @author wenyu
 */
public class TokenEntity {
    private String token;
    private String random;
    private String userId;
    private Long timeout;

    protected TokenEntity() {
    }

    public TokenEntity(String token, String random, String userId, Long timeout) {
        this.token = token;
        this.random = random;
        this.userId = userId;
        this.timeout = timeout;
    }

    public String getToken() {
        return token;
    }

    public String getRandom() {
        return random;
    }

    public String getUserId() {
        return userId;
    }

    public Long getTimeout() {
        return timeout;
    }
}
