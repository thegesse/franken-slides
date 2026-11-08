package com.goose.backend.mapper;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class SlideContentJsonConverter {
    private final JsonMapper jsonMapper;

    public SlideContentJsonConverter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public String serialize(JsonNode content) {
        if (content == null || content.isNull() || (content.isObject() && content.isEmpty())) {
            content = jsonMapper.createObjectNode().put("text", "empty");
        }
        if (!content.isObject()) {
            throw new IllegalArgumentException("Slide content must be a JSON object");
        }

        try {
            return jsonMapper.writeValueAsString(content);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Slide content could not be serialized", exception);
        }
    }

    public JsonNode parse(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        try {
            return jsonMapper.readTree(content);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Stored slide content is not valid JSON", exception);
        }
    }
}
