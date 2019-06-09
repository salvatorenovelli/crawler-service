package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.spider.configuration.RobotsTxtConfiguration;
import com.myseotoolbox.crawler.spider.robotstxt.DefaultRobotsTxt;
import com.myseotoolbox.crawler.spider.robotstxt.EmptyRobotsTxt;
import com.myseotoolbox.crawler.spider.robotstxt.RobotsTxt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class RobotsTxtFactory {

    public RobotsTxt buildRobotsTxtFor(RobotsTxtConfiguration configuration) {
        try {
            return new DefaultRobotsTxt(configuration.getOrigin());
        } catch (IOException e) {
            log.warn("Unable to download robots.txt for website {}. Exception: {}", configuration, e.toString());
            return EmptyRobotsTxt.instance();
        }
    }
}
