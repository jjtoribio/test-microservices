package es.microservices.tests.orders.configurations;

import java.util.Map;
import java.util.Objects;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.ServerRequest;
import brave.Span;
import brave.Tracer;

@Configuration
public class ErrorManagementConfig {

    @Bean
    ErrorAttributes gatewayErrorAttributes(final Tracer tracer) {
        return new GatewayErrorAttributes(tracer);
    }

    public static class GatewayErrorAttributes extends DefaultErrorAttributes {

        private final Tracer tracer;

        public GatewayErrorAttributes(final Tracer tracer) {
            super();
            Assert.notNull(tracer, "\"tracer\" must not be null");
            this.tracer = tracer;
        }

        @Override
        public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
            Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
            errorAttributes.put("errorMessage", errorAttributes.get("message"));
            errorAttributes.put("operationId", this.getTraceId());            

            // I don't like this fields (;p)
            errorAttributes.remove("timestamp");
            errorAttributes.remove("path");
            errorAttributes.remove("error");
            errorAttributes.remove("message");            
            errorAttributes.remove("requestId");            

            return errorAttributes;
        }

        private String getTraceId() {
            Span currentSpan = tracer.currentSpan();
            if (Objects.isNull(tracer.currentSpan())) {
                currentSpan = tracer.nextSpan().name("test").start();
            }
            return currentSpan.context().traceIdString();

        }
    }
}

