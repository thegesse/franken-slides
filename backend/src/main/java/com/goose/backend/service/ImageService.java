package com.goose.backend.service;

import com.goose.backend.dto.ImageResponse;
import com.goose.backend.model.Image;
import com.goose.backend.repository.ImageRepository;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    public ImageService(ImageRepository imageRepository, @Value("${app.upload-dir:../uploads}") String uploadDir) {
        this.imageRepository = imageRepository;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public ImageResponse addImage(String alt, MultipartFile file) {
        if(file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        String extension = IMAGE_TYPES.get(file.getContentType());
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
    public ImageResponse loadImage(Image image, String imagePath) {
        if (image == null || imagePath == null || imagePath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        }

        Path filePath = uploadDir.resolve(imagePath).normalize();
        if(!Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image file not found");
        }
        return new ImageResponse(image.getId(), image.getAlt(), image.getImagePath());
    }
}
