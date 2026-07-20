package com.example.ats.service;

import com.example.ats.dto.request.CreateInterviewRequest;
import com.example.ats.entity.Candidate;
import com.example.ats.entity.Company;
import com.example.ats.entity.Job;
import com.example.ats.entity.JobApplication;
import com.example.ats.enums.ApplicationStatus;
import com.example.ats.enums.EmploymentType;
import com.example.ats.enums.JobStatus;
import com.example.ats.entity.User;
import com.example.ats.repository.CandidateRepository;
import com.example.ats.repository.CompanyRepository;
import com.example.ats.repository.JobApplicationRepository;
import com.example.ats.repository.JobRepository;
import com.example.ats.repository.UserRepository;
import com.example.ats.util.EmailTemplateUtil;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Email Service Integration Tests")
class EmailServiceIntegrationTests {

    @Autowired
    private EmailService emailService;

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @MockBean
    private JavaMailSender mailSender;

    private JobApplication application;

    @BeforeEach
    void setUp() {
        MimeMessage dummyMessage = Mockito.mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(dummyMessage);

        User recruiter = new User();
        recruiter.setEmail("recruiter.mail@example.com");
        recruiter.setPasswordHash("password");
        recruiter.setFirstName("Recruiter");
        recruiter.setLastName("Bob");
        recruiter.setStatus(com.example.ats.enums.UserStatus.ACTIVE);
        recruiter = userRepository.saveAndFlush(recruiter);

        Company company = new Company();
        company.setName("Acme Mail Corp");
        company.setIsDeleted(false);
        company = companyRepository.saveAndFlush(company);

        Job job = new Job();
        job.setTitle("Email Engineer");
        job.setDescription("Mail service role");
        job.setCompany(company);
        job.setCreatedByUser(recruiter);
        job.setStatus(JobStatus.OPEN);
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setIsDeleted(false);
        job = jobRepository.saveAndFlush(job);

        Candidate candidate = new Candidate();
        candidate.setFirstName("Sarah");
        candidate.setLastName("Connor");
        candidate.setEmail("sarah.connor@example.com");
        candidate.setIsDeleted(false);
        candidate = candidateRepository.saveAndFlush(candidate);

        application = new JobApplication();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setStatus(ApplicationStatus.INTERVIEW);
        application.setIsDeleted(false);
        application = jobApplicationRepository.saveAndFlush(application);
    }

    @Test
    @DisplayName("Should build HTML templates with logo, greeting, and content")
    void shouldBuildHtmlTemplates() {
        String templateHtml = EmailTemplateUtil.buildInterviewInvitationTemplate(
                "Sarah Connor", "Email Engineer", "Acme Mail Corp",
                "2026-07-20 10:00", "ONLINE", "https://meet.google.com/abc", "Recruiter Bob"
        );

        assertNotNull(templateHtml);
        assertTrue(templateHtml.contains("LOGO &bull; ACME MAIL CORP"));
        assertTrue(templateHtml.contains("Dear Sarah Connor,"));
        assertTrue(templateHtml.contains("Interview Invitation"));
        assertTrue(templateHtml.contains("https://meet.google.com/abc"));
    }

    @Test
    @DisplayName("Should send email without throwing exception when mailSender succeeds")
    void shouldSendEmailSuccessfully() {
        assertDoesNotThrow(() -> emailService.sendInterviewInvitation(
                "sarah.connor@example.com", "Sarah Connor", "Email Engineer", "Acme Mail Corp",
                "2026-07-20 10:00", "ONLINE", "https://meet.google.com/abc", "Recruiter Bob"
        ));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should suppress MailException and log error without throwing exception")
    void shouldSuppressMailExceptionAndNotFailBusinessOperation() {
        doThrow(new MailSendException("SMTP connection refused"))
                .when(mailSender).send(any(MimeMessage.class));

        CreateInterviewRequest request = CreateInterviewRequest.builder()
                .jobApplicationId(application.getId())
                .interviewDate(LocalDateTime.now().plusDays(2))
                .interviewType("ONLINE")
                .interviewerName("Recruiter Bob")
                .interviewerEmail("bob@example.com")
                .meetingLink("https://meet.google.com/test-fail")
                .build();

        // Business operation (scheduling interview) should succeed despite mail failure
        assertDoesNotThrow(() -> interviewService.scheduleInterview(request));
    }
}
