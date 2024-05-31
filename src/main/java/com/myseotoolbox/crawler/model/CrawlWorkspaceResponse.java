package com.myseotoolbox.crawler.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class CrawlWorkspaceResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    private final ObjectId crawlId;
}
