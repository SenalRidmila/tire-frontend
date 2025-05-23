package com.example.demo.service;

import com.example.demo.config.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    private final Path rootLocation;
    private final String uploadDir;

    @Autowired
    public FileStorageService(FileStorageProperties properties) {
        this.uploadDir = properties.getUploadDir();
        if (this.uploadDir == null || this.uploadDir.isEmpty()) {
            throw new RuntimeException("File upload directory path cannot be empty. Please set 'file.upload-dir' in application properties.");
        }
        this.rootLocation = Paths.get(this.uploadDir);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            // System.out.println("Created upload directory: " + rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location: " + rootLocation.toAbsolutePath(), e);
        }
    }

    public String store(MultipartFile file, String tireRequestId) {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }
        if (tireRequestId == null || tireRequestId.isBlank()) {
            throw new RuntimeException("TireRequest ID cannot be null or empty when storing a file.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i);
        }
        // Ensure only image files are stored (basic check)
        if (!extension.matches("\\.(png|jpg|jpeg|gif)$")) {
            throw new RuntimeException("Invalid file type. Only PNG, JPG, JPEG, GIF images are allowed. Filename: " + originalFilename);
        }
        
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        Path tireRequestSpecificDir = this.rootLocation.resolve(tireRequestId).normalize().toAbsolutePath();
        
        // Security check: ensure tireRequestSpecificDir is child of rootLocation
        if (!tireRequestSpecificDir.startsWith(this.rootLocation.toAbsolutePath())) {
             throw new RuntimeException("Cannot store file outside of configured root upload directory.");
        }

        try {
            Files.createDirectories(tireRequestSpecificDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create directory for tire request: " + tireRequestSpecificDir, e);
        }

        Path destinationFile = tireRequestSpecificDir.resolve(uniqueFilename).normalize().toAbsolutePath();

        // Double check, this should be redundant if above check is correct
        if (!destinationFile.getParent().equals(tireRequestSpecificDir)) {
            throw new RuntimeException("Cannot store file outside current tire request directory structure.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + originalFilename, e);
        }
        return uniqueFilename; // Only return the filename, not the full path
    }

    public Path load(String filename, String tireRequestId) {
        if (tireRequestId == null || tireRequestId.isBlank()) {
            throw new RuntimeException("TireRequest ID cannot be null or empty when loading a file.");
        }
        Path tireRequestSpecificDir = this.rootLocation.resolve(tireRequestId);
        return tireRequestSpecificDir.resolve(filename);
    }

    public Resource loadAsResource(String filename, String tireRequestId) {
        try {
            Path file = load(filename, tireRequestId);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                // System.err.println("Could not read file: " + file.toAbsolutePath());
                throw new RuntimeException("Could not read file: " + filename + " for tire request " + tireRequestId);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL for file: " + filename, e);
        }
    }
    
    public void delete(String filename, String tireRequestId) {
        try {
            Path file = load(filename, tireRequestId);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + filename, e);
        }
    }

    public void deleteAllFilesForTireRequest(String tireRequestId) {
        if (tireRequestId == null || tireRequestId.isBlank()) {
            // Or log a warning, depending on desired behavior
            return; 
        }
        Path tireRequestDir = this.rootLocation.resolve(tireRequestId);
        if (Files.exists(tireRequestDir) && Files.isDirectory(tireRequestDir)) {
            try {
                FileSystemUtils.deleteRecursively(tireRequestDir);
            } catch (IOException e) {
                throw new RuntimeException("Could not delete all files for tire request " + tireRequestId, e);
            }
        }
    }
}
