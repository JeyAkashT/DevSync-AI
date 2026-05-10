package com.devsync.ai.api.dto.pm;

import com.devsync.ai.model.pm.PmBugSeverity;
import com.devsync.ai.model.pm.PmBugStatus;

import java.time.Instant;
import java.util.UUID;

public record BugDto(
        UUID id,
        UUID projectId,
        UUID taskId,
        String title,
        String description,
        PmBugSeverity severity,
        PmBugStatus status,
        UUID reporterUserId,
        UUID assigneeUserId,
        Instant createdAt,
        Instant updatedAt) {}
