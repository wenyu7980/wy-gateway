package com.wenyu7980.gateway.predicate;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wenyu7980.gateway.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author wenyu
 */
@Component
public class AggregationRoutePredicateFactory
  extends AbstractRoutePredicateFactory<AggregationRoutePredicateFactory.Config> {

    @Autowired
    private WebClient.Builder builder;
    private final Cache<String, List<Aggregation>> cache;
    private final static AntPathMatcher MATCHER = new AntPathMatcher();

    public AggregationRoutePredicateFactory() {
        super(Config.class);
        this.cache = Caffeine.newBuilder().build();
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return serverWebExchange -> {
            ServerHttpRequest request = serverWebExchange.getRequest();
            List<Aggregation> permissions = this.cache.get("AGGREGATIONS", key -> new ArrayList<>());
            String method = request.getMethodValue();
            String serviceName = RequestUtils.getServiceName(request);
            String path = RequestUtils.getPathWithoutService(request);
            boolean b = permissions.stream().anyMatch(
              v -> v.getMethod().equals(method) && v.getServiceName().equals(serviceName) && MATCHER
                .match(v.getPath(), path));
            return b;
        };
    }

    private List<Aggregation> getAggregations() {
        return this.builder.build().get().uri("lb://wy-aggregation/internal/aggregations").retrieve()
          .bodyToFlux(Aggregation.class).toStream().collect(Collectors.toList());
    }

    public static class Config {

    }

    @Scheduled(fixedDelay = 1000 * 30)
    private void refresh() {
        try {
            this.cache.put("AGGREGATIONS", this.getAggregations());
        } catch (Exception e) {
        }
    }

    public static class Aggregation {
        private String serviceName;
        private String method;
        private String path;

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
