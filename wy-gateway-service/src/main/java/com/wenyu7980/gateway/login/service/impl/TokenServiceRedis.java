package com.wenyu7980.gateway.login.service.impl;

import com.wenyu7980.common.converter.kryo.KryoConverterUtils;
import com.wenyu7980.gateway.login.entity.TokenEntity;
import com.wenyu7980.gateway.login.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author wenyu
 */
@Service
public class TokenServiceRedis implements TokenService {
    private final RedisTemplate<String, TokenEntity> redisTemplate;

    @Autowired
    public TokenServiceRedis(RedisConnectionFactory factory) {
        this.redisTemplate = new RedisTemplate<>();
        this.redisTemplate.setConnectionFactory(factory);
        this.redisTemplate.setKeySerializer(new StringRedisSerializer());
        this.redisTemplate.setValueSerializer(new RedisSerializer<TokenEntity>() {
            @Override
            public byte[] serialize(TokenEntity tokenEntity) throws SerializationException {
                return KryoConverterUtils.write(tokenEntity);
            }

            @Override
            public TokenEntity deserialize(byte[] bytes) throws SerializationException {
                return KryoConverterUtils.read(bytes, TokenEntity.class);
            }
        });
        this.redisTemplate.afterPropertiesSet();
    }

    @Override
    public void save(TokenEntity tokenEntity) {
        redisTemplate.opsForValue()
          .set(tokenEntity.getToken(), tokenEntity, tokenEntity.getTimeout(), TimeUnit.SECONDS);
    }

    @Override
    public Optional<TokenEntity> findOptionalById(String token) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(token));
    }
}
