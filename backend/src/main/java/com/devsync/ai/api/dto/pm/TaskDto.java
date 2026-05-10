package com.devsync.ai.api.dto.pm;

import com.devsync.ai.model.pm.PmTaskPriority;
import com.devsync.ai.model.pm.PmTaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskDto(
        UUID id,
        UUID projectId,
        UUID sprintId,
        String title,
        String description,
        PmTaskPriority priority,
        PmTaskStatus status,
        UUID assigneeUserId,
        LocalDate dueDate,
        Integer position,
        Instant createdAt,
        Instant updatedAt) {}
