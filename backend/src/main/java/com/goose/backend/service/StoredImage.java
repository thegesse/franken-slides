package com.goose.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record StoredImage(Resource resource, MediaType contentType) {
}
