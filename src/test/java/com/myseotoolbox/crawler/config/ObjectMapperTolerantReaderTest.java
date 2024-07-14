package com.myseotoolbox.crawler.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ObjectMapperTolerantReaderTest {

    @Autowired ObjectMapper objectMapper;

    @Test
    public void shouldImplementTolerantReader() throws Exception {
        String jsonWithUnknownField = "{ \"knownField\": \"value\", \"unknownField\": \"extraValue\" }";

        MyEvent event = objectMapper.readValue(jsonWithUnknownField, MyEvent.class);

        assertNotNull(event);
        assertEquals("value", event.getKnownField());
    }


    public static class MyEvent {
        private String knownField;

        public String getKnownField() {
            return knownField;
        }

        public void setKnownField(String knownField) {
            this.knownField = knownField;
        }
    }

}