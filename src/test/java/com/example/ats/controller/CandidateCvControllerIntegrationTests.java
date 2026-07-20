package com.example.ats.controller;

import com.example.ats.entity.Candidate;
import com.example.ats.entity.Role;
import com.example.ats.entity.User;
import com.example.ats.enums.RoleName;
import com.example.ats.enums.UserStatus;
import com.example.ats.repository.CandidateCvRepository;
import com.example.ats.repository.CandidateRepository;
import com.example.ats.repository.RoleRepository;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.FileStorageService;
import com.example.ats.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Candidate CV Controller Integration Tests")
class CandidateCvControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateCvRepository candidateCvRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JwtService jwtService;

    private User recruiter;
    private String recruiterToken;
    private Candidate candidate;

    @BeforeEach
    void setUp() {
        candidateCvRepository.deleteAll();
        candidateRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Seed recruiter
        Role recruiterRole = roleRepository.findByName(RoleName.RECRUITER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.RECRUITER, "Recruiter")));
        recruiter = new User();
        recruiter.setEmail("recruiter.cv@example.com");
        recruiter.setPasswordHash("password");
        recruiter.setFirstName("Recruiter");
        recruiter.setLastName("CV");
        recruiter.setStatus(UserStatus.ACTIVE);
        recruiter.addRole(recruiterRole);
        recruiter = userRepository.saveAndFlush(recruiter);
        recruiterToken = jwtService.generateAccessToken(recruiter);

        // Seed candidate
        candidate = new Candidate();
        candidate.setFirstName("CV");
        candidate.setLastName("Candidate");
        candidate.setEmail("candidate.cv@example.com");
        candidate.setIsDeleted(false);
        candidate = candidateRepository.saveAndFlush(candidate);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up uploads directory created during testing
        Path uploadsPath = Paths.get("uploads").toAbsolutePath().normalize();
        if (Files.exists(uploadsPath)) {
            Files.walk(uploadsPath)
                    .sorted((p1, p2) -> p2.compareTo(p1))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {}
                    });
        }
    }

    @Test
    @DisplayName("Should reject CV upload with 401 when token is missing")
    void shouldRejectWhenTokenMissing() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "dummy content".getBytes());
        mockMvc.perform(multipart("/api/v1/candidates/{id}/cv", candidate.getId())
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should upload CV successfully when file type is PDF")
    void shouldUploadCvSuccessfully() throws Exception {
        byte[] pdfBytes = "dummy pdf header and content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "my_resume.pdf", "application/pdf", pdfBytes);

        mockMvc.perform(multipart("/api/v1/candidates/{id}/cv", candidate.getId())
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("CV uploaded successfully")))
                .andExpect(jsonPath("$.data.hasCv", is(true)))
                .andExpect(jsonPath("$.data.cvFileName", is("my_resume.pdf")))
                .andExpect(jsonPath("$.data.uploadedAt", notNullValue()));

        // Assert file exists on disk
        Candidate updatedCandidate = candidateRepository.findById(candidate.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotNull(updatedCandidate.getCv());
        Path storedFile = Paths.get(updatedCandidate.getCv().getFilePath());
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(storedFile));
    }

    @Test
    @DisplayName("Should reject CV upload when file type is invalid (e.g. PNG)")
    void shouldRejectInvalidFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "png bytes".getBytes());

        mockMvc.perform(multipart("/api/v1/candidates/{id}/cv", candidate.getId())
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Only PDF files are allowed. Rejected content type: image/png")));
    }

    @Test
    @DisplayName("Should replace previous CV on disk and in database upon new CV upload")
    void shouldReplacePreviousCv() throws Exception {
        // Upload first CV
        MockMultipartFile file1 = new MockMultipartFile("file", "resume_v1.pdf", "application/pdf", "v1 content".getBytes());
        mockMvc.perform(multipart("/api/v1/candidates/{id}/cv", candidate.getId())
                        .file(file1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk());

        Candidate candWithCv1 = candidateRepository.findById(candidate.getId()).orElseThrow();
        Path firstCvPath = Paths.get(candWithCv1.getCv().getFilePath());
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(firstCvPath));

        // Upload second CV replacing it
        MockMultipartFile file2 = new MockMultipartFile("file", "resume_v2.pdf", "application/pdf", "v2 content".getBytes());
        mockMvc.perform(multipart("/api/v1/candidates/{id}/cv", candidate.getId())
                        .file(file2)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cvFileName", is("resume_v2.pdf")));

        // Verify first file is deleted from disk
        org.junit.jupiter.api.Assertions.assertFalse(Files.exists(firstCvPath));

        // Verify second file exists on disk
        Candidate candWithCv2 = candidateRepository.findById(candidate.getId()).orElseThrow();
        Path secondCvPath = Paths.get(candWithCv2.getCv().getFilePath());
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(secondCvPath));
    }

    @Test
    @DisplayName("Should download CV successfully when it exists")
    void shouldDownloadCv() throws Exception {
        // First upload a CV
        byte[] originalBytes = "pdf test content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "my_resume.pdf", "application/pdf", originalBytes);
        mockMvc.perform(multipart("/api/v1/candidates/{id}/cv", candidate.getId())
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk());

        // Perform download
        byte[] downloadedBytes = mockMvc.perform(get("/api/v1/candidates/{id}/cv", candidate.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        org.junit.jupiter.api.Assertions.assertArrayEquals(originalBytes, downloadedBytes);
    }

    @Test
    @DisplayName("Should return 404 when downloading CV that does not exist")
    void shouldReturn404WhenCvMissing() throws Exception {
        mockMvc.perform(get("/api/v1/candidates/{id}/cv", candidate.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("CV not found for candidate with ID: " + candidate.getId())));
    }

    @Test
    @DisplayName("Should delete CV file from disk and metadata from database")
    void shouldDeleteCvSuccessfully() throws Exception {
        // Upload a CV
        MockMultipartFile file = new MockMultipartFile("file", "resume_to_delete.pdf", "application/pdf", "delete me content".getBytes());
        mockMvc.perform(multipart("/api/v1/candidates/{id}/cv", candidate.getId())
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk());

        Candidate candWithCv = candidateRepository.findById(candidate.getId()).orElseThrow();
        Path cvPath = Paths.get(candWithCv.getCv().getFilePath());
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(cvPath));

        // Delete CV
        mockMvc.perform(delete("/api/v1/candidates/{id}/cv", candidate.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("CV deleted successfully")));

        // Verify database metadata is removed
        Candidate candAfterDelete = candidateRepository.findById(candidate.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertNull(candAfterDelete.getCv());

        // Verify physical file is deleted from disk
        org.junit.jupiter.api.Assertions.assertFalse(Files.exists(cvPath));
    }
}
