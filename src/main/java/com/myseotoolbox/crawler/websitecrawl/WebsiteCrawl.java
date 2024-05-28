package com.myseotoolbox.crawler.websitecrawl;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;


@Data
public class WebsiteCrawl {
    @JsonSerialize(using = ToStringSerializer.class)
    private final ObjectId id;
    private final String origin;
    private final String owner;
    private final Instant startedAt;
    private final Collection<String> seeds;


    WebsiteCrawl(ObjectId id, String owner, String origin, Instant startedAt, Collection<String> seeds) {
        this.id = id;
        this.owner = owner;
        this.origin = origin;
        this.startedAt = startedAt;
        this.seeds = seeds;
    }

    @Override
    public String toString() {
        return "WebsiteCrawl{" +
                "id=" + id +
                ", origin='" + origin + '\'' +
                ", owner='" + owner + '\'' +
                ", startedAt=" + startedAt +
                ", seeds(size)=" + seeds.size() +
                '}';
    }
}
