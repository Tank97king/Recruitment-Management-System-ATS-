package com.example.ats.controller;

import com.example.ats.dto.request.CreateCompanyRequest;
import com.example.ats.dto.request.UpdateCompanyRequest;
import com.example.ats.entity.Company;
import com.example.ats.entity.Job;
import com.example.ats.entity.Role;
import com.example.ats.entity.User;
import com.example.ats.enums.EmploymentType;
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
@DisplayName("Company Controller Integration Tests")
class CompanyControllerIntegrationTests {

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
    }

    @Test
    @DisplayName("Should block request with 401 when token is missing")
    void shouldBlockWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/companies"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should create company successfully when payload is valid")
    void shouldCreateCompanySuccessfully() throws Exception {
        CreateCompanyRequest request = CreateCompanyRequest.builder()
                .companyName("Tech Corp")
                .email("contact@techcorp.com")
                .phone("+1234567890")
                .website("https://techcorp.com")
                .address("123 Silicon Valley Road")
                .description("A leading technology solutions provider.")
                .build();

        mockMvc.perform(post("/api/v1/companies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Company created successfully")))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.companyName", is("Tech Corp")))
                .andExpect(jsonPath("$.data.email", is("contact@techcorp.com")))
                .andExpect(jsonPath("$.data.phone", is("+1234567890")))
                .andExpect(jsonPath("$.data.website", is("https://techcorp.com")))
                .andExpect(jsonPath("$.data.address", is("123 Silicon Valley Road")))
                .andExpect(jsonPath("$.data.totalJobs", is(0)));
    }

    @Test
    @DisplayName("Should fail to create company when name is blank")
    void shouldFailWhenNameIsBlank() throws Exception {
        CreateCompanyRequest request = CreateCompanyRequest.builder()
                .companyName("") // blank
                .email("contact@techcorp.com")
                .build();

        mockMvc.perform(post("/api/v1/companies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Validation failed. Please check the errors field.")))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field", is("companyName")))
                .andExpect(jsonPath("$.errors[0].message", is("Company name is required")));
    }

    @Test
    @DisplayName("Should fail to create company when email or phone formats are invalid")
    void shouldFailWhenFormatsAreInvalid() throws Exception {
        CreateCompanyRequest request = CreateCompanyRequest.builder()
                .companyName("Invalid Corp")
                .email("not-an-email")
                .phone("abc-12345") // non-numeric / invalid format
                .website("invalid-url") // invalid URL format
                .build();

        mockMvc.perform(post("/api/v1/companies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors", hasSize(2))); // email + phone (website URL pattern removed)
    }

    @Test
    @DisplayName("Should fail to create company when name already exists")
    void shouldFailWhenNameExists() throws Exception {
        Company existingCompany = new Company();
        existingCompany.setName("Duplicate Inc");
        existingCompany.setIsDeleted(false);
        companyRepository.saveAndFlush(existingCompany);

        CreateCompanyRequest request = CreateCompanyRequest.builder()
                .companyName("Duplicate Inc")
                .build();

        mockMvc.perform(post("/api/v1/companies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Company already exists with name: 'Duplicate Inc'")));
    }

    @Test
    @DisplayName("Should retrieve company by ID successfully")
    void shouldRetrieveCompanyById() throws Exception {
        Company company = new Company();
        company.setName("Specialty Co");
        company.setEmail("hello@specialty.co");
        company.setHeadquartersLocation("Chicago");
        company.setIsDeleted(false);
        company = companyRepository.saveAndFlush(company);

        mockMvc.perform(get("/api/v1/companies/{id}", company.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.companyName", is("Specialty Co")))
                .andExpect(jsonPath("$.data.address", is("Chicago")));
    }

    @Test
    @DisplayName("Should return 404 when company does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/companies/{id}", randomId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Company not found with id: '" + randomId + "'")));
    }

    @Test
    @DisplayName("Should update company successfully")
    void shouldUpdateCompanySuccessfully() throws Exception {
        Company company = new Company();
        company.setName("Old Name Corp");
        company.setEmail("old@corp.com");
        company.setIsDeleted(false);
        company = companyRepository.saveAndFlush(company);

        UpdateCompanyRequest request = UpdateCompanyRequest.builder()
                .companyName("New Name Corp")
                .email("new@corp.com")
                .phone("+987654321")
                .website("https://newcorp.com")
                .address("New Address")
                .description("Updated description")
                .build();

        mockMvc.perform(put("/api/v1/companies/{id}", company.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.companyName", is("New Name Corp")))
                .andExpect(jsonPath("$.data.email", is("new@corp.com")));
    }

    @Test
    @DisplayName("Should retrieve paginated list of companies and filter by search query")
    void shouldRetrievePaginatedCompaniesWithSearch() throws Exception {
        Company comp1 = new Company();
        comp1.setName("Alpha Tech");
        comp1.setEmail("alpha@test.com");
        comp1.setHeadquartersLocation("Austin");
        comp1.setIsDeleted(false);
        companyRepository.save(comp1);

        Company comp2 = new Company();
        comp2.setName("Beta Fin");
        comp2.setEmail("beta@test.com");
        comp2.setHeadquartersLocation("Boston");
        comp2.setIsDeleted(false);
        companyRepository.save(comp2);

        companyRepository.flush();

        // Search matches Boston (Boston/Beta/beta@test.com)
        mockMvc.perform(get("/api/v1/companies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .param("search", "Boston")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].companyName", is("Beta Fin")));

        // Search matches Tech (Alpha Tech)
        mockMvc.perform(get("/api/v1/companies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken)
                        .param("search", "Tech"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].companyName", is("Alpha Tech")));
    }

    @Test
    @DisplayName("Should soft delete company successfully when there are no active jobs")
    void shouldSoftDeleteCompany() throws Exception {
        Company company = new Company();
        company.setName("To Delete Ltd");
        company.setIsDeleted(false);
        company = companyRepository.saveAndFlush(company);

        mockMvc.perform(delete("/api/v1/companies/{id}", company.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Company deleted successfully")));

        // Verify company is marked as deleted
        Company deletedCompany = companyRepository.findById(company.getId()).orElse(null);
        org.junit.jupiter.api.Assertions.assertNotNull(deletedCompany);
        org.junit.jupiter.api.Assertions.assertTrue(deletedCompany.getIsDeleted());
    }

    @Test
    @DisplayName("Should reject company deletion with 422 when company has active job postings")
    void shouldRejectDeletionWhenCompanyHasActiveJobs() throws Exception {
        Company company = new Company();
        company.setName("Active Jobs Ltd");
        company.setIsDeleted(false);
        company = companyRepository.saveAndFlush(company);

        // Seed an OPEN job posting
        Job job = new Job();
        job.setCompany(company);
        job.setCreatedByUser(recruiter);
        job.setTitle("Senior Dev");
        job.setDescription("Job description");
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setStatus(JobStatus.OPEN);
        job.setIsDeleted(false);
        jobRepository.saveAndFlush(job);

        mockMvc.perform(delete("/api/v1/companies/{id}", company.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recruiterToken))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Cannot delete company with active job postings")));
    }
}
