package com.wenyu7980.gateway.common.component;

import org.springframework.http.server.reactive.ServerHttpRequest;

public interface AggregationComponent {
    /**
     * 判定
     * @param request
     * @return
     */
    boolean predicate(ServerHttpRequest request);
}
