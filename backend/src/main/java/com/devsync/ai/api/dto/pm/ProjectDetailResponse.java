package com.devsync.ai.api.dto.pm;

import com.devsync.ai.model.pm.ProjectMgmtStatus;

import java.util.UUID;

public record ProjectDetailResponse(
        UUID id,
        UUID organizationId,
        String name,
        String key,
        String description,
        ProjectMgmtStatus status,
        UUID ownerId,
        String repositoryUrl,
        Long taskCount,
        Long bugCount,
        Boolean activeSprint) {}
