package com.wenyu7980.gateway.configuration;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.cloud.gateway.discovery.GatewayDiscoveryClientAutoConfiguration.initFilters;
import static org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory.PATTERN_KEY;
import static org.springframework.cloud.gateway.support.NameUtils.normalizeRoutePredicateName;

@Configuration
@AutoConfigureBefore(GatewayAutoConfiguration.class)
@ConditionalOnProperty(prefix = "spring.cloud.gateway.discovery.locator", name = "enabled", havingValue = "true")
public class GatewayLocatorConfiguration {
    @Primary
    @Bean
    public DiscoveryLocatorProperties discoveryLocatorPropertiesPrimary() {
        DiscoveryLocatorProperties properties = new DiscoveryLocatorProperties();
        properties.setPredicates(initPredicates());
        properties.setFilters(initFilters());
        return properties;
    }

    public static List<PredicateDefinition> initPredicates() {
        ArrayList<PredicateDefinition> definitions = new ArrayList<>();
        PredicateDefinition predicate = new PredicateDefinition();
        predicate.setName(normalizeRoutePredicateName(PathRoutePredicateFactory.class));
        predicate.addArg(PATTERN_KEY, "'/api/'+serviceId+'/**'");
        definitions.add(predicate);
        return definitions;
    }
}
