package com.devsync.ai.pm.spec;

import com.devsync.ai.model.pm.PmTask;
import com.devsync.ai.model.pm.PmTaskStatus;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class PmTaskSpecifications {

    private PmTaskSpecifications() {}

    public static Specification<PmTask> inProject(UUID projectId) {
        return (root, q, cb) -> cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<PmTask> hasStatus(PmTaskStatus status) {
        if (status == null) {
            return null;
        }
        return (root, q, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<PmTask> hasAssignee(UUID assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return (root, q, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    public static Specification<PmTask> hasSprint(UUID sprintId) {
        if (sprintId == null) {
            return null;
        }
        return (root, q, cb) -> cb.equal(root.get("sprint").get("id"), sprintId);
    }

    public static Specification<PmTask> unassignedSprint() {
        return (root, q, cb) -> cb.isNull(root.get("sprint"));
    }

    public static Specification<PmTask> matchesSearch(String raw) {
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
