package com.myseotoolbox.crawler.websitecrawl;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;


@Data
@Builder
public class WebsiteCrawl {
    @JsonSerialize(using = ToStringSerializer.class)
    private final ObjectId id;
    private final String origin;
    private final CrawlTrigger trigger;
    private final String owner;
    private final Instant startedAt;
    private final Collection<String> seeds;

    WebsiteCrawl(ObjectId id, String owner, CrawlTrigger trigger, String origin, Instant startedAt, Collection<String> seeds) {
        this.id = id;
        this.owner = owner;
        this.trigger = trigger;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WebsiteCrawl that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
