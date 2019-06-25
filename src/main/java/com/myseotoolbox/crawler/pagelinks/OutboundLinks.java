package com.myseotoolbox.crawler.pagelinks;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document
public class OutboundLinks {
    @Id private final String id;
    @Indexed private final ObjectId crawlId;
    @Indexed private final String url;
    private final Map<LinkType, List<String>> linksByType;
}
