package com.example.ats.controller;

import com.example.ats.dto.request.CreateJobApplicationRequest;
import com.example.ats.dto.request.UpdateApplicationStatusRequest;
import com.example.ats.entity.Candidate;
import com.example.ats.entity.CandidateCv;
import com.example.ats.entity.Company;
import com.example.ats.entity.Job;
import com.example.ats.entity.JobApplication;
import com.example.ats.entity.Role;
import com.example.ats.entity.User;
import com.example.ats.enums.ApplicationStatus;
import com.example.ats.enums.EmploymentType;
import com.example.ats.enums.JobStatus;
import com.example.ats.enums.RoleName;
import com.example.ats.enums.UserStatus;
import com.example.ats.repository.CandidateCvRepository;
import com.example.ats.repository.CandidateRepository;
import com.example.ats.repository.CompanyRepository;
import com.example.ats.repository.JobApplicationRepository;
import com.example.ats.repository.JobRepository;
import com.example.ats.repository.RoleRepository;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Job Application Controller Integration Tests")
class JobApplicationControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateCvRepository candidateCvRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User recruiter;
    private String recruiterToken;
    private Company company;
    private Job openJob;
    private Job closedJob;
    private Candidate candidateWithCv;
    private Candidate candidateNoCv;

    @BeforeEach
    void setUp() {
        jobApplicationRepository.deleteAll();
        jobRepository.deleteAll();
        companyRepository.deleteAll();
        candidateCvRepository.deleteAll();
        candidateRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Recruiter
        Role recruiterRole = roleRepository.findByName(RoleName.RECRUITER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.RECRUITER, "Recruiter")));
        recruiter = new User();
        recruiter.setEmail("recruiter@example.com");
        recruiter.setPasswordHash("password");
        recruiter.setFirstName("Recruiter");
        recruiter.setLastName("User");
        recruiter.setStatus(UserStatus.ACTIVE);
        recruiter.addRole(recruiterRole);
        recruiter = userRepository.saveAndFlush(recruiter);
        recruiterToken = jwtService.generateAccessToken(recruiter);

        // Company
        company = new Company();
        company.setName("Acme Corp");
        company.setIsDeleted(false);
        company = companyRepository.saveAndFlush(company);

        // Open Job
        openJob = new Job();
        openJob.setTitle("Java Developer");
        openJob.setDescription("Backend dev role");
        openJob.setCompany(company);
        openJob.setCreatedByUser(recruiter);
        openJob.setStatus(JobStatus.OPEN);
        openJob.setEmploymentType(EmploymentType.FULL_TIME);
        openJob.setIsDeleted(false);
        openJob = jobRepository.saveAndFlush(openJob);

        // Closed Job
        closedJob = new Job();
        closedJob.setTitle("UI Designer");
        closedJob.setDescription("Frontend designer role");
        closedJob.setCompany(company);
        closedJob.setCreatedByUser(recruiter);
        closedJob.setStatus(JobStatus.CLOSED);
        closedJob.setEmploymentType(EmploymentType.FULL_TIME);
        closedJob.setIsDeleted(false);
        closedJob = jobRepository.saveAndFlush(closedJob);

        // Candidate with CV
        candidateWithCv = new Candidate();
        candidateWithCv.setFirstName("John");
        candidateWithCv.setLastName("Doe");
        candidateWithCv.setEmail("john.doe@example.com");
        candidateWithCv.setIsDeleted(false);
        candidateWithCv = candidateRepository.saveAndFlush(candidateWithCv);

        CandidateCv cv = new CandidateCv();
        cv.setCandidate(candidateWithCv);
        cv.setOriginalFileName("resume.pdf");
        cv.setStoredFileName("stored.pdf");
        cv.setFilePath("/uploads/stored.pdf");
        cv.setFileSize(100L);
        cv.setContentType("application/pdf");
        candidateWithCv.setCv(cv);
        candidateWithCv = candidateRepository.saveAndFlush(candidateWithCv);

        // Candidate without CV
        candidateNoCv = new Candidate();
        candidateNoCv.setFirstName("Jane");
        candidateNoCv.setLastName("Smith");
        candidateNoCv.setEmail("jane.smith@example.com");
        candidateNoCv.setIsDeleted(false);
        candidateNoCv = candidateRepository.saveAndFlush(candidateNoCv);
    }

    @Test
    @DisplayName("Should reject job application creation with 401 when token is missing")
    void shouldRejectWhenTokenMissing() throws Exception {
        CreateJobApplicationRequest request = CreateJobApplicationRequest.builder()
                .candidateId(candidateWithCv.getId())
                .jobId(openJob.getId())
                .coverLetter("Here is my application.")
                .build();

        mockMvc.perform(post("/api/v1/job-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should submit application successfully with default status APPLIED")
    void shouldSubmitApplicationSuccessfully() throws Exception {
        CreateJobApplicationRequest request = CreateJobApplicationRequest.builder()
                .candidateId(candidateWithCv.getId())
                .jobId(openJob.getId())
                .coverLetter("Here is my application.")
                .build();

        mockMvc.perform(post("/api/v1/job-applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Job application submitted successfully")))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.candidateId", is(candidateWithCv.getId().toString())))
                .andExpect(jsonPath("$.data.jobId", is(openJob.getId().toString())))
                .andExpect(jsonPath("$.data.applicationStatus", is("APPLIED")))
                .andExpect(jsonPath("$.data.coverLetter", is("Here is my application.")));
    }

    @Test
    @DisplayName("Should fail application creation when candidate has no CV")
    void shouldFailWhenCvMissing() throws Exception {
        CreateJobApplicationRequest request = CreateJobApplicationRequest.builder()
                .candidateId(candidateNoCv.getId())
                .jobId(openJob.getId())
                .build();

        mockMvc.perform(post("/api/v1/job-applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Candidate must upload a CV before applying for a job")));
    }

    @Test
    @DisplayName("Should fail application creation when Job status is not OPEN")
    void shouldFailWhenJobClosed() throws Exception {
        CreateJobApplicationRequest request = CreateJobApplicationRequest.builder()
                .candidateId(candidateWithCv.getId())
                .jobId(closedJob.getId())
                .build();

        mockMvc.perform(post("/api/v1/job-applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Cannot apply to a job that is not OPEN")));
    }

    @Test
    @DisplayName("Should reject duplicate applications for the same candidate and job")
    void shouldRejectDuplicateApplications() throws Exception {
        // First application
        JobApplication application = new JobApplication();
        application.setCandidate(candidateWithCv);
        application.setJob(openJob);
        application.setStatus(ApplicationStatus.APPLIED);
        jobApplicationRepository.saveAndFlush(application);

        // Try duplicate submission
        CreateJobApplicationRequest request = CreateJobApplicationRequest.builder()
                .candidateId(candidateWithCv.getId())
                .jobId(openJob.getId())
                .build();

        mockMvc.perform(post("/api/v1/job-applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Should retrieve job application details successfully")
    void shouldGetJobApplicationDetails() throws Exception {
        JobApplication application = new JobApplication();
        application.setCandidate(candidateWithCv);
        application.setJob(openJob);
        application.setStatus(ApplicationStatus.APPLIED);
        application = jobApplicationRepository.saveAndFlush(application);

        mockMvc.perform(get("/api/v1/job-applications/{id}", application.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(application.getId().toString())))
                .andExpect(jsonPath("$.data.candidateName", is("John Doe")));
    }

    @Test
    @DisplayName("Should transition status correctly along the allowed flow")
    void shouldTransitionStatusCorrectly() throws Exception {
        JobApplication application = new JobApplication();
        application.setCandidate(candidateWithCv);
        application.setJob(openJob);
        application.setStatus(ApplicationStatus.APPLIED);
        application = jobApplicationRepository.saveAndFlush(application);

        // Valid transition: APPLIED -> REVIEWING
        UpdateApplicationStatusRequest req1 = UpdateApplicationStatusRequest.builder()
                .status("REVIEWING")
                .build();

        mockMvc.perform(put("/api/v1/job-applications/{id}/status", application.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationStatus", is("REVIEWING")));

        // Valid transition: REVIEWING -> REJECTED
        UpdateApplicationStatusRequest req2 = UpdateApplicationStatusRequest.builder()
                .status("REJECTED")
                .build();

        mockMvc.perform(put("/api/v1/job-applications/{id}/status", application.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationStatus", is("REJECTED")));
    }

    @Test
    @DisplayName("Should block invalid status transitions (e.g. APPLIED -> OFFER)")
    void shouldBlockInvalidTransition() throws Exception {
        JobApplication application = new JobApplication();
        application.setCandidate(candidateWithCv);
        application.setJob(openJob);
        application.setStatus(ApplicationStatus.APPLIED);
        application = jobApplicationRepository.saveAndFlush(application);

        UpdateApplicationStatusRequest request = UpdateApplicationStatusRequest.builder()
                .status("OFFER") // Invalid transition: skipped REVIEWING and INTERVIEW
                .build();

        mockMvc.perform(put("/api/v1/job-applications/{id}/status", application.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Invalid status transition from APPLIED to OFFER")));
    }

    @Test
    @DisplayName("Should retrieve filtered and paginated job applications")
    void shouldRetrieveFilteredApplications() throws Exception {
        JobApplication app = new JobApplication();
        app.setCandidate(candidateWithCv);
        app.setJob(openJob);
        app.setStatus(ApplicationStatus.REVIEWING);
        jobApplicationRepository.saveAndFlush(app);

        // Filter by candidate ID and status REVIEWING
        mockMvc.perform(get("/api/v1/job-applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .param("candidateId", candidateWithCv.getId().toString())
                        .param("status", "REVIEWING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].candidateName", is("John Doe")));
    }

    @Test
    @DisplayName("Should soft delete job application successfully")
    void shouldSoftDeleteJobApplication() throws Exception {
        JobApplication app = new JobApplication();
        app.setCandidate(candidateWithCv);
        app.setJob(openJob);
        app.setStatus(ApplicationStatus.APPLIED);
        app = jobApplicationRepository.saveAndFlush(app);

        mockMvc.perform(delete("/api/v1/job-applications/{id}", app.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Job application deleted successfully")));

        // Verify soft-deleted
        JobApplication deleted = jobApplicationRepository.findById(app.getId()).orElse(null);
        org.junit.jupiter.api.Assertions.assertNotNull(deleted);
        org.junit.jupiter.api.Assertions.assertTrue(deleted.getIsDeleted());
    }
}
