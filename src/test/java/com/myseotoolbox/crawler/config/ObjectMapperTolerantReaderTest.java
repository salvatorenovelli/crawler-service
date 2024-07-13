package com.myseotoolbox.crawler.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class ObjectMapperTolerantReaderTest {
    @Test
    public void shouldNotFailIfNewFieldsAreAdded() throws Exception {
        String jsonWithUnknownField = "{ \"knownField\": \"value\", \"unknownField\": \"extraValue\" }";

        ObjectMapper objectMapper = new AppConfig().getObjectMapper();

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