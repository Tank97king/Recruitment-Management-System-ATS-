package com.example.ats.controller;

import com.example.ats.entity.Candidate;
import com.example.ats.entity.CandidateTag;
import com.example.ats.entity.Company;
import com.example.ats.entity.Job;
import com.example.ats.entity.Role;
import com.example.ats.entity.User;
import com.example.ats.enums.EmploymentType;
import com.example.ats.enums.JobStatus;
import com.example.ats.enums.RoleName;
import com.example.ats.enums.UserStatus;
import com.example.ats.repository.CandidateRepository;
import com.example.ats.repository.CompanyRepository;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Global Search Controller Integration Tests")
class SearchControllerIntegrationTests {

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
    private JwtService jwtService;

    private User recruiter;
    private String token;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        companyRepository.deleteAll();
        candidateRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role recruiterRole = roleRepository.findByName(RoleName.RECRUITER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.RECRUITER, "Recruiter")));
        recruiter = new User();
        recruiter.setEmail("search.recruiter@example.com");
        recruiter.setPasswordHash("password");
        recruiter.setFirstName("Search");
        recruiter.setLastName("User");
        recruiter.setStatus(UserStatus.ACTIVE);
        recruiter.addRole(recruiterRole);
        recruiter = userRepository.saveAndFlush(recruiter);
        token = jwtService.generateAccessToken(recruiter);

        // Seed Company matching "Alpha"
        Company c1 = new Company();
        c1.setName("Alpha Technologies");
        c1.setEmail("contact@alphatech.com");
        c1.setHeadquartersLocation("San Francisco, CA");
        c1.setIsDeleted(false);
        c1 = companyRepository.saveAndFlush(c1);

        // Seed Job matching "Alpha"
        Job j1 = new Job();
        j1.setTitle("Alpha Developer");
        j1.setDescription("Leading core development");
        j1.setLocation("Remote");
        j1.setCompany(c1);
        j1.setCreatedByUser(recruiter);
        j1.setStatus(JobStatus.OPEN);
        j1.setEmploymentType(EmploymentType.FULL_TIME);
        j1.setIsDeleted(false);
        jobRepository.saveAndFlush(j1);

        // Seed Candidate matching "Alpha" via skill
        Candidate candidate = new Candidate();
        candidate.setFirstName("Bruce");
        candidate.setLastName("Wayne");
        candidate.setEmail("bruce@wayne.com");
        candidate.setPhone("+123456789");
        candidate.setIsDeleted(false);

        CandidateTag tag = new CandidateTag();
        tag.setTag("Alpha-Skill");
        candidate.addTag(tag);
        candidateRepository.saveAndFlush(candidate);
    }

    @Test
    @DisplayName("Should return 401 when token is missing")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/search").param("keyword", "Alpha"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 when keyword is blank")
    void shouldReturn400WhenKeywordIsBlank() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("keyword", "   ")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Search keyword is required and cannot be blank")));
    }

    @Test
    @DisplayName("Should return 400 when keyword exceeds 100 characters")
    void shouldReturn400WhenKeywordExceedsLimit() throws Exception {
        String longKeyword = "a".repeat(101);
        mockMvc.perform(get("/api/v1/search")
                        .param("keyword", longKeyword)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Search keyword cannot exceed 100 characters")));
    }

    @Test
    @DisplayName("Should return 400 when sort field is invalid")
    void shouldReturn400WhenSortFieldIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("keyword", "Alpha")
                        .param("sortBy", "invalidField")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid sort field: invalidField")));
    }

    @Test
    @DisplayName("Should return matching companies, jobs, and candidates for keyword 'Alpha'")
    void shouldReturnMatchingResultsForKeyword() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("keyword", "  Alpha  ")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.companies.totalResults", is(1)))
                .andExpect(jsonPath("$.data.companies.data.content[0].companyName", is("Alpha Technologies")))
                .andExpect(jsonPath("$.data.jobs.totalResults", is(1)))
                .andExpect(jsonPath("$.data.jobs.data.content[0].title", is("Alpha Developer")))
                .andExpect(jsonPath("$.data.candidates.totalResults", is(1)))
                .andExpect(jsonPath("$.data.candidates.data.content[0].fullName", is("Bruce Wayne")));
    }

    @Test
    @DisplayName("Should support pagination and sorting parameters")
    void shouldSupportPaginationAndSorting() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("keyword", "Alpha")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "DESC")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.companies.data.page", is(0)))
                .andExpect(jsonPath("$.data.companies.data.size", is(5)));
    }
}
