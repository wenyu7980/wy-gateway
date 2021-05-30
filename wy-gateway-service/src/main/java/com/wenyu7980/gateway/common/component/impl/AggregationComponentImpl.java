package com.wenyu7980.gateway.common.component.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wenyu7980.authentication.api.domain.Permission;
import com.wenyu7980.gateway.common.component.AggregationComponent;
import com.wenyu7980.gateway.common.component.PermissionComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AggregationComponentImpl implements AggregationComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregationComponent.class);
    private final Cache<String, List<Aggregation>> cache;
    @Autowired
    private WebClient.Builder builder;
    @Autowired
    private PermissionComponent permissionComponent;

    public AggregationComponentImpl() {
        this.cache = Caffeine.newBuilder().build();
    }

    @Override
    public boolean predicate(ServerHttpRequest request) {
        Optional<Permission> optional = permissionComponent.getPermissionFromRequest(request);
        if (optional.isPresent()) {
            final Permission permission = optional.get();
            List<Aggregation> aggregations = this.cache.get("AGGREGATIONS", key -> new ArrayList<>());
            return aggregations.stream().anyMatch(a -> Objects.equals(a.getMethod(), permission.getMethod()) && Objects
              .equals(a.getServiceName(), permission.getMethod()) && Objects.equals(a.getPath(), permission.getPath()));
        }
        return false;
    }

    private List<Aggregation> getAggregations() {
        try {
            return this.builder.build().get().uri("lb://wy-aggregation/internal/aggregations").retrieve()
              .bodyToFlux(Aggregation.class).toStream().collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
            return new ArrayList<>();
        }
    }

    @Scheduled(fixedDelay = 1000 * 30)
    private void refresh() {
        try {
            this.cache.put("AGGREGATIONS", this.getAggregations());
        } catch (Exception e) {
        }
    }

    private static class Aggregation {
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
