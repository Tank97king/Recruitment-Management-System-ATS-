package com.example.ats.repository;

import com.example.ats.entity.CandidateTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link CandidateTag} entity.
 *
 * <p>In most cases, tags are managed via the {@link CandidateRepository}
 * through the {@code Candidate.tags} collection (cascade). This repository
 * is available for direct tag queries when needed.
 */
@Repository
public interface CandidateTagRepository extends JpaRepository<CandidateTag, UUID> {

    /**
     * Finds all tags for a specific candidate.
     *
     * @param candidateId the candidate's UUID
     * @return list of all tags belonging to this candidate
     */
    List<CandidateTag> findByCandidateId(UUID candidateId);

    /**
     * Checks whether a candidate already has a specific tag (case-insensitive).
     * Used to prevent duplicate tags before calling {@code candidate.addTag()}.
     *
     * @param candidateId the candidate's UUID
     * @param tag         the tag value to check (case-insensitive)
     * @return true if the candidate already has this tag
     */
    boolean existsByCandidateIdAndTagIgnoreCase(UUID candidateId, String tag);

    /**
     * Deletes all tags for a specific candidate.
     * Used when replacing all tags in bulk (clear-and-re-add pattern).
     *
     * @param candidateId the candidate's UUID
     */
    void deleteByCandidateId(UUID candidateId);
}
