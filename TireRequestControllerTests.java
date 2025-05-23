package com.example.demo.controller;

import com.example.demo.dto.StatusUpdateRequest;
import com.example.demo.model.Employee;
import com.example.demo.model.TireRequest;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.TireRequestRepository;
import com.example.demo.service.AppUserDetailsService;
import com.example.demo.service.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TireRequestController.class)
public class TireRequestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TireRequestRepository tireRequestRepository;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private AppUserDetailsService appUserDetailsService; // For Spring Security context

    @Autowired
    private ObjectMapper objectMapper;

    private TireRequest tireRequest1;
    private TireRequest tireRequest2;
    private Employee mockEmployee;
    private Employee mockManager;

    @BeforeEach
    void setUp() {
        mockEmployee = new Employee("user123", "Test", "User", "user@example.com", "password", "ROLE_USER");
        mockManager = new Employee("manager456", "Test", "Manager", "manager@example.com", "password", "ROLE_MANAGER");

        tireRequest1 = new TireRequest();
        tireRequest1.setId("TR001");
        tireRequest1.setVehicleNo("V001");
        tireRequest1.setOfficerServiceNo(mockEmployee.getEmployeeId());
        tireRequest1.setStatus("Pending");
        tireRequest1.setRequestDate(new Date());
        tireRequest1.setImagePaths(new ArrayList<>());

        tireRequest2 = new TireRequest();
        tireRequest2.setId("TR002");
        tireRequest2.setVehicleNo("V002");
        tireRequest2.setOfficerServiceNo(mockEmployee.getEmployeeId());
        tireRequest2.setStatus("Approved");
        tireRequest2.setApprovedBy(mockManager.getEmployeeId());
        tireRequest2.setApprovedDate(new Date());
        tireRequest2.setImagePaths(new ArrayList<>(Arrays.asList("image2.jpg")));
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void createTireRequest_success_withImages() throws Exception {
        when(employeeRepository.findByEmployeeId(mockEmployee.getEmployeeId())).thenReturn(Optional.of(mockEmployee));
        
        // Capture TireRequest passed to save, to simulate ID generation
        ArgumentCaptor<TireRequest> tireRequestCaptor = ArgumentCaptor.forClass(TireRequest.class);
        when(tireRequestRepository.save(tireRequestCaptor.capture())).thenAnswer(invocation -> {
            TireRequest tr = invocation.getArgument(0);
            if (tr.getId() == null) tr.setId("TR_NEW_ID"); // Simulate ID generation
            return tr;
        });

        when(fileStorageService.store(any(MockMultipartFile.class), eq("TR_NEW_ID"))).thenReturn("stored_image1.jpg", "stored_image2.png");

        MockMultipartFile image1 = new MockMultipartFile("images", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, "image1_content".getBytes());
        MockMultipartFile image2 = new MockMultipartFile("images", "image2.png", MediaType.IMAGE_PNG_VALUE, "image2_content".getBytes());
        
        // TireRequest data as JSON string
        String tireRequestJson = objectMapper.writeValueAsString(tireRequest1); // tireRequest1 has officerServiceNo set
        MockMultipartFile tireRequestPart = new MockMultipartFile("tireRequest", "", MediaType.APPLICATION_JSON_VALUE, tireRequestJson.getBytes(StandardCharsets.UTF_8));


        mockMvc.perform(multipart("/api/tire-requests/")
                .file(image1)
                .file(image2)
                .file(tireRequestPart)
                .with(csrf()) // Important for multipart POST if CSRF is enabled (even if globally disabled in SecurityConfig for APIs, @WebMvcTest might behave differently)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("TR_NEW_ID")))
                .andExpect(jsonPath("$.officerServiceNo", is(mockEmployee.getEmployeeId())))
                .andExpect(jsonPath("$.imagePaths", hasSize(2)))
                .andExpect(jsonPath("$.imagePaths[0]", is("stored_image1.jpg")))
                .andExpect(jsonPath("$.imagePaths[1]", is("stored_image2.png")));

        verify(employeeRepository).findByEmployeeId(mockEmployee.getEmployeeId());
        verify(tireRequestRepository, times(2)).save(any(TireRequest.class)); // Once for ID, once for image paths
        verify(fileStorageService, times(2)).store(any(MockMultipartFile.class), eq("TR_NEW_ID"));
    }
    
    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void createTireRequest_success_noImages() throws Exception {
        when(employeeRepository.findByEmployeeId(mockEmployee.getEmployeeId())).thenReturn(Optional.of(mockEmployee));
        when(tireRequestRepository.save(any(TireRequest.class))).thenAnswer(invocation -> {
             TireRequest tr = invocation.getArgument(0);
            if (tr.getId() == null) tr.setId("TR_NO_IMG_ID");
            return tr;
        });
        
        String tireRequestJson = objectMapper.writeValueAsString(tireRequest1);
        MockMultipartFile tireRequestPart = new MockMultipartFile("tireRequest", "", MediaType.APPLICATION_JSON_VALUE, tireRequestJson.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/tire-requests/")
                .file(tireRequestPart)
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("TR_NO_IMG_ID")))
                .andExpect(jsonPath("$.officerServiceNo", is(mockEmployee.getEmployeeId())))
                .andExpect(jsonPath("$.imagePaths", hasSize(0)));

        verify(tireRequestRepository, times(1)).save(any(TireRequest.class)); // Only saved once
        verify(fileStorageService, never()).store(any(), anyString());
    }


    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void createTireRequest_invalidOfficerServiceNo() throws Exception {
        when(employeeRepository.findByEmployeeId(anyString())).thenReturn(Optional.empty());
        
        String tireRequestJson = objectMapper.writeValueAsString(tireRequest1); // Contains officerServiceNo
        MockMultipartFile tireRequestPart = new MockMultipartFile("tireRequest", "", MediaType.APPLICATION_JSON_VALUE, tireRequestJson.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/tire-requests/")
                .file(tireRequestPart)
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid or missing officerServiceNo."));

        verify(tireRequestRepository, never()).save(any());
        verify(fileStorageService, never()).store(any(), any());
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void createTireRequest_fileStorageError() throws Exception {
        when(employeeRepository.findByEmployeeId(mockEmployee.getEmployeeId())).thenReturn(Optional.of(mockEmployee));
        when(tireRequestRepository.save(any(TireRequest.class))).thenAnswer(invocation -> {
            TireRequest tr = invocation.getArgument(0);
            if (tr.getId() == null) tr.setId("TR_FAIL_ID");
            return tr;
        });
        
        MockMultipartFile image1 = new MockMultipartFile("images", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, "image1_content".getBytes());
        String tireRequestJson = objectMapper.writeValueAsString(tireRequest1);
        MockMultipartFile tireRequestPart = new MockMultipartFile("tireRequest", "", MediaType.APPLICATION_JSON_VALUE, tireRequestJson.getBytes(StandardCharsets.UTF_8));


        when(fileStorageService.store(any(MockMultipartFile.class), eq("TR_FAIL_ID")))
            .thenThrow(new RuntimeException("Disk full"));

        mockMvc.perform(multipart("/api/tire-requests/")
                .file(image1)
                .file(tireRequestPart)
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("Failed to store image image1.jpg: Disk full")));
        
        verify(fileStorageService).store(any(MockMultipartFile.class), eq("TR_FAIL_ID"));
        // Verify cleanup
        verify(fileStorageService).deleteAllFilesForTireRequest("TR_FAIL_ID");
        verify(tireRequestRepository).deleteById("TR_FAIL_ID");
    }

    @Test
    @WithMockUser
    void getAllTireRequests_success() throws Exception {
        when(tireRequestRepository.findAll()).thenReturn(Arrays.asList(tireRequest1, tireRequest2));
        mockMvc.perform(get("/api/tire-requests/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("TR001")))
                .andExpect(jsonPath("$[1].id", is("TR002")));
    }

    @Test
    @WithMockUser
    void getTireRequestById_success() throws Exception {
        when(tireRequestRepository.findById("TR001")).thenReturn(Optional.of(tireRequest1));
        mockMvc.perform(get("/api/tire-requests/{id}", "TR001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("TR001")));
    }

    @Test
    @WithMockUser
    void getTireRequestById_notFound() throws Exception {
        when(tireRequestRepository.findById("TR_NON_EXISTENT")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/tire-requests/{id}", "TR_NON_EXISTENT"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser
    void getTireRequestsByUser_success() throws Exception {
        when(employeeRepository.findByEmployeeId(mockEmployee.getEmployeeId())).thenReturn(Optional.of(mockEmployee));
        when(tireRequestRepository.findByOfficerServiceNo(mockEmployee.getEmployeeId())).thenReturn(Arrays.asList(tireRequest1));

        mockMvc.perform(get("/api/tire-requests/user/{officerServiceNo}", mockEmployee.getEmployeeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].officerServiceNo", is(mockEmployee.getEmployeeId())));
    }

    @Test
    @WithMockUser
    void getTireRequestsByUser_userNotFound() throws Exception {
        when(employeeRepository.findByEmployeeId("UNKNOWN_USER")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/tire-requests/user/{officerServiceNo}", "UNKNOWN_USER"))
                .andExpect(status().isNotFound()); // As per controller logic
    }
    
    @Test
    @WithMockUser
    void getTireRequestsByStatus_success() throws Exception {
        when(tireRequestRepository.findByStatus("Pending")).thenReturn(Arrays.asList(tireRequest1));
        mockMvc.perform(get("/api/tire-requests/status/{status}", "Pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("Pending")));
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void updateTireRequestStatus_success() throws Exception {
        StatusUpdateRequest statusUpdate = new StatusUpdateRequest();
        statusUpdate.setStatus("Approved");
        statusUpdate.setApprovedBy(mockManager.getEmployeeId());
        statusUpdate.setComments("Looks good.");

        when(tireRequestRepository.findById("TR001")).thenReturn(Optional.of(tireRequest1));
        when(employeeRepository.findByEmployeeId(mockManager.getEmployeeId())).thenReturn(Optional.of(mockManager));
        when(tireRequestRepository.save(any(TireRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/tire-requests/{id}/status", "TR001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Approved")))
                .andExpect(jsonPath("$.approvedBy", is(mockManager.getEmployeeId())))
                .andExpect(jsonPath("$.comments", containsString("Looks good.")));
        
        verify(tireRequestRepository).save(any(TireRequest.class));
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void updateTireRequestStatus_requestNotFound() throws Exception {
        StatusUpdateRequest statusUpdate = new StatusUpdateRequest();
        statusUpdate.setStatus("Approved");
        statusUpdate.setApprovedBy(mockManager.getEmployeeId());
        when(tireRequestRepository.findById("TR_NON_EXISTENT")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/tire-requests/{id}/status", "TR_NON_EXISTENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser(roles = {"MANAGER"})
    void updateTireRequestStatus_invalidApprovedBy() throws Exception {
        StatusUpdateRequest statusUpdate = new StatusUpdateRequest();
        statusUpdate.setStatus("Approved");
        statusUpdate.setApprovedBy("UNKNOWN_APPROVER");

        when(tireRequestRepository.findById("TR001")).thenReturn(Optional.of(tireRequest1));
        when(employeeRepository.findByEmployeeId("UNKNOWN_APPROVER")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/tire-requests/{id}/status", "TR001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid or missing approvedBy employee ID."));
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void updateTireRequestStatus_invalidStatusValue() throws Exception {
        StatusUpdateRequest statusUpdate = new StatusUpdateRequest();
        statusUpdate.setStatus("INVALID_STATUS_XYZ");
        statusUpdate.setApprovedBy(mockManager.getEmployeeId());

        when(tireRequestRepository.findById("TR001")).thenReturn(Optional.of(tireRequest1));
        when(employeeRepository.findByEmployeeId(mockManager.getEmployeeId())).thenReturn(Optional.of(mockManager));


        mockMvc.perform(put("/api/tire-requests/{id}/status", "TR001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid status value. Must be 'Approved' or 'Rejected'."));
    }


    @Test
    @WithMockUser // No specific role needed if SecurityConfig permits all for image serving
    void serveFile_success() throws Exception {
        String filename = "test-image.png";
        String tireRequestId = "TR001";
        Resource mockResource = new ByteArrayResource("image content".getBytes(), "Test Image");
        
        when(fileStorageService.loadAsResource(filename, tireRequestId)).thenReturn(mockResource);

        mockMvc.perform(get("/api/tire-requests/images/{tireRequestId}/{filename:.+}", tireRequestId, filename))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("inline; filename=\"" + filename + "\"")))
                .andExpect(content().bytes("image content".getBytes()));
    }

    @Test
    @WithMockUser
    void serveFile_notFound() throws Exception {
        String filename = "notfound.png";
        String tireRequestId = "TR001";
        when(fileStorageService.loadAsResource(filename, tireRequestId)).thenThrow(new RuntimeException("Could not read file"));

        mockMvc.perform(get("/api/tire-requests/images/{tireRequestId}/{filename:.+}", tireRequestId, filename))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser(username="user123", roles={"USER"})
    void deleteFile_success() throws Exception {
        String tireRequestId = "TR001";
        String filename = "imageToDelete.png";
        tireRequest1.getImagePaths().add(filename); // Ensure the image is part of the request

        when(tireRequestRepository.findById(tireRequestId)).thenReturn(Optional.of(tireRequest1));
        doNothing().when(fileStorageService).delete(filename, tireRequestId);
        when(tireRequestRepository.save(any(TireRequest.class))).thenReturn(tireRequest1);

        mockMvc.perform(delete("/api/tire-requests/{tireRequestId}/images/{filename:.+}", tireRequestId, filename)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("File deleted successfully: " + filename));

        verify(fileStorageService).delete(filename, tireRequestId);
        verify(tireRequestRepository).save(tireRequest1);
        assertTrue(tireRequest1.getImagePaths().isEmpty()); // Assuming it was the only image
    }

    @Test
    @WithMockUser(username="user123", roles={"USER"})
    void deleteFile_requestNotFound() throws Exception {
        when(tireRequestRepository.findById("TR_NON_EXISTENT")).thenReturn(Optional.empty());
        mockMvc.perform(delete("/api/tire-requests/{tireRequestId}/images/{filename:.+}", "TR_NON_EXISTENT", "anyfile.png")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("TireRequest not found."));
    }
    
    @Test
    @WithMockUser(username="user123", roles={"USER"})
    void deleteFile_imageNotAssociatedWithRequest() throws Exception {
        String tireRequestId = "TR001"; // tireRequest1 has empty imagePaths initially
        String filename = "non_associated_image.png";
        
        when(tireRequestRepository.findById(tireRequestId)).thenReturn(Optional.of(tireRequest1)); // tireRequest1 has no imagePaths

        mockMvc.perform(delete("/api/tire-requests/{tireRequestId}/images/{filename:.+}", tireRequestId, filename)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Filename not associated with this TireRequest."));
    }

}
