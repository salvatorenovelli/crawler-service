package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.spider.robotstxt.DefaultRobotsTxt;
import com.myseotoolbox.crawler.spider.robotstxt.EmptyRobotsTxt;
import com.myseotoolbox.crawler.spider.robotstxt.RobotsTxt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

@Slf4j
@Component
public class RobotsTxtFactory {

    public RobotsTxt buildRobotsTxtFor(URI websiteOrigin) {
        try {
            return new DefaultRobotsTxt(websiteOrigin);
        } catch (IOException e) {
            log.warn("Unable to download robots.txt for website {}. Exception: {}", websiteOrigin, e.toString());
            return EmptyRobotsTxt.instance();
        }
    }
}
