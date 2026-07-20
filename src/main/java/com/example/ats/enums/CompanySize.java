package com.example.ats.enums;

/**
 * Categorizes companies by their approximate headcount.
 *
 * <p>Used on the {@code Company} entity to allow filtering of companies
 * by size bracket. Values are intentionally broad ranges to accommodate
 * self-reported company data.
 */
public enum CompanySize {

    /** 1–10 employees. Early-stage company. */
    STARTUP,

    /** 11–50 employees. */
    SMALL,

    /** 51–250 employees. */
    MEDIUM,

    /** 251–1,000 employees. */
    LARGE,

    /** 1,000+ employees. Multinational or publicly listed corporation. */
    ENTERPRISE
}
