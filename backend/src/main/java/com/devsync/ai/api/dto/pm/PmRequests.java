package com.devsync.ai.api.dto.pm;

import com.devsync.ai.model.pm.PmBugSeverity;
import com.devsync.ai.model.pm.PmBugStatus;
import com.devsync.ai.model.pm.PmSprintStatus;
import com.devsync.ai.model.pm.PmTaskPriority;
import com.devsync.ai.model.pm.PmTaskStatus;
import com.devsync.ai.model.pm.ProjectMgmtStatus;
import com.devsync.ai.model.pm.ProjTeamRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public final class PmRequests {

    private PmRequests() {}

    public record CreateProject(
            @NotBlank @Size(max = 255) String name,
            @Size(max = 32) String key,
            @Size(max = 8000) String description,
            @Size(max = 1024) String repositoryUrl) {}

    public record UpdateProject(
            @Size(max = 255) String name,
            @Size(max = 8000) String description,
            ProjectMgmtStatus status,
            UUID ownerUserId,
            @Size(max = 1024) String repositoryUrl) {}

    public record AddMember(@NotNull UUID userId, @NotNull ProjTeamRole role) {}

    public record CreateTask(
            @NotBlank @Size(max = 512) String title,
            @Size(max = 8000) String description,
            @NotNull PmTaskPriority priority,
            @NotNull PmTaskStatus status,
            UUID assigneeUserId,
            LocalDate dueDate,
            UUID sprintId,
            Integer position) {}

    public record PatchTask(
            @Size(max = 512) String title,
            @Size(max = 8000) String description,
            PmTaskPriority priority,
            PmTaskStatus status,
            UUID assigneeUserId,
            LocalDate dueDate,
            UUID sprintId,
            Boolean detachSprint,
            Integer position) {}

    public record CreateBug(
            @NotBlank @Size(max = 512) String title,
            @Size(max = 8000) String description,
            @NotNull PmBugSeverity severity,
            @NotNull PmBugStatus status,
            UUID taskId,
            UUID assigneeUserId) {}

    public record PatchBug(
            @Size(max = 512) String title,
            @Size(max = 8000) String description,
            PmBugSeverity severity,
            PmBugStatus status,
            UUID taskId,
            UUID assigneeUserId) {}

    public record CreateSprint(
            @NotBlank String name,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            String goal,
            PmSprintStatus status) {}

    public record PatchSprint(String name, LocalDate startDate, LocalDate endDate, String goal, PmSprintStatus status) {}

    public record CreateComment(@NotBlank @Size(max = 8000) String body, UUID parentCommentId) {}
}
