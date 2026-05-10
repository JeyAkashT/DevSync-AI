package com.devsync.ai.pm.spec;

import com.devsync.ai.model.pm.PmBug;
import com.devsync.ai.model.pm.PmBugSeverity;
import com.devsync.ai.model.pm.PmBugStatus;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class PmBugSpecifications {

    private PmBugSpecifications() {}

    public static Specification<PmBug> inProject(UUID projectId) {
        return (root, q, cb) -> cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<PmBug> hasStatus(PmBugStatus status) {
        if (status == null) {
            return null;
        }
        return (root, q, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<PmBug> hasSeverity(PmBugSeverity severity) {
        if (severity == null) {
            return null;
        }
        return (root, q, cb) -> cb.equal(root.get("severity"), severity);
    }

    public static Specification<PmBug> matchesSearch(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String needle = "%" + raw.trim().toLowerCase() + "%";
        return (root, q, cb) -> {
            Expression<String> title = cb.lower(root.get("title"));
            Expression<String> desc = cb.lower(cb.coalesce(root.get("description"), ""));
            return cb.or(cb.like(title, needle), cb.like(desc, needle));
        };
    }
}
