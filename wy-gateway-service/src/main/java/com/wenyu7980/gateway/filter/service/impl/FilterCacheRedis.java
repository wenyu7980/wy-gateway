package com.wenyu7980.gateway.filter.service.impl;

import com.wenyu7980.authentication.api.domain.PermissionInternal;
import com.wenyu7980.common.converter.kryo.KryoConverterUtils;
import com.wenyu7980.gateway.filter.service.FilterCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 *
 * @author wenyu
 */
@Service
public class FilterCacheRedis implements FilterCacheService {
    private static final String KEY = "FILTER_CACHE_PERMISSIONS";
    private RedisTemplate<String, List<PermissionInternal>> redisTemplate;

    @Autowired
    public FilterCacheRedis(RedisConnectionFactory factory) {
        this.redisTemplate = new RedisTemplate<>();
        this.redisTemplate.setConnectionFactory(factory);
        this.redisTemplate.setValueSerializer(new RedisSerializer<List<PermissionInternal>>() {
            @Override
            public byte[] serialize(List<PermissionInternal> permissions) throws SerializationException {
                return KryoConverterUtils.write(new ArrayList<>(permissions));
            }

            @Override
            public List<PermissionInternal> deserialize(byte[] bytes) throws SerializationException {
                return KryoConverterUtils.read(bytes, ArrayList.class);
            }
        });
        this.redisTemplate.afterPropertiesSet();
    }

    public void save(List<PermissionInternal> permissions) {
        this.redisTemplate.opsForValue().set(KEY, permissions, 1, TimeUnit.MINUTES);
    }

    @Override
    public List<PermissionInternal> getPermissions(Supplier<List<PermissionInternal>> supplier) {
        List<PermissionInternal> permissions = this.redisTemplate.opsForValue().get(KEY);
        if (permissions == null) {
            permissions = supplier.get();
            this.save(permissions);
        }
        return permissions;
    }

}
