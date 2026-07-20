package com.example.ats.controller;

import com.example.ats.dto.request.UpdateApplicationStageRequest;
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
import java.time.Instant;
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

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Pipeline Controller Integration Tests")
class PipelineControllerIntegrationTests {

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
    private Job job;
    private Candidate candidate1;
    private Candidate candidate2;

    @BeforeEach
    void setUp() {
        jobApplicationRepository.deleteAll();
        jobRepository.deleteAll();
        companyRepository.deleteAll();
        candidateCvRepository.deleteAll();
        candidateRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Seed recruiter
        Role recruiterRole = roleRepository.findByName(RoleName.RECRUITER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.RECRUITER, "Recruiter")));
        recruiter = new User();
        recruiter.setEmail("recruiter.pipeline@example.com");
        recruiter.setPasswordHash("password");
        recruiter.setFirstName("Recruiter");
        recruiter.setLastName("Pipeline");
        recruiter.setStatus(UserStatus.ACTIVE);
        recruiter.addRole(recruiterRole);
        recruiter = userRepository.saveAndFlush(recruiter);
        recruiterToken = jwtService.generateAccessToken(recruiter);

        // Company & Job
        company = new Company();
        company.setName("Pipeline Inc");
        company.setIsDeleted(false);
        company = companyRepository.saveAndFlush(company);

        job = new Job();
        job.setTitle("DevOps Lead");
        job.setDescription("Cloud Infrastructure role");
        job.setCompany(company);
        job.setCreatedByUser(recruiter);
        job.setStatus(JobStatus.OPEN);
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setIsDeleted(false);
        job = jobRepository.saveAndFlush(job);

        // Candidate 1
        candidate1 = new Candidate();
        candidate1.setFirstName("Alice");
        candidate1.setLastName("Wonder");
        candidate1.setEmail("alice@pipeline.com");
        candidate1.setIsDeleted(false);
        candidate1 = candidateRepository.saveAndFlush(candidate1);

        // Candidate 2
        candidate2 = new Candidate();
        candidate2.setFirstName("Bob");
        candidate2.setLastName("Builder");
        candidate2.setEmail("bob@pipeline.com");
        candidate2.setIsDeleted(false);
        candidate2 = candidateRepository.saveAndFlush(candidate2);
    }

    @Test
    @DisplayName("Should block pipeline requests with 401 when token is missing")
    void shouldBlockWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/pipeline"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should retrieve pipeline board with 6 stage columns")
    void shouldRetrievePipelineWith6Columns() throws Exception {
        // Application 1 in APPLIED
        JobApplication app1 = new JobApplication();
        app1.setCandidate(candidate1);
        app1.setJob(job);
        app1.setStatus(ApplicationStatus.APPLIED);
        app1.setIsDeleted(false);
        jobApplicationRepository.saveAndFlush(app1);

        // Application 2 in REVIEWING
        JobApplication app2 = new JobApplication();
        app2.setCandidate(candidate2);
        app2.setJob(job);
        app2.setStatus(ApplicationStatus.REVIEWING);
        app2.setIsDeleted(false);
        jobApplicationRepository.saveAndFlush(app2);

        mockMvc.perform(get("/api/v1/pipeline")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.columns", hasSize(6)))
                .andExpect(jsonPath("$.data.columns[0].stage", is("APPLIED")))
                .andExpect(jsonPath("$.data.columns[0].totalApplications", is(1)))
                .andExpect(jsonPath("$.data.columns[1].stage", is("REVIEWING")))
                .andExpect(jsonPath("$.data.columns[1].totalApplications", is(1)));
    }

    @Test
    @DisplayName("Should retrieve pipeline by job ID")
    void shouldRetrievePipelineByJobId() throws Exception {
        JobApplication app = new JobApplication();
        app.setCandidate(candidate1);
        app.setJob(job);
        app.setStatus(ApplicationStatus.APPLIED);
        app.setIsDeleted(false);
        jobApplicationRepository.saveAndFlush(app);

        mockMvc.perform(get("/api/v1/pipeline/jobs/{jobId}", job.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.columns", hasSize(6)));
    }

    @Test
    @DisplayName("Should return 404 when getting pipeline for non-existent job ID")
    void shouldReturn404ForMissingJob() throws Exception {
        mockMvc.perform(get("/api/v1/pipeline/jobs/{jobId}", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Should advance application stage along allowed path (APPLIED -> REVIEWING -> INTERVIEW)")
    void shouldAdvanceStageSuccessfully() throws Exception {
        JobApplication app = new JobApplication();
        app.setCandidate(candidate1);
        app.setJob(job);
        app.setStatus(ApplicationStatus.APPLIED);
        app.setAppliedAt(Instant.now());
        app.setIsDeleted(false);
        app = jobApplicationRepository.saveAndFlush(app);

        UpdateApplicationStageRequest req1 = UpdateApplicationStageRequest.builder()
                .status("REVIEWING")
                .build();

        mockMvc.perform(patch("/api/v1/pipeline/applications/{id}/status", app.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStatus", is("REVIEWING")));

        UpdateApplicationStageRequest req2 = UpdateApplicationStageRequest.builder()
                .status("INTERVIEW")
                .build();

        mockMvc.perform(patch("/api/v1/pipeline/applications/{id}/status", app.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStatus", is("INTERVIEW")));
    }

    @Test
    @DisplayName("Should allow moving to REJECTED from any non-terminal stage")
    void shouldAllowRejectionFromNonTerminalStage() throws Exception {
        JobApplication app = new JobApplication();
        app.setCandidate(candidate1);
        app.setJob(job);
        app.setStatus(ApplicationStatus.REVIEWING);
        app.setAppliedAt(Instant.now());
        app.setIsDeleted(false);
        app = jobApplicationRepository.saveAndFlush(app);

        UpdateApplicationStageRequest request = UpdateApplicationStageRequest.builder()
                .status("REJECTED")
                .build();

        mockMvc.perform(patch("/api/v1/pipeline/applications/{id}/status", app.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStatus", is("REJECTED")));
    }

    @Test
    @DisplayName("Should reject invalid stage transition (HIRED -> REVIEWING or REJECTED -> APPLIED)")
    void shouldRejectInvalidStageTransition() throws Exception {
        JobApplication app = new JobApplication();
        app.setCandidate(candidate1);
        app.setJob(job);
        app.setStatus(ApplicationStatus.HIRED); // Terminal stage
        app.setAppliedAt(Instant.now());
        app.setIsDeleted(false);
        app = jobApplicationRepository.saveAndFlush(app);

        UpdateApplicationStageRequest request = UpdateApplicationStageRequest.builder()
                .status("REVIEWING") // Invalid move out of HIRED
                .build();

        mockMvc.perform(patch("/api/v1/pipeline/applications/{id}/status", app.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Invalid status transition from HIRED to REVIEWING")));
    }

    @Test
    @DisplayName("Should retrieve pipeline summary totals correctly")
    void shouldRetrievePipelineSummary() throws Exception {
        JobApplication app1 = new JobApplication();
        app1.setCandidate(candidate1);
        app1.setJob(job);
        app1.setStatus(ApplicationStatus.APPLIED);
        app1.setIsDeleted(false);
        jobApplicationRepository.saveAndFlush(app1);

        JobApplication app2 = new JobApplication();
        app2.setCandidate(candidate2);
        app2.setJob(job);
        app2.setStatus(ApplicationStatus.HIRED);
        app2.setIsDeleted(false);
        jobApplicationRepository.saveAndFlush(app2);

        mockMvc.perform(get("/api/v1/pipeline/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .param("jobId", job.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalApplications", is(2)))
                .andExpect(jsonPath("$.data.applied", is(1)))
                .andExpect(jsonPath("$.data.hired", is(1)))
                .andExpect(jsonPath("$.data.rejected", is(0)));
    }
}
