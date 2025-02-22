package com.myseotoolbox.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data@NoArgsConstructor
public class InboundLinksCount {
    InboundLinkCounts internal;
    InboundLinkCounts external;
}