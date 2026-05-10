package com.devsync.ai.pm;

import com.devsync.ai.api.dto.pm.ActivityEntryDto;
import com.devsync.ai.api.dto.pm.BugDto;
import com.devsync.ai.api.dto.pm.CommentDto;
import com.devsync.ai.api.dto.pm.ProjectDetailResponse;
import com.devsync.ai.api.dto.pm.ProjectMemberResponse;
import com.devsync.ai.api.dto.pm.ProjectSummaryResponse;
import com.devsync.ai.api.dto.pm.SprintDto;
import com.devsync.ai.api.dto.pm.TaskDto;
import com.devsync.ai.model.pm.ActivityLog;
import com.devsync.ai.model.pm.PmBug;
import com.devsync.ai.model.pm.PmComment;
import com.devsync.ai.model.pm.PmTask;
import com.devsync.ai.model.pm.Project;
import com.devsync.ai.model.pm.ProjectMember;
import com.devsync.ai.model.pm.Sprint;

public final class PmMappings {

    private PmMappings() {}

    public static ProjectSummaryResponse toSummary(Project p) {
        return new ProjectSummaryResponse(
                p.getId(),
                p.getName(),
                p.getKey(),
                p.getStatus(),
                p.getOwner() != null ? p.getOwner().getId() : null,
                p.getOrganization().getId());
    }

    public static ProjectDetailResponse toDetail(
            Project p, long taskCount, long bugCount, boolean activeSprint) {
        return new ProjectDetailResponse(
                p.getId(),
                p.getOrganization().getId(),
                p.getName(),
                p.getKey(),
                p.getDescription(),
                p.getStatus(),
                p.getOwner() != null ? p.getOwner().getId() : null,
                p.getRepositoryUrl(),
                taskCount,
                bugCount,
                activeSprint);
    }

    public static ProjectMemberResponse toMember(ProjectMember pm) {
        return new ProjectMemberResponse(
                pm.getId(),
                pm.getUser().getId(),
                pm.getUser().getEmail(),
                pm.getUser().getFullName(),
                pm.getRole());
    }

    public static TaskDto toTask(PmTask t) {
        return new TaskDto(
                t.getId(),
                t.getProject().getId(),
                t.getSprint() != null ? t.getSprint().getId() : null,
                t.getTitle(),
                t.getDescription(),
                t.getPriority(),
                t.getStatus(),
                t.getAssignee() != null ? t.getAssignee().getId() : null,
                t.getDueDate(),
                t.getPosition(),
                t.getCreatedAt(),
                t.getUpdatedAt());
    }

    public static BugDto toBug(PmBug b) {
        return new BugDto(
                b.getId(),
                b.getProject().getId(),
                b.getTask() != null ? b.getTask().getId() : null,
                b.getTitle(),
                b.getDescription(),
                b.getSeverity(),
                b.getStatus(),
                b.getReporter().getId(),
                b.getAssignee() != null ? b.getAssignee().getId() : null,
                b.getCreatedAt(),
                b.getUpdatedAt());
    }

    public static SprintDto toSprint(Sprint s) {
        return new SprintDto(
                s.getId(),
                s.getProject().getId(),
                s.getName(),
                s.getStartDate(),
                s.getEndDate(),
                s.getGoal(),
                s.getStatus(),
                s.getCreatedAt(),
                s.getUpdatedAt());
    }

    public static CommentDto toComment(PmComment c) {
        return new CommentDto(
                c.getId(),
                c.getAuthor().getId(),
                c.getAuthor().getEmail(),
                c.getBody(),
                c.getParentComment() != null ? c.getParentComment().getId() : null,
                c.getCreatedAt());
    }

    public static ActivityEntryDto toActivity(ActivityLog log) {
        return new ActivityEntryDto(
                log.getId(),
                log.getActor() != null ? log.getActor().getId() : null,
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getPayload(),
                log.getCreatedAt());
    }
}
