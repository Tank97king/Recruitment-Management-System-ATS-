package com.example.ats.controller;

import com.example.ats.dto.request.CreateInterviewRequest;
import com.example.ats.dto.request.UpdateInterviewRequest;
import com.example.ats.dto.request.UpdateInterviewStatusRequest;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Interview Controller Integration Tests")
class InterviewControllerIntegrationTests {

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

    @Autowired
    private ObjectMapper objectMapper;

    private User recruiter;
    private String recruiterToken;
    private Company company;
    private Job job;
    private Candidate candidate;
    private JobApplication interviewStageApp;
    private JobApplication appliedStageApp;

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

        // Seed recruiter
        Role recruiterRole = roleRepository.findByName(RoleName.RECRUITER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.RECRUITER, "Recruiter")));
        recruiter = new User();
        recruiter.setEmail("recruiter.interview@example.com");
        recruiter.setPasswordHash("password");
        recruiter.setFirstName("Recruiter");
        recruiter.setLastName("User");
        recruiter.setStatus(UserStatus.ACTIVE);
        recruiter.addRole(recruiterRole);
        recruiter = userRepository.saveAndFlush(recruiter);
        recruiterToken = jwtService.generateAccessToken(recruiter);

        // Company & Job
        company = new Company();
        company.setName("Global Tech");
        company.setIsDeleted(false);
        company = companyRepository.saveAndFlush(company);

        job = new Job();
        job.setTitle("Senior Backend Engineer");
        job.setDescription("Backend Java role");
        job.setCompany(company);
        job.setCreatedByUser(recruiter);
        job.setStatus(JobStatus.OPEN);
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setIsDeleted(false);
        job = jobRepository.saveAndFlush(job);

        // Candidate
        candidate = new Candidate();
        candidate.setFirstName("Michael");
        candidate.setLastName("Scott");
        candidate.setEmail("michael.scott@example.com");
        candidate.setIsDeleted(false);
        candidate = candidateRepository.saveAndFlush(candidate);

        CandidateCv cv = new CandidateCv();
        cv.setCandidate(candidate);
        cv.setOriginalFileName("michael_cv.pdf");
        cv.setStoredFileName("michael_cv_stored.pdf");
        cv.setFilePath("/uploads/michael_cv_stored.pdf");
        cv.setFileSize(200L);
        cv.setContentType("application/pdf");
        candidate.setCv(cv);
        candidate = candidateRepository.saveAndFlush(candidate);

        // Application 1 in INTERVIEW stage
        interviewStageApp = new JobApplication();
        interviewStageApp.setCandidate(candidate);
        interviewStageApp.setJob(job);
        interviewStageApp.setStatus(ApplicationStatus.INTERVIEW);
        interviewStageApp.setIsDeleted(false);
        interviewStageApp = jobApplicationRepository.saveAndFlush(interviewStageApp);

        // Application 2 in APPLIED stage
        appliedStageApp = new JobApplication();
        appliedStageApp.setCandidate(candidate);
        appliedStageApp.setJob(job);
        appliedStageApp.setStatus(ApplicationStatus.APPLIED);
        appliedStageApp.setIsDeleted(false);
        appliedStageApp = jobApplicationRepository.saveAndFlush(appliedStageApp);
    }

    @Test
    @DisplayName("Should block interview scheduling with 401 when token is missing")
    void shouldBlockWhenTokenMissing() throws Exception {
        CreateInterviewRequest request = CreateInterviewRequest.builder()
                .jobApplicationId(interviewStageApp.getId())
                .interviewDate(LocalDateTime.now().plusDays(2))
                .interviewType("ONLINE")
                .interviewerName("Dwight Schrute")
                .interviewerEmail("dwight@example.com")
                .meetingLink("https://meet.google.com/abc-defg-hij")
                .build();

        mockMvc.perform(post("/api/v1/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should schedule ONLINE interview successfully when link is provided")
    void shouldScheduleOnlineInterviewSuccessfully() throws Exception {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(3);

        CreateInterviewRequest request = CreateInterviewRequest.builder()
                .jobApplicationId(interviewStageApp.getId())
                .interviewDate(futureDate)
                .interviewType("ONLINE")
                .interviewerName("Jim Halpert")
                .interviewerEmail("jim@example.com")
                .meetingLink("https://meet.google.com/xyz-1234-abc")
                .notes("Technical round")
                .build();

        mockMvc.perform(post("/api/v1/interviews")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Interview scheduled successfully")))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.candidateName", is("Michael Scott")))
                .andExpect(jsonPath("$.data.jobTitle", is("Senior Backend Engineer")))
                .andExpect(jsonPath("$.data.companyName", is("Global Tech")))
                .andExpect(jsonPath("$.data.interviewType", is("ONLINE")))
                .andExpect(jsonPath("$.data.interviewerName", is("Jim Halpert")))
                .andExpect(jsonPath("$.data.interviewerEmail", is("jim@example.com")))
                .andExpect(jsonPath("$.data.meetingLink", is("https://meet.google.com/xyz-1234-abc")))
                .andExpect(jsonPath("$.data.status", is("SCHEDULED")));
    }

    @Test
    @DisplayName("Should schedule OFFLINE interview successfully when location is provided")
    void shouldScheduleOfflineInterviewSuccessfully() throws Exception {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(4);

        CreateInterviewRequest request = CreateInterviewRequest.builder()
                .jobApplicationId(interviewStageApp.getId())
                .interviewDate(futureDate)
                .interviewType("OFFLINE")
                .interviewerName("Pam Beesly")
                .interviewerEmail("pam@example.com")
                .meetingLocation("Scranton Office, Room 204")
                .notes("Onsite interview")
                .build();

        mockMvc.perform(post("/api/v1/interviews")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.interviewType", is("OFFLINE")))
                .andExpect(jsonPath("$.data.meetingLocation", is("Scranton Office, Room 204")));
    }

    @Test
    @DisplayName("Should fail scheduling when meeting link is missing for ONLINE interview")
    void shouldFailWhenMeetingLinkMissingForOnline() throws Exception {
        CreateInterviewRequest request = CreateInterviewRequest.builder()
                .jobApplicationId(interviewStageApp.getId())
                .interviewDate(LocalDateTime.now().plusDays(2))
                .interviewType("ONLINE")
                .interviewerName("Ryan Howard")
                .interviewerEmail("ryan@example.com")
                .meetingLink("") // Missing
                .build();

        mockMvc.perform(post("/api/v1/interviews")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Meeting link is required for ONLINE interviews")));
    }

    @Test
    @DisplayName("Should fail scheduling when meeting location is missing for OFFLINE interview")
    void shouldFailWhenMeetingLocationMissingForOffline() throws Exception {
        CreateInterviewRequest request = CreateInterviewRequest.builder()
                .jobApplicationId(interviewStageApp.getId())
                .interviewDate(LocalDateTime.now().plusDays(2))
                .interviewType("OFFLINE")
                .interviewerName("Kelly Kapoor")
                .interviewerEmail("kelly@example.com")
                .meetingLocation("") // Missing
                .build();

        mockMvc.perform(post("/api/v1/interviews")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Meeting location is required for OFFLINE interviews")));
    }

    @Test
    @DisplayName("Should fail scheduling when interview date is in the past")
    void shouldFailWhenDateInPast() throws Exception {
        CreateInterviewRequest request = CreateInterviewRequest.builder()
                .jobApplicationId(interviewStageApp.getId())
                .interviewDate(LocalDateTime.now().minusDays(1)) // Past
                .interviewType("ONLINE")
                .interviewerName("Stanley Hudson")
                .interviewerEmail("stanley@example.com")
                .meetingLink("https://meet.google.com/past-date")
                .build();

        mockMvc.perform(post("/api/v1/interviews")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[0].message", is("Interview date must be in the future")));
    }

    @Test
    @DisplayName("Should fail scheduling when job application status is not INTERVIEW")
    void shouldFailWhenAppStatusNotInterview() throws Exception {
        CreateInterviewRequest request = CreateInterviewRequest.builder()
                .jobApplicationId(appliedStageApp.getId()) // APPLIED status
                .interviewDate(LocalDateTime.now().plusDays(5))
                .interviewType("ONLINE")
                .interviewerName("Phyllis Vance")
                .interviewerEmail("phyllis@example.com")
                .meetingLink("https://meet.google.com/applied-app")
                .build();

        mockMvc.perform(post("/api/v1/interviews")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Job Application status must be INTERVIEW to schedule an interview")));
    }

    @Test
    @DisplayName("Should update interview status using PATCH /api/interviews/{id}/status")
    void shouldPatchStatusSuccessfully() throws Exception {
        Interview interview = new Interview();
        interview.setApplication(interviewStageApp);
        interview.setScheduledAt(Instant.now().plusSeconds(86400));
        interview.setInterviewType(InterviewType.ONLINE);
        interview.setInterviewerNames("Andy Bernard");
        interview.setInterviewerEmail("andy@example.com");
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setMeetingLink("https://meet.google.com/patch-test");
        interview = interviewRepository.saveAndFlush(interview);

        UpdateInterviewStatusRequest request = UpdateInterviewStatusRequest.builder()
                .status("COMPLETED")
                .build();

        mockMvc.perform(patch("/api/v1/interviews/{id}/status", interview.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("COMPLETED")));
    }

    @Test
    @DisplayName("Should block invalid status transition on terminal interview state")
    void shouldBlockInvalidStatusTransition() throws Exception {
        Interview interview = new Interview();
        interview.setApplication(interviewStageApp);
        interview.setScheduledAt(Instant.now().plusSeconds(86400));
        interview.setInterviewType(InterviewType.ONLINE);
        interview.setInterviewerNames("Angela Martin");
        interview.setInterviewerEmail("angela@example.com");
        interview.setStatus(InterviewStatus.COMPLETED); // Terminal state
        interview.setMeetingLink("https://meet.google.com/terminal-test");
        interview = interviewRepository.saveAndFlush(interview);

        UpdateInterviewStatusRequest request = UpdateInterviewStatusRequest.builder()
                .status("SCHEDULED")
                .build();

        mockMvc.perform(patch("/api/v1/interviews/{id}/status", interview.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Cannot transition interview status from COMPLETED to SCHEDULED")));
    }

    @Test
    @DisplayName("Should retrieve paginated list and filter by candidateId, companyId, and status")
    void shouldRetrieveFilteredInterviews() throws Exception {
        Interview interview = new Interview();
        interview.setApplication(interviewStageApp);
        interview.setScheduledAt(Instant.now().plusSeconds(86400));
        interview.setInterviewType(InterviewType.ONLINE);
        interview.setInterviewerNames("Oscar Martinez");
        interview.setInterviewerEmail("oscar@example.com");
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setMeetingLink("https://meet.google.com/filter-test");
        interviewRepository.saveAndFlush(interview);

        mockMvc.perform(get("/api/v1/interviews")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .param("candidateId", candidate.getId().toString())
                        .param("companyId", company.getId().toString())
                        .param("interviewStatus", "SCHEDULED")
                        .param("interviewType", "ONLINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].interviewerName", is("Oscar Martinez")));
    }

    @Test
    @DisplayName("Should soft delete interview successfully")
    void shouldSoftDeleteInterview() throws Exception {
        Interview interview = new Interview();
        interview.setApplication(interviewStageApp);
        interview.setScheduledAt(Instant.now().plusSeconds(86400));
        interview.setInterviewType(InterviewType.ONLINE);
        interview.setInterviewerNames("Toby Flenderson");
        interview.setInterviewerEmail("toby@example.com");
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setMeetingLink("https://meet.google.com/delete-test");
        interview = interviewRepository.saveAndFlush(interview);

        mockMvc.perform(delete("/api/v1/interviews/{id}", interview.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Interview deleted successfully")));

        // Verify soft-deleted
        Interview deleted = interviewRepository.findById(interview.getId()).orElse(null);
        org.junit.jupiter.api.Assertions.assertNotNull(deleted);
        org.junit.jupiter.api.Assertions.assertTrue(deleted.getIsDeleted());
    }
}
