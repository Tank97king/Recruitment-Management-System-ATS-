-- =============================================================================
--  ATS System — Flyway Baseline Migration
--  Version : V0
--  Author  : ATS Dev Team
--  Date    : 2026-07-19
--
--  Purpose : Baseline migration to initialize the Flyway migration history.
--            This script runs first and acts as the starting point for all
--            subsequent schema versioned migrations.
--
--            Real table DDL begins at V1__create_users_table.sql (Phase 1).
-- =============================================================================

-- This migration intentionally contains no DDL.
-- It establishes V0 as the baseline so Flyway tracks subsequent migrations.
-- Add table creation scripts starting from V1__ in the next implementation phase.

SELECT 1; -- No-op statement; required for Flyway to mark this as applied.
