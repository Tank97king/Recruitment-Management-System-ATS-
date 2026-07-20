package com.example.ats.repository.specification;

import com.example.ats.entity.Candidate;
import com.example.ats.entity.CandidateTag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA specifications for dynamic filtering and global searching of {@link Candidate} entities.
 */
public class CandidateSpecification {

    /**
     * Constructs a specification for global candidate search.
     * Searches by full name, email, phone, and skill tags.
     *
     * @param keyword the search term
     * @return the JPA specification
     */
    public static Specification<Candidate> globalSearch(String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude soft-deleted records
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";

                // Full name matching
                Predicate firstNameMatch = cb.like(cb.lower(root.get("firstName")), pattern);
                Predicate lastNameMatch = cb.like(cb.lower(root.get("lastName")), pattern);
                Predicate fullNameMatch = cb.like(
                        cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))),
                        pattern
                );

                Predicate emailMatch = cb.like(cb.lower(root.get("email")), pattern);
                Predicate phoneMatch = cb.like(root.get("phone"), pattern);

                // Skill tags matching
                Join<Candidate, CandidateTag> tagsJoin = root.join("tags", JoinType.LEFT);
                Predicate skillMatch = cb.like(cb.lower(tagsJoin.get("tag")), pattern);

                if (query != null) {
                    query.distinct(true);
                }

                predicates.add(cb.or(firstNameMatch, lastNameMatch, fullNameMatch, emailMatch, phoneMatch, skillMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
