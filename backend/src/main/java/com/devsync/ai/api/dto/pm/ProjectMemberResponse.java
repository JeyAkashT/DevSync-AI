package com.devsync.ai.api.dto.pm;

import com.devsync.ai.model.pm.ProjTeamRole;

import java.util.UUID;

public record ProjectMemberResponse(UUID id, UUID userId, String email, String fullName, ProjTeamRole role) {}
