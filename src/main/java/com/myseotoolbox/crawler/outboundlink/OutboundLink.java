package com.myseotoolbox.crawler.outboundlink;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

@Data
public class OutboundLink {
    @Id private final String id;
    @Indexed private final String crawlId;
    @Indexed private final String url;
    private final List<Link> links;
}
