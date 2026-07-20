package com.example.ats.service;

/**
 * Service interface for dispatching recruitment email notifications.
 */
public interface EmailService {

    /**
     * Sends an interview invitation email to the candidate.
     */
    void sendInterviewInvitation(
            String recipientEmail,
            String candidateName,
            String jobTitle,
            String companyName,
            String interviewDate,
            String interviewType,
            String meetingLocationOrLink,
            String recruiterName
    );

    /**
     * Sends an application status update notification to the candidate.
     */
    void sendApplicationStatusUpdate(
            String recipientEmail,
            String candidateName,
            String jobTitle,
            String companyName,
            String newStatus
    );

    /**
     * Sends an official job offer notification to the candidate.
     */
    void sendOfferNotification(
            String recipientEmail,
            String candidateName,
            String jobTitle,
            String companyName
    );

    /**
     * Sends a job application rejection notification to the candidate.
     */
    void sendRejectionNotification(
            String recipientEmail,
            String candidateName,
            String jobTitle,
            String companyName
    );
}
