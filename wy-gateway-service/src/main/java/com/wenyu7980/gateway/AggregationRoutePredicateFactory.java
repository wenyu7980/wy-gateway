package com.wenyu7980.gateway;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
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

    public AggregationRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return serverWebExchange -> {
            return true;
        };
    }

    public static class Config {

    }
}
