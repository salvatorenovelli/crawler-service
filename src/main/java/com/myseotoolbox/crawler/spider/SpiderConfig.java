package com.myseotoolbox.crawler.spider;


import com.myseotoolbox.crawler.spider.configuration.ClockUtils;
import com.myseotoolbox.crawler.spider.ratelimiter.SystemClockUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpiderConfig {

    @Bean
    public ClockUtils getClockUtils() {
        return new SystemClockUtils();
    }
}
