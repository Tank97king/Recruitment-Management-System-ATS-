package com.example.ats.util;

/**
 * Utility class for constructing reusable HTML email templates.
 *
 * <p>Separates HTML presentation logic from core business logic, adhering to the
 * Single Responsibility Principle (SRP).
 */
public final class EmailTemplateUtil {

    private EmailTemplateUtil() {
        // Utility class private constructor
    }

    /**
     * Builds an HTML template for interview invitations.
     */
    public static String buildInterviewInvitationTemplate(
            String candidateName,
            String jobTitle,
            String companyName,
            String interviewDate,
            String interviewType,
            String meetingLocationOrLink,
            String recruiterName
    ) {
        String locationOrLinkLabel = "ONLINE".equalsIgnoreCase(interviewType) || "VIDEO".equalsIgnoreCase(interviewType)
                ? "Meeting Link" : "Location";

        String contentHtml = String.format("""
                <p>We are pleased to invite you for an interview regarding your application for <strong>%s</strong> at <strong>%s</strong>.</p>
                <div style="background-color: #f8fafc; border-left: 4px solid #2563eb; padding: 15px; margin: 20px 0;">
                    <p style="margin: 5px 0;"><strong>Date & Time:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Format:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>%s:</strong> %s</p>
                    <p style="margin: 5px 0;"><strong>Scheduled By:</strong> %s</p>
                </div>
                <p>Please confirm your availability by replying to this email.</p>
                """,
                escapeHtml(jobTitle),
                escapeHtml(companyName),
                escapeHtml(interviewDate),
                escapeHtml(interviewType),
                locationOrLinkLabel,
                escapeHtml(meetingLocationOrLink),
                escapeHtml(recruiterName)
        );

        return wrapInBaseLayout("Interview Invitation", candidateName, companyName, contentHtml);
    }

    /**
     * Builds an HTML template for general application status updates.
     */
    public static String buildStatusUpdateTemplate(
            String candidateName,
            String jobTitle,
            String companyName,
            String newStatus
    ) {
        String contentHtml = String.format("""
                <p>There is an update on your job application for <strong>%s</strong> at <strong>%s</strong>.</p>
                <div style="background-color: #f8fafc; border-left: 4px solid #0284c7; padding: 15px; margin: 20px 0;">
                    <p style="margin: 5px 0;"><strong>New Application Status:</strong> <span style="color: #0284c7; font-weight: bold;">%s</span></p>
                </div>
                <p>Our hiring team will reach out with next steps as your application progresses.</p>
                """,
                escapeHtml(jobTitle),
                escapeHtml(companyName),
                escapeHtml(newStatus)
        );

        return wrapInBaseLayout("Application Status Update", candidateName, companyName, contentHtml);
    }

    /**
     * Builds an HTML template for job offer notifications.
     */
    public static String buildOfferNotificationTemplate(
            String candidateName,
            String jobTitle,
            String companyName
    ) {
        String contentHtml = String.format("""
                <p>Congratulations! We are thrilled to extend an official job offer for the position of <strong>%s</strong> at <strong>%s</strong>!</p>
                <div style="background-color: #f0fdf4; border-left: 4px solid #16a34a; padding: 15px; margin: 20px 0;">
                    <p style="margin: 5px 0; color: #15803d; font-weight: bold;">Offer Extended</p>
                    <p style="margin: 5px 0;">We were very impressed by your qualifications and experience during our recruitment process.</p>
                </div>
                <p>Our HR department will follow up shortly with formal offer documents and onboarding details.</p>
                """,
                escapeHtml(jobTitle),
                escapeHtml(companyName)
        );

        return wrapInBaseLayout("Job Offer Extended", candidateName, companyName, contentHtml);
    }

    /**
     * Builds an HTML template for job application rejection notifications.
     */
    public static String buildRejectionNotificationTemplate(
            String candidateName,
            String jobTitle,
            String companyName
    ) {
        String contentHtml = String.format("""
                <p>Thank you for giving us the opportunity to consider your application for <strong>%s</strong> at <strong>%s</strong>.</p>
                <div style="background-color: #fef2f2; border-left: 4px solid #dc2626; padding: 15px; margin: 20px 0;">
                    <p style="margin: 5px 0;">After careful review, we regret to inform you that we will not be moving forward with your application for this position.</p>
                </div>
                <p>We appreciate your interest in joining our team and wish you every success in your career endeavors.</p>
                """,
                escapeHtml(jobTitle),
                escapeHtml(companyName)
        );

        return wrapInBaseLayout("Application Update", candidateName, companyName, contentHtml);
    }

    private static String wrapInBaseLayout(String subject, String candidateName, String companyName, String contentHtml) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>%s</title>
                </head>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #334155; margin: 0; padding: 0; background-color: #f1f5f9;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);">
                        <!-- Header / Logo Placeholder -->
                        <tr>
                            <td style="background-color: #1e293b; padding: 25px; text-align: center; color: #ffffff;">
                                <div style="font-size: 24px; font-weight: bold; letter-spacing: 1px;">LOGO &bull; %s</div>
                            </td>
                        </tr>
                        <!-- Main Body -->
                        <tr>
                            <td style="padding: 30px;">
                                <p style="font-size: 16px; font-weight: bold; margin-top: 0;">Dear %s,</p>
                                %s
                                <!-- Closing -->
                                <div style="margin-top: 30px; border-top: 1px solid #e2e8f0; padding-top: 20px;">
                                    <p style="margin: 0; font-size: 14px; color: #64748b;">Best regards,</p>
                                    <p style="margin: 5px 0 0 0; font-size: 14px; font-weight: bold; color: #1e293b;">Recruitment Team at %s</p>
                                </div>
                            </td>
                        </tr>
                        <!-- Footer -->
                        <tr>
                            <td style="background-color: #f8fafc; padding: 15px; text-align: center; font-size: 12px; color: #94a3b8;">
                                &copy; ATS Recruitment Management System. All rights reserved.
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """,
                escapeHtml(subject),
                escapeHtml(companyName != null ? companyName.toUpperCase() : "COMPANY"),
                escapeHtml(candidateName),
                contentHtml,
                escapeHtml(companyName != null ? companyName : "Company")
        );
    }

    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
