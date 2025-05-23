package com.example.demo.controller;

import com.example.demo.dto.StatusUpdateRequest;
import com.example.demo.model.Employee;
import com.example.demo.model.TireRequest;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.TireRequestRepository;
import com.example.demo.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tire-requests")
public class TireRequestController {

    @Autowired
    private TireRequestRepository tireRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // 1. Submit a new tire request with image uploads
    @PostMapping(value = "/", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> createTireRequest(
            @RequestPart("tireRequest") TireRequest tireRequest, // JSON part
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        
        // Validate officerServiceNo
        if (tireRequest.getOfficerServiceNo() == null ||
            employeeRepository.findByEmployeeId(tireRequest.getOfficerServiceNo()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Invalid or missing officerServiceNo.");
        }

        tireRequest.setRequestDate(new Date());
        tireRequest.setStatus("Pending");
        tireRequest.setImagePaths(new ArrayList<>()); // Initialize imagePaths

        // Save the tireRequest first to generate its ID
        TireRequest savedTireRequest = tireRequestRepository.save(tireRequest);
        String tireRequestId = savedTireRequest.getId();

        if (images != null && images.length > 0) {
            List<String> uploadedFileNames = new ArrayList<>();
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        String storedFileName = fileStorageService.store(image, tireRequestId);
                        uploadedFileNames.add(storedFileName);
                    } catch (RuntimeException e) {
                        // If storing fails, we might want to delete already saved request or handle partial failure
                        // For now, delete the request and its previously uploaded files
                        fileStorageService.deleteAllFilesForTireRequest(tireRequestId);
                        tireRequestRepository.deleteById(tireRequestId);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                             .body("Failed to store image " + image.getOriginalFilename() + ": " + e.getMessage());
                    }
                }
            }
            if (!uploadedFileNames.isEmpty()) {
                savedTireRequest.setImagePaths(uploadedFileNames);
                savedTireRequest = tireRequestRepository.save(savedTireRequest); // Save again with image paths
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTireRequest);
    }


    // 2. Get all tire requests
    @GetMapping("/")
    public ResponseEntity<List<TireRequest>> getAllTireRequests() {
        List<TireRequest> requests = tireRequestRepository.findAll();
        return ResponseEntity.ok(requests);
    }

    // 3. Get a tire request by its ID
    @GetMapping("/{id}")
    public ResponseEntity<TireRequest> getTireRequestById(@PathVariable String id) {
        Optional<TireRequest> tireRequest = tireRequestRepository.findById(id);
        return tireRequest.map(ResponseEntity::ok)
                          .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 4. Get tire requests by the user who submitted them
    @GetMapping("/user/{officerServiceNo}")
    public ResponseEntity<List<TireRequest>> getTireRequestsByUser(@PathVariable String officerServiceNo) {
        if (employeeRepository.findByEmployeeId(officerServiceNo).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        List<TireRequest> requests = tireRequestRepository.findByOfficerServiceNo(officerServiceNo);
        return ResponseEntity.ok(requests);
    }

    // 5. Get tire requests by their current status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TireRequest>> getTireRequestsByStatus(@PathVariable String status) {
        List<TireRequest> requests = tireRequestRepository.findByStatus(status);
        return ResponseEntity.ok(requests);
    }

    // 6. Update the status of a tire request
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTireRequestStatus(@PathVariable String id, @RequestBody StatusUpdateRequest statusUpdateRequest) {
        Optional<TireRequest> optionalTireRequest = tireRequestRepository.findById(id);
        if (optionalTireRequest.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (statusUpdateRequest.getApprovedBy() == null ||
            employeeRepository.findByEmployeeId(statusUpdateRequest.getApprovedBy()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Invalid or missing approvedBy employee ID.");
        }
        
        Optional<Employee> approverOptional = employeeRepository.findByEmployeeId(statusUpdateRequest.getApprovedBy());
        if (approverOptional.isEmpty()) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Approver (employee) with ID " + statusUpdateRequest.getApprovedBy() + " not found.");
        }
        // TODO: Check if approver has "MANAGER" or "TTO" role.

        TireRequest tireRequest = optionalTireRequest.get();
        String newStatus = statusUpdateRequest.getStatus();

        if (!("Approved".equalsIgnoreCase(newStatus) || "Rejected".equalsIgnoreCase(newStatus))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Invalid status value. Must be 'Approved' or 'Rejected'.");
        }

        tireRequest.setStatus(newStatus);
        tireRequest.setApprovedBy(statusUpdateRequest.getApprovedBy());
        if (statusUpdateRequest.getComments() != null) {
            String existingComments = tireRequest.getComments() == null ? "" : tireRequest.getComments() + " | ";
            tireRequest.setComments(existingComments + "Status update (" + newStatus + "): " + statusUpdateRequest.getComments());
        }

        if ("Approved".equalsIgnoreCase(newStatus)) {
            tireRequest.setApprovedDate(new Date());
            tireRequest.setRejectedDate(null); 
        } else if ("Rejected".equalsIgnoreCase(newStatus)) {
            tireRequest.setRejectedDate(new Date());
            tireRequest.setApprovedDate(null); 
        }

        TireRequest updatedTireRequest = tireRequestRepository.save(tireRequest);
        return ResponseEntity.ok(updatedTireRequest);
    }

    // 7. Endpoint to serve files
    @GetMapping("/images/{tireRequestId}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String tireRequestId, @PathVariable String filename) {
        Resource file;
        try {
            file = fileStorageService.loadAsResource(filename, tireRequestId);
        } catch (RuntimeException e) {
            // Log the error and return 404
            // System.err.println("Error loading file: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }

        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        
        String contentType = null;
        try {
            contentType = Files.probeContentType(file.getFile().toPath());
        } catch (IOException e) {
            // log error
        }
        if(contentType == null) {
            contentType = "application/octet-stream"; // Default content type if unable to determine
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
    
    // 8. Endpoint to delete a specific image for a tire request
    @DeleteMapping("/{tireRequestId}/images/{filename:.+}")
    public ResponseEntity<?> deleteFile(@PathVariable String tireRequestId, @PathVariable String filename) {
        Optional<TireRequest> optionalTireRequest = tireRequestRepository.findById(tireRequestId);
        if (optionalTireRequest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("TireRequest not found.");
        }

        TireRequest tireRequest = optionalTireRequest.get();
        if (!tireRequest.getImagePaths().contains(filename)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Filename not associated with this TireRequest.");
        }

        try {
            fileStorageService.delete(filename, tireRequestId);
            tireRequest.getImagePaths().remove(filename);
            tireRequestRepository.save(tireRequest); // Update the TireRequest
            return ResponseEntity.ok().body("File deleted successfully: " + filename);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not delete file: " + e.getMessage());
        }
    }
}
