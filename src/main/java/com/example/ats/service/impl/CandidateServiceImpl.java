package com.example.ats.service.impl;

import com.example.ats.dto.request.CreateCandidateRequest;
import com.example.ats.dto.request.UpdateCandidateRequest;
import com.example.ats.dto.response.CandidateResponse;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.entity.Candidate;
import com.example.ats.entity.CandidateTag;
import com.example.ats.exception.BusinessRuleViolationException;
import com.example.ats.exception.DuplicateResourceException;
import com.example.ats.exception.ResourceNotFoundException;
import com.example.ats.repository.CandidateRepository;
import com.example.ats.repository.JobApplicationRepository;
import com.example.ats.service.CandidateService;
import com.example.ats.service.AuditLogService;
import com.example.ats.util.CandidateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import com.example.ats.service.FileStorageService;
import org.springframework.web.multipart.MultipartFile;
import com.example.ats.entity.CandidateCv;

/**
 * Service implementation for managing candidates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CandidateMapper candidateMapper;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public CandidateResponse createCandidate(CreateCandidateRequest request) {
        log.info("Creating candidate profile with email: {}", request.getEmail());

        if (candidateRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new DuplicateResourceException("Candidate", "email", request.getEmail());
        }

        Candidate candidate = candidateMapper.toEntity(request);
        parseAndSetFullName(request.getFullName(), candidate);

        // Map and link skills to candidate tags
        if (request.getSkills() != null) {
            for (String skill : request.getSkills()) {
                candidate.addTag(new CandidateTag(candidate, skill));
            }
        }

        Candidate savedCandidate = candidateRepository.save(candidate);
        log.info("Candidate created successfully with ID: {}", savedCandidate.getId());

        auditLogService.logAction("CANDIDATE_CREATE", "CANDIDATE", savedCandidate.getId().toString(), "Created candidate profile for: " + savedCandidate.getEmail());

        return candidateMapper.toResponse(savedCandidate);
    }

    @Override
    public PageResponse<CandidateResponse> getCandidates(String search, Pageable pageable) {
        log.info("Fetching candidates list. Search: {}, Pageable: {}", search, pageable);
        
        String searchKeyword = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<Candidate> candidatesPage = candidateRepository.findAllActiveWithSearch(searchKeyword, pageable);
        
        Page<CandidateResponse> dtoPage = candidatesPage.map(candidateMapper::toResponse);
        return PageResponse.from(dtoPage);
    }

    @Override
    public CandidateResponse getCandidateById(UUID id) {
        log.info("Fetching candidate profile by ID: {}", id);
        
        Candidate candidate = candidateRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", id));
                
        return candidateMapper.toResponse(candidate);
    }

    @Override
    @Transactional
    public CandidateResponse updateCandidate(UUID id, UpdateCandidateRequest request) {
        log.info("Updating candidate profile with ID: {}", id);

        Candidate candidate = candidateRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", id));

        // Check email uniqueness if email has changed
        if (!candidate.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (candidateRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
                throw new DuplicateResourceException("Candidate", "email", request.getEmail());
            }
        }

        candidateMapper.updateEntityFromRequest(request, candidate);
        parseAndSetFullName(request.getFullName(), candidate);

        // Rebuild and update skills
        candidate.getTags().clear();
        if (request.getSkills() != null) {
            for (String skill : request.getSkills()) {
                candidate.addTag(new CandidateTag(candidate, skill));
            }
        }

        Candidate updatedCandidate = candidateRepository.save(candidate);
        log.info("Candidate updated successfully with ID: {}", updatedCandidate.getId());

        auditLogService.logAction("CANDIDATE_UPDATE", "CANDIDATE", updatedCandidate.getId().toString(), "Updated candidate profile for: " + updatedCandidate.getEmail());

        return candidateMapper.toResponse(updatedCandidate);
    }

    @Override
    @Transactional
    public void deleteCandidate(UUID id) {
        log.info("Deleting candidate profile with ID: {}", id);

        Candidate candidate = candidateRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", id));

        // Check if candidate has active job applications
        if (jobApplicationRepository.existsByCandidateIdAndIsDeletedFalse(id)) {
            throw new BusinessRuleViolationException("Cannot delete candidate with active job applications");
        }

        candidate.setIsDeleted(true);
        candidateRepository.save(candidate);

        auditLogService.logAction("CANDIDATE_DELETE", "CANDIDATE", id.toString(), "Deleted candidate profile for: " + candidate.getEmail());
        log.info("Candidate with ID: {} soft-deleted successfully", id);
    }

    private void parseAndSetFullName(String fullName, Candidate candidate) {
        if (fullName == null) return;
        String trimmed = fullName.trim();
        int spaceIndex = trimmed.indexOf(' ');
        if (spaceIndex == -1) {
            candidate.setFirstName(trimmed);
            candidate.setLastName("");
        } else {
            candidate.setFirstName(trimmed.substring(0, spaceIndex).trim());
            candidate.setLastName(trimmed.substring(spaceIndex + 1).trim());
        }
    }

    @Override
    @Transactional
    public CandidateResponse uploadCv(UUID candidateId, MultipartFile file) {
        log.info("Uploading CV for candidate: {}", candidateId);
        
        Candidate candidate = candidateRepository.findByIdAndIsDeletedFalse(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", candidateId));

        // Enforce replacement rule: If CV exists, delete physical file first
        if (candidate.getCv() != null) {
            CandidateCv oldCv = candidate.getCv();
            log.info("Replacing existing CV for candidate. Deleting old file: {}", oldCv.getFilePath());
            fileStorageService.deleteFile(oldCv.getFilePath());
            candidate.setCv(null);
            candidateRepository.saveAndFlush(candidate); // trigger orphan removal
        }

        // Store new file on disk
        String originalFileName = file.getOriginalFilename();
        String storedFileName = fileStorageService.generateUniqueFileName(originalFileName);
        String subFolder = "cv/" + candidateId.toString();
        String filePath = fileStorageService.storeFile(file, subFolder, storedFileName);

        // Store CV metadata
        CandidateCv cv = new CandidateCv();
        cv.setCandidate(candidate);
        cv.setOriginalFileName(originalFileName);
        cv.setStoredFileName(storedFileName);
        cv.setFilePath(filePath);
        cv.setFileSize(file.getSize());
        cv.setContentType(file.getContentType());
        cv.setUploadedAt(java.time.Instant.now());

        candidate.setCv(cv);
        Candidate updatedCandidate = candidateRepository.save(candidate);
        log.info("Successfully uploaded CV for candidate: {}. Path: {}", candidateId, filePath);
        return candidateMapper.toResponse(updatedCandidate);
    }

    @Override
    public CandidateCv getCvMetadata(UUID candidateId) {
        log.info("Retrieving CV metadata for candidate: {}", candidateId);
        
        Candidate candidate = candidateRepository.findByIdAndIsDeletedFalse(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", candidateId));

        if (candidate.getCv() == null) {
            throw new ResourceNotFoundException("CV not found for candidate with ID: " + candidateId);
        }
        return candidate.getCv();
    }

    @Override
    @Transactional
    public void deleteCv(UUID candidateId) {
        log.info("Deleting CV for candidate: {}", candidateId);
        
        Candidate candidate = candidateRepository.findByIdAndIsDeletedFalse(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", candidateId));

        if (candidate.getCv() == null) {
            throw new ResourceNotFoundException("CV not found for candidate with ID: " + candidateId);
        }

        CandidateCv cv = candidate.getCv();
        fileStorageService.deleteFile(cv.getFilePath());

        candidate.setCv(null);
        candidateRepository.save(candidate);
        log.info("CV metadata and file deleted successfully for candidate: {}", candidateId);
    }
}
