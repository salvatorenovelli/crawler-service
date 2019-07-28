package com.myseotoolbox.crawler.config;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;


@Slf4j
@Component
@Profile("!test")
public class ArchiveConnectionVerification {


    private final RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
            .handle(RestClientException.class)
            .withDelay(Duration.ofSeconds(30))
            .onFailedAttempt(e -> log.info("Failed attempt to connect to archive service: {}", e.getLastFailure().getMessage()))
            .withMaxRetries(3);

    private final RestTemplate restTemplate;
    private final String healthCheckUrl;

    public ArchiveConnectionVerification(RestTemplate restTemplate, @Value("${archive.host}") String host, @Value("${archive.healthCheckUrl}") String url) {
        this.restTemplate = restTemplate;
        this.healthCheckUrl = host + url;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void verify() {
        log.info("Checking Archive service health status...");
        HttpStatus httpStatus = Failsafe.with(retryPolicy).get(this::probeHealthCheck);
        Assert.isTrue(httpStatus == HttpStatus.OK, "Status should be 200");
        log.info("Connection to Archive successful");
    }

    private HttpStatus probeHealthCheck() {
        return restTemplate.getForEntity(healthCheckUrl, HealthCheckResponse.class).getStatusCode();
    }

    @Data
    @NoArgsConstructor
    private static class HealthCheckResponse {
        private String status;
    }
}
