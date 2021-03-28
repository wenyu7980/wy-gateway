package com.wenyu7980.gateway.swagger;

import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author wenyu
 */
@Primary
@Component
public class GatewaySwaggerProvider implements SwaggerResourcesProvider {

    public static final String API_URI = "/v3/api-docs";
    private final RouteLocator routeLocator;
    private final GatewayProperties gatewayProperties;

    public GatewaySwaggerProvider(RouteLocator routeLocator, GatewayProperties gatewayProperties) {
        this.routeLocator = routeLocator;
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        List<String> routes = new ArrayList<>();
        // 取出Spring Cloud Gateway中的route
        routeLocator.getRoutes().subscribe(route -> routes.add(route.getId()));
        // 结合路由配置，只获取有效的route节点
        for (RouteDefinition routeDefinition : gatewayProperties.getRoutes()) {
            if (routes.contains(routeDefinition.getId())) {
                resources.addAll(routeDefinition.getPredicates().stream()
                  .filter(predicateDefinition -> ("Path").equalsIgnoreCase(predicateDefinition.getName())).map(
                    predicateDefinition -> swaggerResource(routeDefinition.getId(),
                      predicateDefinition.getArgs().get(NameUtils.GENERATED_NAME_PREFIX + "0").replace("/**", API_URI)))
                  .collect(Collectors.toList()));
            }
        }
        return resources.stream().filter(swaggerResource -> !swaggerResource.getName().equals("aggregation"))
          .collect(Collectors.toList());
    }

    private SwaggerResource swaggerResource(String name, String location) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location.replace("/api/", "/"));
        swaggerResource.setSwaggerVersion("3.0");
        return swaggerResource;
    }
}
