package com.wenyu7980.gateway.handler;

import com.wenyu7980.common.exceptions.AbstractException;
import com.wenyu7980.common.exceptions.ErrorResponseBody;
import com.wenyu7980.common.gson.adapter.GsonUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 *
 * @author wenyu
 */
@Component
public class GlobalErrorHandler implements WebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        ErrorResponseBody body = new ErrorResponseBody();
        // header set
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (ex instanceof AbstractException) {
            AbstractException exception = (AbstractException) ex;
            response.setStatusCode(HttpStatus.resolve(exception.getStatus()));
            body.setCodeMessage(exception.getCode(), exception.getMessage());
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            body.setCodeMessage(9, ex.getMessage());
        }
        return response.writeWith(Mono.fromSupplier(
          () -> response.bufferFactory().wrap(GsonUtil.gson().toJson(body).getBytes(StandardCharsets.UTF_8))));
    }
}
