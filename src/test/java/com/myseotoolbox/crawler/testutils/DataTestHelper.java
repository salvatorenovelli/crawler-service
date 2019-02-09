package com.myseotoolbox.crawler.testutils;

import org.springframework.data.repository.CrudRepository;

import java.util.Objects;
import java.util.stream.Stream;

public class DataTestHelper {
    public static void clearRepos(CrudRepository... repo) {
        Stream.of(repo).filter(Objects::nonNull).forEach(CrudRepository::deleteAll);
    }
}
