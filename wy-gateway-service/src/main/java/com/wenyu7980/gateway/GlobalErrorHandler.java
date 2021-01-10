package com.wenyu7980.gateway;

import com.wenyu7980.common.exceptions.AbstractException;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author wenyu
 */
@Component
public class GlobalErrorHandler extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        Throwable error = getError(request);
        if (error instanceof AbstractException) {
            errorAttributes.put("timestamp", new Date());
            errorAttributes.put("path", request.path());
            errorAttributes.put("status", ((AbstractException) error).getStatus());
            errorAttributes.put("code", ((AbstractException) error).getCode());
            errorAttributes.put("message", error.getMessage());
            return errorAttributes;
        }
        return super.getErrorAttributes(request, includeStackTrace);
    }
}
