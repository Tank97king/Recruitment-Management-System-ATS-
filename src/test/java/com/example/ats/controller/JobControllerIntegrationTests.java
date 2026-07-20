package com.example.ats.controller;

import com.example.ats.dto.request.CreateJobRequest;
import com.example.ats.dto.request.UpdateJobRequest;
import com.example.ats.entity.Company;
import com.example.ats.entity.Job;
import com.example.ats.entity.Role;
import com.example.ats.entity.User;
import com.example.ats.enums.EmploymentType;
import com.example.ats.enums.ExperienceLevel;
import com.example.ats.enums.JobStatus;
import com.example.ats.enums.RoleName;
import com.example.ats.enums.UserStatus;
import com.example.ats.repository.CompanyRepository;
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
@DisplayName("Job Controller Integration Tests")
class JobControllerIntegrationTests {

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
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User recruiter;
    private String recruiterToken;
    private Company company;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        companyRepository.deleteAll();
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

        // Generate Access Token
        recruiterToken = jwtService.generateAccessToken(recruiter);

        // Create Company
        company = new Company();
        company.setName("Ats Systems");
        company.setEmail("contact@ats.com");
        company.setIsDeleted(false);
        company = companyRepository.saveAndFlush(company);
    }

    @Test
    @DisplayName("Should allow public GET access to jobs list without token")
    void shouldAllowPublicGetJobs() throws Exception {
        mockMvc.perform(get("/api/v1/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", notNullValue()));
    }

    @Test
    @DisplayName("Should block write operations with 401 when token is missing")
    void shouldBlockWriteWhenTokenMissing() throws Exception {
        CreateJobRequest request = CreateJobRequest.builder()
                .title("Software Engineer")
                .companyId(company.getId())
                .build();

        mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should create job successfully when payload is valid")
    void shouldCreateJobSuccessfully() throws Exception {
        CreateJobRequest request = CreateJobRequest.builder()
                .title("Java Developer")
                .companyId(company.getId())
                .description("Java 21 development role.")
                .requirements("Spring Boot experience.")
                .location("Dallas, TX")
                .employmentType("FULL_TIME")
                .experienceLevel("MID")
                .salaryMin(new BigDecimal("80000.00"))
                .salaryMax(new BigDecimal("120000.00"))
                .deadline(LocalDate.now().plusDays(30))
                .build();

        mockMvc.perform(post("/api/v1/jobs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Job created successfully")))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.title", is("Java Developer")))
                .andExpect(jsonPath("$.data.status", is("OPEN")))
                .andExpect(jsonPath("$.data.company.companyName", is("Ats Systems")))
                .andExpect(jsonPath("$.data.salaryMin", is(80000.0)))
                .andExpect(jsonPath("$.data.salaryMax", is(120000.0)));
    }

    @Test
    @DisplayName("Should fail to create job when title is blank")
    void shouldFailWhenTitleIsBlank() throws Exception {
        CreateJobRequest request = CreateJobRequest.builder()
                .title("") // Blank
                .companyId(company.getId())
                .build();

        mockMvc.perform(post("/api/v1/jobs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field", is("title")))
                .andExpect(jsonPath("$.errors[0].message", is("Title is required")));
    }

    @Test
    @DisplayName("Should fail to create job when company does not exist")
    void shouldFailWhenCompanyDoesNotExist() throws Exception {
        UUID nonExistentCompanyId = UUID.randomUUID();
        CreateJobRequest request = CreateJobRequest.builder()
                .title("DevOps Engineer")
                .companyId(nonExistentCompanyId)
                .build();

        mockMvc.perform(post("/api/v1/jobs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Company not found with id: '" + nonExistentCompanyId + "'")));
    }

    @Test
    @DisplayName("Should fail to create job when salary values are negative")
    void shouldFailWhenSalaryIsNegative() throws Exception {
        CreateJobRequest request = CreateJobRequest.builder()
                .title("Frontend Developer")
                .companyId(company.getId())
                .salaryMin(new BigDecimal("-1000.00")) // Negative
                .build();

        mockMvc.perform(post("/api/v1/jobs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[0].message", is("Minimum salary must be zero or positive")));
    }

    @Test
    @DisplayName("Should fail to create job when minimum salary exceeds maximum")
    void shouldFailWhenMinExceedsMaxSalary() throws Exception {
        CreateJobRequest request = CreateJobRequest.builder()
                .title("Frontend Developer")
                .companyId(company.getId())
                .salaryMin(new BigDecimal("100000.00"))
                .salaryMax(new BigDecimal("90000.00")) // Min > Max
                .build();

        mockMvc.perform(post("/api/v1/jobs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[0].message", is("Salary min must not be greater than salary max")));
    }

    @Test
    @DisplayName("Should fail to create job when deadline is not in the future")
    void shouldFailWhenDeadlineInPast() throws Exception {
        CreateJobRequest request = CreateJobRequest.builder()
                .title("QA Specialist")
                .companyId(company.getId())
                .deadline(LocalDate.now().minusDays(1)) // Past
                .build();

        mockMvc.perform(post("/api/v1/jobs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[0].message", is("Deadline must be today or in the future")));
    }

    @Test
    @DisplayName("Should retrieve job details by ID successfully")
    void shouldGetJobById() throws Exception {
        Job job = new Job();
        job.setTitle("UX Designer");
        job.setDescription("UX Design description");
        job.setCompany(company);
        job.setCreatedByUser(recruiter);
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setStatus(JobStatus.OPEN);
        job.setIsDeleted(false);
        job = jobRepository.saveAndFlush(job);

        mockMvc.perform(get("/api/v1/jobs/{id}", job.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("UX Designer")))
                .andExpect(jsonPath("$.data.company.companyName", is("Ats Systems")));
    }

    @Test
    @DisplayName("Should update job successfully")
    void shouldUpdateJob() throws Exception {
        Job job = new Job();
        job.setTitle("Old Title");
        job.setDescription("Old description");
        job.setCompany(company);
        job.setCreatedByUser(recruiter);
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setStatus(JobStatus.OPEN);
        job.setIsDeleted(false);
        job = jobRepository.saveAndFlush(job);

        UpdateJobRequest request = UpdateJobRequest.builder()
                .title("New Title")
                .companyId(company.getId())
                .employmentType("PART_TIME")
                .status("CLOSED")
                .build();

        mockMvc.perform(put("/api/v1/jobs/{id}", job.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("New Title")))
                .andExpect(jsonPath("$.data.status", is("CLOSED")))
                .andExpect(jsonPath("$.data.employmentType", is("PART_TIME")));
    }

    @Test
    @DisplayName("Should reject updates on CLOSED jobs unless they are reopened")
    void shouldRejectUpdateOnClosedJob() throws Exception {
        Job job = new Job();
        job.setTitle("Closed Job");
        job.setDescription("Closed job description");
        job.setCompany(company);
        job.setCreatedByUser(recruiter);
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setStatus(JobStatus.CLOSED); // CLOSED state
        job.setIsDeleted(false);
        job = jobRepository.saveAndFlush(job);

        UpdateJobRequest request = UpdateJobRequest.builder()
                .title("Attempted Update")
                .companyId(company.getId())
                .employmentType("FULL_TIME")
                .status("CLOSED") // trying to keep it CLOSED or not specifying OPEN
                .build();

        mockMvc.perform(put("/api/v1/jobs/{id}", job.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("A CLOSED job cannot be updated unless reopened.")));

        // Reopen should succeed
        request.setStatus("OPEN");
        mockMvc.perform(put("/api/v1/jobs/{id}", job.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("OPEN")));
    }

    @Test
    @DisplayName("Should soft delete job successfully")
    void shouldSoftDeleteJob() throws Exception {
        Job job = new Job();
        job.setTitle("To Delete");
        job.setDescription("Description to delete");
        job.setCompany(company);
        job.setCreatedByUser(recruiter);
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setStatus(JobStatus.OPEN);
        job.setIsDeleted(false);
        job = jobRepository.saveAndFlush(job);

        mockMvc.perform(delete("/api/v1/jobs/{id}", job.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Job deleted successfully")));

        // Verify soft-deleted
        Job deletedJob = jobRepository.findById(job.getId()).orElse(null);
        org.junit.jupiter.api.Assertions.assertNotNull(deletedJob);
        org.junit.jupiter.api.Assertions.assertTrue(deletedJob.getIsDeleted());
    }

    @Test
    @DisplayName("Should retrieve paginated list of jobs with advanced filtering, search, and sorting")
    void shouldRetrievePaginatedJobsWithAdvancedFilters() throws Exception {
        // Seed Job 1
        Job job1 = new Job();
        job1.setTitle("Software Engineer");
        job1.setDescription("Excellent Java role");
        job1.setCompany(company);
        job1.setCreatedByUser(recruiter);
        job1.setLocation("Austin, TX");
        job1.setEmploymentType(EmploymentType.FULL_TIME);
        job1.setExperienceLevel(ExperienceLevel.SENIOR);
        job1.setStatus(JobStatus.OPEN);
        job1.setSalaryMin(new BigDecimal("120000.00"));
        job1.setSalaryMax(new BigDecimal("150000.00"));
        job1.setDeadline(LocalDate.now().plusDays(10));
        job1.setIsDeleted(false);
        jobRepository.save(job1);

        // Seed Job 2
        Job job2 = new Job();
        job2.setTitle("QA Analyst");
        job2.setDescription("Manual and automated testing");
        job2.setCompany(company);
        job2.setCreatedByUser(recruiter);
        job2.setLocation("Boston, MA");
        job2.setEmploymentType(EmploymentType.CONTRACT);
        job2.setExperienceLevel(ExperienceLevel.MID);
        job2.setStatus(JobStatus.OPEN);
        job2.setSalaryMin(new BigDecimal("60000.00"));
        job2.setSalaryMax(new BigDecimal("80000.00"));
        job2.setDeadline(LocalDate.now().plusDays(5));
        job2.setIsDeleted(false);
        jobRepository.save(job2);

        // Seed Job 3
        Job job3 = new Job();
        job3.setTitle("Product Manager");
        job3.setDescription("Product lifecycle management");
        job3.setCompany(company);
        job3.setCreatedByUser(recruiter);
        job3.setLocation("San Francisco, CA");
        job3.setEmploymentType(EmploymentType.FULL_TIME);
        job3.setExperienceLevel(ExperienceLevel.LEAD);
        job3.setStatus(JobStatus.CLOSED);
        job3.setSalaryMin(new BigDecimal("150000.00"));
        job3.setSalaryMax(new BigDecimal("200000.00"));
        job3.setDeadline(LocalDate.now().plusDays(20));
        job3.setIsDeleted(false);
        jobRepository.save(job3);

        jobRepository.flush();

        // 1. Search by keyword
        mockMvc.perform(get("/api/v1/jobs")
                        .param("keyword", "Analyst"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title", is("QA Analyst")));

        // 2. Filter by location (case-insensitive substring)
        mockMvc.perform(get("/api/v1/jobs")
                        .param("location", "austin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title", is("Software Engineer")));

        // 3. Filter by employment type
        mockMvc.perform(get("/api/v1/jobs")
                        .param("employmentType", "CONTRACT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title", is("QA Analyst")));

        // 4. Filter by salary range
        mockMvc.perform(get("/api/v1/jobs")
                        .param("salaryMin", "100000.00")
                        .param("salaryMax", "160000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title", is("Software Engineer")));

        // 5. Filter by deadline range
        mockMvc.perform(get("/api/v1/jobs")
                        .param("deadlineFrom", LocalDate.now().plusDays(2).toString())
                        .param("deadlineTo", LocalDate.now().plusDays(7).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title", is("QA Analyst")));

        // 6. Sort by salaryMin desc
        mockMvc.perform(get("/api/v1/jobs")
                        .param("sortBy", "salaryMin")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.content[0].title", is("Product Manager")))
                .andExpect(jsonPath("$.data.content[1].title", is("Software Engineer")))
                .andExpect(jsonPath("$.data.content[2].title", is("QA Analyst")));
    }
}
