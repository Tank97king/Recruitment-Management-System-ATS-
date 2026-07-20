package com.example.ats.controller;

import com.example.ats.dto.request.CreateCandidateRequest;
import com.example.ats.dto.request.UpdateCandidateRequest;
import com.example.ats.entity.Candidate;
import com.example.ats.entity.Company;
import com.example.ats.entity.Job;
import com.example.ats.entity.JobApplication;
import com.example.ats.entity.Role;
import com.example.ats.entity.User;
import com.example.ats.enums.EmploymentType;
import com.example.ats.enums.JobStatus;
import com.example.ats.enums.RoleName;
import com.example.ats.enums.UserStatus;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
@DisplayName("Candidate Controller Integration Tests")
class CandidateControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User recruiter;
    private String recruiterToken;

    @BeforeEach
    void setUp() {
        jobApplicationRepository.deleteAll();
        jobRepository.deleteAll();
        companyRepository.deleteAll();
        candidateRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Seed Roles
        Role recruiterRole = roleRepository.findByName(RoleName.RECRUITER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.RECRUITER, "Recruiter")));

        // Create Recruiter
        recruiter = new User();
        recruiter.setEmail("recruiter@example.com");
        recruiter.setPasswordHash("password");
        recruiter.setFirstName("Recruiter");
        recruiter.setLastName("User");
        recruiter.setStatus(UserStatus.ACTIVE);
        recruiter.addRole(recruiterRole);
        recruiter = userRepository.saveAndFlush(recruiter);

        // Generate Token
        recruiterToken = jwtService.generateAccessToken(recruiter);
    }

    @Test
    @DisplayName("Should block CRUD operations with 401 when token is missing")
    void shouldBlockWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/candidates"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should create candidate successfully when payload is valid")
    void shouldCreateCandidateSuccessfully() throws Exception {
        CreateCandidateRequest request = CreateCandidateRequest.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+123456789")
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .gender("Male")
                .address("123 Elm Street")
                .yearsOfExperience(5)
                .highestEducation("Master's in Computer Science")
                .skills(List.of("Java", "Spring Boot", "PostgreSQL"))
                .summary("Experienced backend engineer.")
                .build();

        mockMvc.perform(post("/api/v1/candidates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Candidate profile created successfully")))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.fullName", is("John Doe")))
                .andExpect(jsonPath("$.data.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.data.phone", is("+123456789")))
                .andExpect(jsonPath("$.data.skills", hasSize(3)))
                .andExpect(jsonPath("$.data.skills[0]", is("Java")));
    }

    @Test
    @DisplayName("Should fail candidate creation when validation constraints fail")
    void shouldFailWhenConstraintsFail() throws Exception {
        CreateCandidateRequest request = CreateCandidateRequest.builder()
                .fullName("") // blank name
                .email("invalid-email") // invalid email
                .yearsOfExperience(-2) // negative experience
                .build();

        mockMvc.perform(post("/api/v1/candidates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    @DisplayName("Should fail candidate creation when email already exists")
    void shouldFailWhenEmailExists() throws Exception {
        Candidate existing = new Candidate();
        existing.setFirstName("Jane");
        existing.setLastName("Doe");
        existing.setEmail("jane.doe@example.com");
        existing.setIsDeleted(false);
        candidateRepository.saveAndFlush(existing);

        CreateCandidateRequest request = CreateCandidateRequest.builder()
                .fullName("Jane Smith")
                .email("jane.doe@example.com") // Conflict
                .build();

        mockMvc.perform(post("/api/v1/candidates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Candidate already exists with email: 'jane.doe@example.com'")));
    }

    @Test
    @DisplayName("Should get candidate details by ID successfully")
    void shouldGetCandidateById() throws Exception {
        Candidate candidate = new Candidate();
        candidate.setFirstName("Alice");
        candidate.setLastName("Smith");
        candidate.setEmail("alice@example.com");
        candidate.setAddress("Seattle, WA");
        candidate.setIsDeleted(false);
        candidate = candidateRepository.saveAndFlush(candidate);

        mockMvc.perform(get("/api/v1/candidates/{id}", candidate.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.fullName", is("Alice Smith")))
                .andExpect(jsonPath("$.data.address", is("Seattle, WA")));
    }

    @Test
    @DisplayName("Should update candidate successfully")
    void shouldUpdateCandidate() throws Exception {
        Candidate candidate = new Candidate();
        candidate.setFirstName("Bob");
        candidate.setLastName("Jones");
        candidate.setEmail("bob@jones.com");
        candidate.setIsDeleted(false);
        candidate = candidateRepository.saveAndFlush(candidate);

        UpdateCandidateRequest request = UpdateCandidateRequest.builder()
                .fullName("Robert Jones")
                .email("robert@jones.com")
                .phone("+999888777")
                .skills(List.of("Python", "Django"))
                .build();

        mockMvc.perform(put("/api/v1/candidates/{id}", candidate.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.fullName", is("Robert Jones")))
                .andExpect(jsonPath("$.data.email", is("robert@jones.com")))
                .andExpect(jsonPath("$.data.skills", hasSize(2)))
                .andExpect(jsonPath("$.data.skills[0]", is("Python")));
    }

    @Test
    @DisplayName("Should retrieve paginated list and keyword search matching full name, email, or phone")
    void shouldRetrievePaginatedAndFilteredCandidates() throws Exception {
        Candidate c1 = new Candidate();
        c1.setFirstName("David");
        c1.setLastName("Miller");
        c1.setEmail("david.m@test.com");
        c1.setPhone("+111222333");
        c1.setIsDeleted(false);
        candidateRepository.save(c1);

        Candidate c2 = new Candidate();
        c2.setFirstName("Miller");
        c2.setLastName("Smith");
        c2.setEmail("smith@test.com");
        c2.setPhone("+444555666");
        c2.setIsDeleted(false);
        candidateRepository.save(c2);

        candidateRepository.flush();

        // Search matches "David Miller" (Full Name match)
        mockMvc.perform(get("/api/v1/candidates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .param("search", "david Miller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].email", is("david.m@test.com")));

        // Search matches "+444555666" (Phone match)
        mockMvc.perform(get("/api/v1/candidates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .param("search", "+444555666"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].fullName", is("Miller Smith")));
    }

    @Test
    @DisplayName("Should soft delete candidate successfully when there are no applications")
    void shouldSoftDeleteCandidate() throws Exception {
        Candidate candidate = new Candidate();
        candidate.setFirstName("Mark");
        candidate.setLastName("Taylor");
        candidate.setEmail("mark@taylor.com");
        candidate.setIsDeleted(false);
        candidate = candidateRepository.saveAndFlush(candidate);

        mockMvc.perform(delete("/api/v1/candidates/{id}", candidate.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Candidate profile deleted successfully")));

        // Verify soft-deleted
        Candidate deleted = candidateRepository.findById(candidate.getId()).orElse(null);
        org.junit.jupiter.api.Assertions.assertNotNull(deleted);
        org.junit.jupiter.api.Assertions.assertTrue(deleted.getIsDeleted());
    }

    @Test
    @DisplayName("Should reject deletion with 422 when candidate has job applications")
    void shouldRejectDeletionWhenCandidateHasApplications() throws Exception {
        // Seed Candidate
        Candidate candidate = new Candidate();
        candidate.setFirstName("Sarah");
        candidate.setLastName("Connor");
        candidate.setEmail("sarah@resistance.com");
        candidate.setIsDeleted(false);
        candidate = candidateRepository.saveAndFlush(candidate);

        // Seed Company
        Company company = new Company();
        company.setName("Cyberdyne");
        company.setIsDeleted(false);
        company = companyRepository.saveAndFlush(company);

        // Seed Job
        Job job = new Job();
        job.setTitle("AI Specialist");
        job.setDescription("Job description");
        job.setCompany(company);
        job.setCreatedByUser(recruiter);
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setStatus(JobStatus.OPEN);
        job.setIsDeleted(false);
        job = jobRepository.saveAndFlush(job);

        // Seed Job Application
        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setIsDeleted(false);
        jobApplicationRepository.saveAndFlush(application);

        // Attempt Delete Candidate
        mockMvc.perform(delete("/api/v1/candidates/{id}", candidate.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Cannot delete candidate with active job applications")));
    }
}
