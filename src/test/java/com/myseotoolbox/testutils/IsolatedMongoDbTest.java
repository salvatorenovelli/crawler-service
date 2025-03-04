package com.myseotoolbox.testutils;


import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@DataMongoTest
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class IsolatedMongoDbTest {

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        String dbName = "IsolatedMongoDbTest-" + UUID.randomUUID();
        registry.add("spring.data.mongodb.database", () -> dbName);
    }
}
