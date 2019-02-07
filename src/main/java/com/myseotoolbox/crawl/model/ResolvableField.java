package com.myseotoolbox.crawl.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;


@Data
@AllArgsConstructor
public class ResolvableField<T> {
    private final T value;
    private final ObjectId reference;


    /**
     * Null value is an allowed value. We infer we have a value when reference is not set
     */
    public boolean isValueField() {
        return reference == null;
    }

    public static <T> ResolvableField<T> forValue(T value) {
        return new ResolvableField<>(value, null);
    }

    public static <T> ResolvableField<T> forReference(ObjectId reference) {
        return new ResolvableField<>(null, reference);
    }
}
