package com.myseotoolbox.crawler.config;

import com.google.pubsub.v1.Topic;
import com.myseotoolbox.crawler.spider.configuration.PubSubProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.cloud.gcp.pubsub.PubSubAdmin;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class PubSubConnectionVerification {

    private final GcpProjectIdProvider projectIdProvider;
    private final PubSubAdmin pubSubAdmin;
    private final PubSubProperties pubSubProperties;

    @EventListener(ApplicationStartedEvent.class)
    public void verifyPubSubConnection() {
        try {
            log.info("Verifying pubsub connection. ProjectId: {}", projectIdProvider.getProjectId());
            Topic topic = CompletableFuture.supplyAsync(() -> pubSubAdmin.getTopic(pubSubProperties.getTopicName())).get(pubSubProperties.getConnectionTimeoutSeconds(), TimeUnit.SECONDS);
            log.info("Successfully verified connection to pubsub. Topic: {}", topic.getName());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("Exception while connecting to PubSub. This can be caused by bad permissions in credentials. Try to set DEBUG log level to io.grpc & com.google.api.client");
            throw new RuntimeException("Unable to connect to PubSub with timeout of " + pubSubProperties.getConnectionTimeoutSeconds() + " seconds", e);
        }
    }
}