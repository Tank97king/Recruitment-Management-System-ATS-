package com.example.ats.repository.specification;

import com.example.ats.entity.Company;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA specifications for dynamic filtering and global searching of {@link Company} entities.
 */
public class CompanySpecification {

    /**
     * Constructs a specification for global company search.
     * Searches by company name, email, and headquarters location (address).
     *
     * @param keyword the search term
     * @return the JPA specification
     */
    public static Specification<Company> globalSearch(String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude soft-deleted records
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), pattern);
                Predicate locationMatch = cb.like(cb.lower(root.get("headquartersLocation")), pattern);
                predicates.add(cb.or(nameMatch, emailMatch, locationMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
