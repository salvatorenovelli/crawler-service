package com.myseotoolbox.crawler.debug;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;

@Slf4j
public class RequestResponseDebugLoggingFilter implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "\"password\"\\s*:\\s*\"(.*?)\"|password=([^&]*)",
            Pattern.CASE_INSENSITIVE
    );

    public RequestResponseDebugLoggingFilter() {
        log.warn("Logging filter is initialised. This might log CONFIDENTIAL data! " +
                "If this is not meant to be logging please remove `request-response-debug` from active spring profiles");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

        chain.doFilter(wrappedRequest, wrappedResponse);

        logRequestDetails(wrappedRequest);
        logResponseDetails(wrappedResponse);

        wrappedResponse.copyBodyToResponse();
    }

    private void logRequestDetails(ContentCachingRequestWrapper request) throws JsonProcessingException {
        SerializableRequest serializableRequest = SerializableRequest.from(request);
        log.info("\nRequest: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(serializableRequest));
    }

    private void logResponseDetails(ContentCachingResponseWrapper response) throws IOException {
        SerializableResponse serializableResponse = SerializableResponse.from(response);
        log.info("\nResponse: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(serializableResponse));
    }

    @Data
    @AllArgsConstructor
    public static class SerializableRequest {
        private String uri;
        private String method;
        private Map<String, List<String>> headers;
        private String body;

        public static SerializableRequest from(ContentCachingRequestWrapper request) {
            Map<String, List<String>> headers = Collections.list(request.getHeaderNames()).stream()
                    .collect(toMap(
                            headerName -> headerName,
                            headerName -> Collections.list(request.getHeaders(headerName)),
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));
            String body = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
            return new SerializableRequest(request.getRequestURI(), request.getMethod(), headers, redactSensitiveData(body));
        }

        private static String redactSensitiveData(String body) {
            return PASSWORD_PATTERN.matcher(body).replaceAll("password=[REDACTED]");
        }
    }

    @Data
    @AllArgsConstructor
    public static class SerializableResponse {
        private int status;
        private Map<String, List<String>> headers;
        private String body;

        public static SerializableResponse from(ContentCachingResponseWrapper response) {
            Map<String, List<String>> headers = response.getHeaderNames().stream()
                    .collect(toMap(
                            headerName -> headerName,
                            headerName -> Arrays.asList(response.getHeaders(headerName).toArray(new String[0])),
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));
            String body = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
            return new SerializableResponse(response.getStatus(), headers, body);
        }
    }
}
