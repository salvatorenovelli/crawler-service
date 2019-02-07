package com.myseotoolbox.crawl.repository;

public class CommonMongoFilters {
    public static final String PRINCIPAL_FILTER = "{ 'ownerName' : ?#{principal?.username} }";
    public static final String ID_FILTER = "{ 'id' : :#{#id} }";
}
