package com.devsync.ai.api.dto.pm;

import com.devsync.ai.model.pm.PmSprintStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SprintDto(
        UUID id,
        UUID projectId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String goal,
        PmSprintStatus status,
        Instant createdAt,
        Instant updatedAt) {}
