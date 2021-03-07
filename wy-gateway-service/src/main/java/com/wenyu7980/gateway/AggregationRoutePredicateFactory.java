package com.wenyu7980.gateway;

import com.wenyu7980.aggregation.api.domain.AggregationItem;
import com.wenyu7980.aggregation.api.service.AggregationInternalService;
import com.wenyu7980.authentication.api.domain.PermissionInternal;
import com.wenyu7980.common.converter.kryo.KryoConverterUtils;
import com.wenyu7980.gateway.filter.component.FilterComponent;
import com.wenyu7980.gateway.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 *
 * @author wenyu
 */
@Component
public class AggregationRoutePredicateFactory
  extends AbstractRoutePredicateFactory<AggregationRoutePredicateFactory.Config> {

    @Autowired
    private AggregationInternalService aggregationInternalService;
    @Autowired
    private FilterComponent filterComponent;
    private RedisTemplate<String, List<AggregationItem>> redisTemplate;
    private static final String KEY = "AggregationItems";

    @Autowired
    public AggregationRoutePredicateFactory(RedisConnectionFactory factory) {
        super(Config.class);
        this.redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        this.redisTemplate.setValueSerializer(new RedisSerializer<List<AggregationItem>>() {
            @Override
            public byte[] serialize(List<AggregationItem> permissions) throws SerializationException {
                return KryoConverterUtils.write(new ArrayList<>(permissions));
            }

            @Override
            public List<AggregationItem> deserialize(byte[] bytes) throws SerializationException {
                return KryoConverterUtils.read(bytes, ArrayList.class);
            }
        });
        this.redisTemplate.afterPropertiesSet();
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return serverWebExchange -> {
            ServerHttpRequest request = serverWebExchange.getRequest();
            Optional<PermissionInternal> optional = filterComponent
              .getPermission(RequestUtils.getServiceName(request), RequestUtils.getMethod(request),
                RequestUtils.getPathWithoutService(request));
            if (!optional.isPresent()) {
                return false;
            }
            PermissionInternal permission = optional.get();
            List<AggregationItem> items = redisTemplate.opsForValue().get(KEY);
            if (items == null) {
                synchronized (this) {
                    items = redisTemplate.opsForValue().get(KEY);
                    if (items == null) {
                        items = new ArrayList<>(aggregationInternalService.getItem());
                        redisTemplate.opsForValue().set(KEY, items, 2, TimeUnit.MINUTES);
                    }
                }
            }
            return items.stream().anyMatch(
              v -> v.getMethod().equals(permission.getMethod()) && v.getPath().equals(permission.getPath()) && v
                .getServiceName().equals(permission.getServiceName()));
        };
    }

    public static class Config {

    }
}
