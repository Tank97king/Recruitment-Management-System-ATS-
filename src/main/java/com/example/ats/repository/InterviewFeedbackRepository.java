package com.example.ats.repository;

import com.example.ats.entity.InterviewFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link InterviewFeedback} entity.
 */
@Repository
public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedback, UUID> {

    /**
     * Finds feedback associated with a specific interview.
     * Enforces the One-to-One relationship semantics.
     *
     * @param interviewId the interview UUID
     * @return an Optional containing the feedback if found
     */
    Optional<InterviewFeedback> findByInterviewId(UUID interviewId);

    /**
     * Checks if feedback has already been submitted for a specific interview.
     *
     * @param interviewId the interview UUID
     * @return true if feedback exists
     */
    boolean existsByInterviewId(UUID interviewId);
}
