package com.myseotoolbox.crawler.debug;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.servlet.Filter;

@Slf4j
@Configuration
@Profile("request-response-debug")
public class RequestLoggingFilterConfig {
    @Bean
    public Filter logFilter() {
        log.warn("Logging filter is initialised. This might log CONFIDENTIAL data! " +
                "If this is not meant to be logging please remove `request-response-debug` from active spring profiles");

        return new RequestResponseDebugLoggingFilter();
    }
}