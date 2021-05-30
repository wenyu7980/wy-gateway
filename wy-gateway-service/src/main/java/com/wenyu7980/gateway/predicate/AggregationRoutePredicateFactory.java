package com.wenyu7980.gateway.predicate;

import com.wenyu7980.gateway.common.component.AggregationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

/**
 *
 * @author wenyu
 */
@Component
public class AggregationRoutePredicateFactory
  extends AbstractRoutePredicateFactory<AggregationRoutePredicateFactory.Config> {

    @Autowired
    private AggregationComponent aggregationComponent;

    public AggregationRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return serverWebExchange -> {
            ServerHttpRequest request = serverWebExchange.getRequest();
            return aggregationComponent.predicate(request);
        };
    }

    public static class Config {

    }

}
