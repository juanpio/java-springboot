package com.microservices.common.logging.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.microservices.common.logging.constants.LoggingConstants.DEFAULT_SENSITIVE_FIELDS;

/**
 * Utility to filter sensitive data from log messages.
 */
public class SensitiveDataFilter {
    
    private static final String REDACTED = "***REDACTED***";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final Set<String> sensitiveFields;
    
    public SensitiveDataFilter(List<String> customSensitiveFields) {
        this.sensitiveFields = Arrays.stream(DEFAULT_SENSITIVE_FIELDS)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        
        if (customSensitiveFields != null) {
            customSensitiveFields.stream()
                .map(String::toLowerCase)
                .forEach(sensitiveFields::add);
        }
    }
    
    /**
     * Filter sensitive data from a JSON string.
     */
    public String filterJson(String json) {
        if (json == null || json.isBlank()) {
            return json;
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode filteredNode = filterNode(rootNode);
            return objectMapper.writeValueAsString(filteredNode);
        } catch (Exception e) {
            // If parsing fails, apply simple text filtering
            return filterText(json);
        }
    }
    
    /**
     * Filter sensitive data from plain text using simple pattern matching.
     */
    public String filterText(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        
        String filtered = text;
        for (String field : sensitiveFields) {
            // Match patterns like: "password":"value" or password=value
            filtered = filtered.replaceAll(
                "(?i)(['\"]?" + field + "['\"]?\\s*[:=]\\s*['\"]?)[^'\"\\s,}]+",
                "$1" + REDACTED
            );
        }
        return filtered;
    }
    
    /**
     * Recursively filter sensitive fields from JSON node.
     */
    private JsonNode filterNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            ObjectNode filtered = objectMapper.createObjectNode();
            
            objectNode.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode value = entry.getValue();
                
                if (isSensitiveField(fieldName)) {
                    filtered.put(fieldName, REDACTED);
                } else if (value.isContainerNode()) {
                    filtered.set(fieldName, filterNode(value));
                } else {
                    filtered.set(fieldName, value);
                }
            });
            
            return filtered;
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            ArrayNode filtered = objectMapper.createArrayNode();
            
            arrayNode.forEach(item -> filtered.add(filterNode(item)));
            
            return filtered;
        }
        
        return node;
    }
    
    private boolean isSensitiveField(String fieldName) {
        return sensitiveFields.contains(fieldName.toLowerCase());
    }
}
