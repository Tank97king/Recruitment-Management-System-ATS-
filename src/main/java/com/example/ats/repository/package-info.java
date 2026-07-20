/**
 * Repository layer — Data Access Objects.
 *
 * <p>Contains Spring Data JPA repository interfaces that extend
 * {@code JpaRepository<Entity, ID>} or {@code PagingAndSortingRepository}.
 *
 * <p>Spring Data automatically generates the implementation at runtime.
 * Custom queries are added via:
 * <ul>
 *   <li>{@code @Query} annotation with JPQL or native SQL</li>
 *   <li>Method name derivation (e.g., {@code findByEmail}, {@code existsByEmailAndDeletedFalse})</li>
 *   <li>Spring Data {@code Specification} for dynamic complex queries</li>
 * </ul>
 *
 * <p>Repositories are injected into Service implementations only.
 * They are never called directly from Controllers.
 */
package com.example.ats.repository;
