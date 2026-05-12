package com.internship.tool.service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public final class ChangedFieldsUtil {

    private static final ObjectMapper MAPPER =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private ChangedFieldsUtil() {}

    // Builds JSON: { "field": { "before": X, "after": Y } }
    public static String diff(
            Map<String, Object[]> changes) {
        try {
            Map<String, Map<String, Object>> result =
                new LinkedHashMap<>();
            changes.forEach((field, values) -> {
                Map<String, Object> change =
                    new LinkedHashMap<>();
                change.put("before", values[0]);
                change.put("after",  values[1]);
                result.put(field, change);
            });
            return MAPPER.writeValueAsString(result);
        } catch (Exception e) {
            log.warn("Could not serialize diff: {}",
                     e.getMessage());
            return "{}";
        }
    }

    // Simple full snapshot of any object
    public static String snapshot(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Could not serialize snapshot: {}",
                     e.getMessage());
            return "{}";
        }
    }
}