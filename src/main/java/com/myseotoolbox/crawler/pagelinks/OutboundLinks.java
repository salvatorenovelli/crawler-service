package com.myseotoolbox.crawler.pagelinks;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document@AllArgsConstructor
public class OutboundLinks {
    @Id private String id;
    @Indexed private final ObjectId crawlId;
    private final String url;
    private final LocalDateTime crawledAt;
    @Indexed private final String domain;
    private final Map<LinkType, List<String>> linksByType;
}
