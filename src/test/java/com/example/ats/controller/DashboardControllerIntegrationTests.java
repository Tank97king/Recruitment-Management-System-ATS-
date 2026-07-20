package com.example.ats.controller;

import com.example.ats.entity.Candidate;
import com.example.ats.entity.CandidateCv;
import com.example.ats.entity.Company;
import com.example.ats.entity.Interview;
import com.example.ats.entity.Job;
import com.example.ats.entity.JobApplication;
import com.example.ats.entity.Role;
import com.example.ats.entity.User;
import com.example.ats.enums.ApplicationStatus;
import com.example.ats.enums.EmploymentType;
import com.example.ats.enums.InterviewStatus;
import com.example.ats.enums.InterviewType;
import com.example.ats.enums.JobStatus;
import com.example.ats.enums.RoleName;
import com.example.ats.enums.UserStatus;
import com.example.ats.repository.CandidateCvRepository;
import com.example.ats.repository.CandidateRepository;
import com.example.ats.repository.CompanyRepository;
import com.example.ats.repository.InterviewRepository;
import com.example.ats.repository.JobApplicationRepository;
import com.example.ats.repository.JobRepository;
import com.example.ats.repository.RoleRepository;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Dashboard Controller Integration Tests")
class DashboardControllerIntegrationTests {

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
    private InterviewRepository interviewRepository;

    @Autowired
    private JwtService jwtService;

    private User admin;
    private String adminToken;

    @BeforeEach
    void setUp() {
        interviewRepository.deleteAll();
        jobApplicationRepository.deleteAll();
        jobRepository.deleteAll();
        companyRepository.deleteAll();
        candidateCvRepository.deleteAll();
        candidateRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Admin User
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ADMIN, "Admin")));
        admin = new User();
        admin.setEmail("admin.dashboard@example.com");
        admin.setPasswordHash("password");
        admin.setFirstName("Admin");
        admin.setLastName("Stats");
        admin.setStatus(UserStatus.ACTIVE);
        admin.addRole(adminRole);
        admin = userRepository.saveAndFlush(admin);
        adminToken = jwtService.generateAccessToken(admin);

        // Companies
        Company c1 = new Company();
        c1.setName("Acme Analytics");
        c1.setIsDeleted(false);
        c1 = companyRepository.saveAndFlush(c1);

        Company c2 = new Company();
        c2.setName("Quiet Enterprise");
        c2.setIsDeleted(false);
        c2 = companyRepository.saveAndFlush(c2);

        // Jobs
        Job openJob = new Job();
        openJob.setTitle("Backend Lead");
        openJob.setDescription("Java Spring");
        openJob.setCompany(c1);
        openJob.setCreatedByUser(admin);
        openJob.setStatus(JobStatus.OPEN);
        openJob.setEmploymentType(EmploymentType.FULL_TIME);
        openJob.setIsDeleted(false);
        openJob = jobRepository.saveAndFlush(openJob);

        Job closedJob = new Job();
        closedJob.setTitle("Staging Dev");
        closedJob.setDescription("Temporary post");
        closedJob.setCompany(c1);
        closedJob.setCreatedByUser(admin);
        closedJob.setStatus(JobStatus.CLOSED);
        closedJob.setEmploymentType(EmploymentType.CONTRACT);
        closedJob.setIsDeleted(false);
        closedJob = jobRepository.saveAndFlush(closedJob);

        // Candidates
        Candidate candidateWithCv = new Candidate();
        candidateWithCv.setFirstName("Carol");
        candidateWithCv.setLastName("Danvers");
        candidateWithCv.setEmail("carol@example.com");
        candidateWithCv.setIsDeleted(false);
        candidateWithCv = candidateRepository.saveAndFlush(candidateWithCv);

        CandidateCv cv = new CandidateCv();
        cv.setCandidate(candidateWithCv);
        cv.setOriginalFileName("resume.pdf");
        cv.setStoredFileName("stored.pdf");
        cv.setFilePath("/uploads/stored.pdf");
        cv.setFileSize(300L);
        cv.setContentType("application/pdf");
        candidateWithCv.setCv(cv);
        candidateWithCv = candidateRepository.saveAndFlush(candidateWithCv);

        Candidate candidateNoCv = new Candidate();
        candidateNoCv.setFirstName("David");
        candidateNoCv.setLastName("Banner");
        candidateNoCv.setEmail("david@example.com");
        candidateNoCv.setIsDeleted(false);
        candidateNoCv = candidateRepository.saveAndFlush(candidateNoCv);

        // Applications
        JobApplication app1 = new JobApplication();
        app1.setCandidate(candidateWithCv);
        app1.setJob(openJob);
        app1.setStatus(ApplicationStatus.APPLIED);
        app1.setIsDeleted(false);
        app1 = jobApplicationRepository.saveAndFlush(app1);

        JobApplication app2 = new JobApplication();
        app2.setCandidate(candidateNoCv);
        app2.setJob(openJob);
        app2.setStatus(ApplicationStatus.HIRED);
        app2.setIsDeleted(false);
        app2 = jobApplicationRepository.saveAndFlush(app2);

        // Interview
        Interview interview = new Interview();
        interview.setApplication(app1);
        interview.setScheduledAt(Instant.now().plusSeconds(3600));
        interview.setInterviewType(InterviewType.ONLINE);
        interview.setInterviewerNames("Nick Fury");
        interview.setInterviewerEmail("fury@example.com");
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setMeetingLink("https://meet.google.com/dashboard-test");
        interviewRepository.saveAndFlush(interview);
    }

    @Test
    @DisplayName("Should block dashboard requests with 401 when token is missing")
    void shouldBlockWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return accurate dashboard summary totals")
    void shouldGetDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalUsers", is(1)))
                .andExpect(jsonPath("$.data.totalCompanies", is(2)))
                .andExpect(jsonPath("$.data.totalJobs", is(2)))
                .andExpect(jsonPath("$.data.totalCandidates", is(2)))
                .andExpect(jsonPath("$.data.totalApplications", is(2)))
                .andExpect(jsonPath("$.data.totalInterviews", is(1)));
    }

    @Test
    @DisplayName("Should return application statistics and percentage breakdowns")
    void shouldGetApplicationStatistics() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalApplications", is(2)))
                .andExpect(jsonPath("$.data.applied", is(1)))
                .andExpect(jsonPath("$.data.appliedPercentage", is(50.0)))
                .andExpect(jsonPath("$.data.hired", is(1)))
                .andExpect(jsonPath("$.data.hiredPercentage", is(50.0)))
                .andExpect(jsonPath("$.data.rejected", is(0)))
                .andExpect(jsonPath("$.data.rejectedPercentage", is(0.0)));
    }

    @Test
    @DisplayName("Should return job statistics by status")
    void shouldGetJobStatistics() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/jobs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalJobs", is(2)))
                .andExpect(jsonPath("$.data.openJobs", is(1)))
                .andExpect(jsonPath("$.data.closedJobs", is(1)))
                .andExpect(jsonPath("$.data.draftJobs", is(0)));
    }

    @Test
    @DisplayName("Should return candidate resume statistics")
    void shouldGetCandidateStatistics() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/candidates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalCandidates", is(2)))
                .andExpect(jsonPath("$.data.candidatesWithCv", is(1)))
                .andExpect(jsonPath("$.data.candidatesWithoutCv", is(1)));
    }

    @Test
    @DisplayName("Should return company active job statistics")
    void shouldGetCompanyStatistics() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/companies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalCompanies", is(2)))
                .andExpect(jsonPath("$.data.companiesWithActiveJobs", is(1)))
                .andExpect(jsonPath("$.data.companiesWithoutJobs", is(1)));
    }
}
