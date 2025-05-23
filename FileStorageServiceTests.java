package com.example.demo.service;

import com.example.demo.config.FileStorageProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class FileStorageServiceTests {

    @TempDir
    Path tempDir; // JUnit 5 extension to create a temporary directory

    private FileStorageService fileStorageService;
    private FileStorageProperties properties;
    private String tireRequestId = "TR001";

    @BeforeEach
    void setUp() throws IOException {
        // Create a subdirectory within tempDir that matches the expected structure
        Path actualUploadDir = tempDir.resolve("test_uploads");
        Files.createDirectories(actualUploadDir);

        properties = new FileStorageProperties();
        properties.setUploadDir(actualUploadDir.toString());
        
        fileStorageService = new FileStorageService(properties); // Use constructor that takes properties
        fileStorageService.init(); // Manually call init since we are not using full Spring context
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up the temporary directory if needed, though @TempDir should handle it
        // FileSystemUtils.deleteRecursively(tempDir);
    }

    @Test
    void store_success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test-image.png", "image/png", "test image content".getBytes());

        String filename = fileStorageService.store(file, tireRequestId);

        assertNotNull(filename);
        assertTrue(filename.endsWith(".png"));
        Path expectedPath = fileStorageService.getRootLocation().resolve(tireRequestId).resolve(filename);
        assertTrue(Files.exists(expectedPath));
        assertEquals("test image content", Files.readString(expectedPath));
    }

    @Test
    void store_invalidFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "some text".getBytes());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.store(file, tireRequestId);
        });
        assertTrue(exception.getMessage().contains("Invalid file type"));
    }

    @Test
    void store_emptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "empty.png", "image/png", new byte[0]);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.store(file, tireRequestId);
        });
        assertTrue(exception.getMessage().contains("Failed to store empty file"));
    }
    
    @Test
    void store_nullTireRequestId() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test-image.png", "image/png", "test image content".getBytes());
        Exception exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.store(file, null);
        });
        assertTrue(exception.getMessage().contains("TireRequest ID cannot be null or empty"));
    }

    @Test
    void loadAsResource_success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "image", "image.jpg", "image/jpeg", "content".getBytes());
        String filename = fileStorageService.store(file, tireRequestId);

        Resource resource = fileStorageService.loadAsResource(filename, tireRequestId);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertEquals(filename, resource.getFilename());
    }

    @Test
    void loadAsResource_fileNotFound() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.loadAsResource("nonexistent.jpg", tireRequestId);
        });
        assertTrue(exception.getMessage().contains("Could not read file: nonexistent.jpg"));
    }
    
    @Test
    void loadAsResource_nullFilename() {
         Exception exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.loadAsResource(null, tireRequestId);
        });
        assertTrue(exception.getMessage().contains("Filename cannot be null or empty"));
    }


    @Test
    void delete_success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "image", "delete-me.png", "image/png", "content".getBytes());
        String filename = fileStorageService.store(file, tireRequestId);
        Path filePath = fileStorageService.getRootLocation().resolve(tireRequestId).resolve(filename);
        assertTrue(Files.exists(filePath));

        fileStorageService.delete(filename, tireRequestId);
        assertFalse(Files.exists(filePath));
    }

    @Test
    void delete_fileNotFound() {
        // Should not throw an error, as deleteIfExists is used.
        assertDoesNotThrow(() -> {
            fileStorageService.delete("non_existent_file.png", tireRequestId);
        });
    }

    @Test
    void deleteAllFilesForTireRequest_success() throws IOException {
        MockMultipartFile file1 = new MockMultipartFile("img1", "img1.png", "image/png", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("img2", "img2.jpg", "image/jpeg", "content2".getBytes());
        fileStorageService.store(file1, tireRequestId);
        fileStorageService.store(file2, tireRequestId);

        Path tireRequestDirPath = fileStorageService.getRootLocation().resolve(tireRequestId);
        assertTrue(Files.exists(tireRequestDirPath));
        assertEquals(2, Files.list(tireRequestDirPath).count());

        fileStorageService.deleteAllFilesForTireRequest(tireRequestId);
        assertFalse(Files.exists(tireRequestDirPath));
    }
    
    @Test
    void deleteAllFilesForTireRequest_nonExistentDirectory() {
         assertDoesNotThrow(() -> {
            fileStorageService.deleteAllFilesForTireRequest("TR_NON_EXISTENT");
        });
    }

    @Test
    void init_createsDirectory() {
        // The @BeforeEach already calls init(). We just verify the root location exists.
        assertTrue(Files.exists(fileStorageService.getRootLocation()));
        assertTrue(Files.isDirectory(fileStorageService.getRootLocation()));
    }
    
    @Test
    void store_filenameCleaning() throws IOException {
        // Test with a filename that needs cleaning (e.g., contains "..")
        // StringUtils.cleanPath should handle this.
        MockMultipartFile file = new MockMultipartFile(
                "image", "../../../test-image.png", "image/png", "test image content".getBytes());

        String filename = fileStorageService.store(file, tireRequestId);
        assertNotNull(filename);
        assertFalse(filename.contains("..")); // Ensure ".." is removed or handled by cleanPath
        
        Path expectedPath = fileStorageService.getRootLocation().resolve(tireRequestId).resolve(filename);
        assertTrue(Files.exists(expectedPath));
    }
}
