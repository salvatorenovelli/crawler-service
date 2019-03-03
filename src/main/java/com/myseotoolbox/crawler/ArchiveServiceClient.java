package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.model.PageSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.Optional;


@Component
public class ArchiveServiceClient {

    private final RestTemplate restTemplate;
    private final String archiveServiceUrl;

    public ArchiveServiceClient(RestTemplate restTemplate, @Value("${archive.serviceUrl}") String archiveServiceUrl) {
        this.restTemplate = restTemplate;
        this.archiveServiceUrl = archiveServiceUrl;
    }

    public Optional<PageSnapshot> getLastPageSnapshot(String uri) {
        try {
            return Optional.of(Objects.requireNonNull(restTemplate.getForObject(archiveServiceUrl, PageSnapshot.class, uri)));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            } else throw e;
        }
    }

}
