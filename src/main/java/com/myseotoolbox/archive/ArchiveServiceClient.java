package com.myseotoolbox.archive;

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

    public ArchiveServiceClient(RestTemplate restTemplate, @Value("${archive.host}") String host, @Value("${archive.serviceUrl}") String url) {
        this.restTemplate = restTemplate;
        this.archiveServiceUrl = host + url;
    }

    public Optional<PageSnapshot> getLastPageSnapshot(String uri) {
        try {
            PageSnapshot getCurrentValue = restTemplate.getForObject(archiveServiceUrl, PageSnapshot.class, uri);
            return Optional.of(Objects.requireNonNull(getCurrentValue));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            } else throw e;
        }
    }

}
