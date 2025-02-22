package com.myseotoolbox.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor@NoArgsConstructor
public class InboundLinkCounts {
    private Integer ahref;
    private Integer sitemap;
    private Integer redirect;
}
