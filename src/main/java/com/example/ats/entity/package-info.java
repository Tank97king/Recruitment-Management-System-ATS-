/**
 * Entity layer — JPA-managed domain objects.
 *
 * <p>Contains {@code @Entity} classes mapped to PostgreSQL database tables.
 * Each entity represents a core domain concept (User, Job, Candidate, etc.).
 *
 * <p>All entities extend {@link com.example.ats.entity.base.BaseEntity} which
 * provides common auditing fields: {@code id}, {@code createdAt},
 * {@code updatedAt}, {@code createdBy}, {@code deleted}.
 *
 * <p>Entities must NOT be returned directly from controllers.
 * They must be mapped to DTOs via MapStruct mappers before leaving the service layer.
 */
package com.example.ats.entity;
