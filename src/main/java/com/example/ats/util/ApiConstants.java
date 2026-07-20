package com.example.ats.util;

/**
 * Constants holding API paths and version prefixes.
 */
public final class ApiConstants {

    private ApiConstants() {
        // Prevent instantiation
    }

    public static final String API_V1 = "/api/v1";

    public static final String AUTH = API_V1 + "/auth";
    public static final String USERS = API_V1 + "/users";
    public static final String COMPANIES = API_V1 + "/companies";
    public static final String JOBS = API_V1 + "/jobs";
    public static final String CANDIDATES = API_V1 + "/candidates";
    public static final String APPLICATIONS = API_V1 + "/job-applications";
    public static final String INTERVIEWS = API_V1 + "/interviews";
    public static final String DASHBOARD = API_V1 + "/dashboard";
    public static final String SEARCH = API_V1 + "/search";
    public static final String PIPELINE = API_V1 + "/pipeline";
    public static final String AUDIT_LOGS = API_V1 + "/audit-logs";
}
