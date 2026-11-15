package com.goose.backend.service;

import com.goose.backend.dto.ImageResponse;
import com.goose.backend.model.Image;
import com.goose.backend.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final Path uploadDir;

    private static final Map<String, String> IMAGE_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private static final Map<String, MediaType> MEDIA_TYPES = Map.of(
            ".jpg", MediaType.IMAGE_JPEG,
            ".png", MediaType.IMAGE_PNG,
            ".webp", MediaType.parseMediaType("image/webp")
    );

    public ImageService(ImageRepository imageRepository, @Value("${app.upload-dir:../uploads}") String uploadDir) {
        this.imageRepository = imageRepository;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public ImageResponse addImage(String alt, MultipartFile file) {
        if(file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        String contentType = file.getContentType();
        String extension = contentType == null ? null : IMAGE_TYPES.get(contentType);
        if(extension == null) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Use a JPEG, PNG or WebP image");
        }
        String storageKey = UUID.randomUUID() + extension;
        Path destination = uploadDir.resolve(storageKey).normalize();

        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), destination);

            Image image = new Image();
            image.setAlt(alt);
            image.setImagePath(storageKey);
            Image saved = imageRepository.save(image);
            return new ImageResponse(saved.getId(), saved.getAlt(), saved.getImagePath());
        } catch (IOException e) {
            try {
                Files.deleteIfExists(destination);
            }catch (IOException cleaneupException) {
                e.addSuppressed(cleaneupException);
            }
            throw new UncheckedIOException("Could not save image", e);
        } catch (RuntimeException e ) {
            try {
                Files.deleteIfExists(destination);
            } catch (IOException cleanupException) {
                e.addSuppressed(cleanupException);
            }
            throw e;
        }
    }
    public StoredImage loadImage(Image image) {
        if (image == null || image.getImagePath() == null || image.getImagePath().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        }

        Path filePath = uploadDir.resolve(image.getImagePath()).normalize();
        if (!filePath.startsWith(uploadDir) || !Files.isRegularFile(filePath) || !Files.isReadable(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image file not found");
        }

        String fileName = filePath.getFileName().toString().toLowerCase(Locale.ROOT);
        MediaType contentType = MEDIA_TYPES.entrySet().stream()
                .filter(entry -> fileName.endsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        Resource resource = new FileSystemResource(filePath);
        return new StoredImage(resource, contentType);
    }

    public void deleteImageFile(Image image) {
        if (image == null || image.getImagePath() == null || image.getImagePath().isBlank()) {
            return;
        }

        Path filePath = uploadDir.resolve(image.getImagePath()).normalize();
        if (!filePath.startsWith(uploadDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image storage path");
        }
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not delete image file", exception);
        }
    }
}
