package com.myseotoolbox.crawler.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.net.URI;
import java.util.Date;
import java.util.List;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.extractHostAndPort;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageCrawl {

    @Id private ObjectId id;
    @Indexed private String uri;
    @Indexed private String host;
    private LastCrawl lastCrawl;
    private Date createDate;
    private ResolvableField<List<RedirectChainElement>> redirectChainElements;
    private ResolvableField<String> title;
    private ResolvableField<List<String>> metaDescriptions;
    private ResolvableField<List<String>> h1s;
    private ResolvableField<List<String>> h2s;
    private ResolvableField<List<String>> canonicals;

    private String crawlStatus;

    public PageCrawl(String uri, Date createDate) {
        this.uri = uri;
        this.host = extractHostAndPort(URI.create(uri));
        this.createDate = createDate;
    }
}
