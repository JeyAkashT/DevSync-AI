package com.devsync.ai.api.dto.pm;

import com.devsync.ai.model.pm.ProjectMgmtStatus;

import java.util.UUID;

public record ProjectSummaryResponse(
        UUID id, String name, String key, ProjectMgmtStatus status, UUID ownerId, UUID organizationId) {}
