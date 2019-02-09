package com.myseotoolbox.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    private String destinationUri;
    private String title;
    private List<String> metaDescriptions;
    private List<String> h1s;
    private String error;
}
