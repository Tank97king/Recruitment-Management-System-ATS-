package com.example.ats.service.impl;

import com.example.ats.dto.request.CreateJobRequest;
import com.example.ats.dto.request.UpdateJobRequest;
import com.example.ats.dto.response.JobResponse;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.entity.Company;
import com.example.ats.entity.Job;
import com.example.ats.entity.User;
import com.example.ats.enums.EmploymentType;
import com.example.ats.enums.ExperienceLevel;
import com.example.ats.enums.JobStatus;
import com.example.ats.exception.BusinessRuleViolationException;
import com.example.ats.exception.InvalidDeadlineException;
import com.example.ats.exception.InvalidSalaryException;
import com.example.ats.exception.ResourceNotFoundException;
import com.example.ats.repository.CompanyRepository;
import com.example.ats.repository.JobRepository;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.JobService;
import com.example.ats.service.AuditLogService;
import com.example.ats.util.JobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Implementation of {@link JobService} for managing job postings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public JobResponse createJob(CreateJobRequest request, String userEmail) {
        log.info("Creating job posting: {}", request.getTitle());

        User user = userRepository.findByEmailAndIsDeletedFalse(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        Company company = companyRepository.findByIdAndIsDeletedFalse(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));

        // Validations
        validateSalary(request.getSalaryMin(), request.getSalaryMax());
        validateDeadline(request.getDeadline());

        if (request.getEmploymentType() == null || request.getEmploymentType().trim().isEmpty()) {
            throw new BusinessRuleViolationException("Employment type is required");
        }

        Job job = jobMapper.toEntity(request);
        job.setCompany(company);
        job.setCreatedByUser(user);
        job.setEmploymentType(parseEmploymentType(request.getEmploymentType()));
        job.setExperienceLevel(parseExperienceLevel(request.getExperienceLevel()));
        job.setStatus(JobStatus.OPEN); // Default status on creation is OPEN

        Job savedJob = jobRepository.save(job);
        log.info("Job created successfully with ID: {}", savedJob.getId());

        auditLogService.logAction(user.getId(), user.getEmail(), "JOB_CREATE", "JOB", savedJob.getId().toString(), "Created job posting: " + savedJob.getTitle());

        return jobMapper.toResponse(savedJob);
    }

    @Override
    public PageResponse<JobResponse> getJobs(
            String keyword,
            UUID companyId,
            String location,
            String employmentTypeStr,
            String experienceLevelStr,
            String statusStr,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            LocalDate deadlineFrom,
            LocalDate deadlineTo,
            Pageable pageable
    ) {
        log.info("Fetching jobs list with Specifications. Keyword: {}, CompanyId: {}, Location: {}, Status: {}",
                keyword, companyId, location, statusStr);

        // Validation for query parameter ranges
        if (salaryMin != null && salaryMax != null && salaryMin.compareTo(salaryMax) > 0) {
            throw new InvalidSalaryException("Minimum salary filter cannot exceed maximum salary filter");
        }
        if (deadlineFrom != null && deadlineTo != null && deadlineFrom.isAfter(deadlineTo)) {
            throw new InvalidDeadlineException("Deadline 'from' date filter cannot be after 'to' date filter");
        }

        JobStatus status = parseJobStatus(statusStr);
        EmploymentType employmentType = parseEmploymentType(employmentTypeStr);
        ExperienceLevel experienceLevel = parseExperienceLevel(experienceLevelStr);

        org.springframework.data.jpa.domain.Specification<Job> spec = 
                com.example.ats.repository.specification.JobSpecification.filterJobs(
                        keyword, companyId, location, employmentType, experienceLevel, status,
                        salaryMin, salaryMax, deadlineFrom, deadlineTo
                );

        Page<Job> jobsPage = jobRepository.findAll(spec, pageable);
        Page<JobResponse> dtoPage = jobsPage.map(jobMapper::toResponse);
        return PageResponse.from(dtoPage);
    }

    @Override
    public JobResponse getJobById(UUID id) {
        log.info("Fetching job by ID: {}", id);
        
        Job job = jobRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));
                
        return jobMapper.toResponse(job);
    }

    @Override
    @Transactional
    public JobResponse updateJob(UUID id, UpdateJobRequest request) {
        log.info("Updating job with ID: {}", id);

        Job job = jobRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));

        // Enforce: A CLOSED job cannot be updated unless reopened
        if (job.getStatus() == JobStatus.CLOSED) {
            if (request.getStatus() == null || !request.getStatus().equalsIgnoreCase("OPEN")) {
                throw new BusinessRuleViolationException("A CLOSED job cannot be updated unless reopened.");
            }
        }

        Company company = companyRepository.findByIdAndIsDeletedFalse(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));

        // Validations
        validateSalary(request.getSalaryMin(), request.getSalaryMax());
        validateDeadline(request.getDeadline());

        if (request.getEmploymentType() == null || request.getEmploymentType().trim().isEmpty()) {
            throw new BusinessRuleViolationException("Employment type is required");
        }

        jobMapper.updateEntityFromRequest(request, job);
        job.setCompany(company);
        job.setEmploymentType(parseEmploymentType(request.getEmploymentType()));
        job.setExperienceLevel(parseExperienceLevel(request.getExperienceLevel()));

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            job.setStatus(parseJobStatus(request.getStatus()));
        }

        Job updatedJob = jobRepository.save(job);
        log.info("Job updated successfully with ID: {}", updatedJob.getId());

        auditLogService.logAction("JOB_UPDATE", "JOB", updatedJob.getId().toString(), "Updated job posting: " + updatedJob.getTitle());

        return jobMapper.toResponse(updatedJob);
    }

    @Override
    @Transactional
    public void deleteJob(UUID id) {
        log.info("Deleting job with ID: {}", id);
        
        Job job = jobRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));

        job.setIsDeleted(true);
        jobRepository.save(job);

        auditLogService.logAction("JOB_DELETE", "JOB", id.toString(), "Deleted job posting: " + job.getTitle());
        log.info("Job with ID: {} soft-deleted successfully", id);
    }

    private void validateSalary(BigDecimal min, BigDecimal max) {
        if (min != null && min.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidSalaryException("Salary cannot be negative");
        }
        if (max != null && max.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidSalaryException("Salary cannot be negative");
        }
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new InvalidSalaryException("Minimum salary cannot be greater than maximum salary");
        }
    }

    private void validateDeadline(LocalDate deadline) {
        if (deadline != null && !deadline.isAfter(LocalDate.now())) {
            throw new InvalidDeadlineException("Deadline must be after the current date");
        }
    }

    private EmploymentType parseEmploymentType(String val) {
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        try {
            return EmploymentType.valueOf(val.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleViolationException("Invalid employment type: " + val);
        }
    }

    private ExperienceLevel parseExperienceLevel(String val) {
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        try {
            return ExperienceLevel.valueOf(val.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleViolationException("Invalid experience level: " + val);
        }
    }

    private JobStatus parseJobStatus(String val) {
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        try {
            return JobStatus.valueOf(val.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleViolationException("Invalid job status: " + val);
        }
    }
}
