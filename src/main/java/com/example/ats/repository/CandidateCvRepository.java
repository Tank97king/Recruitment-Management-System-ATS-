package com.example.ats.repository;

import com.example.ats.entity.CandidateCv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link CandidateCv} entity.
 */
@Repository
public interface CandidateCvRepository extends JpaRepository<CandidateCv, UUID> {

    /**
     * Finds CV metadata associated with a given candidate UUID.
     *
     * @param candidateId the candidate's UUID
     * @return an optional containing the CV metadata if found
     */
    Optional<CandidateCv> findByCandidateId(UUID candidateId);
}
