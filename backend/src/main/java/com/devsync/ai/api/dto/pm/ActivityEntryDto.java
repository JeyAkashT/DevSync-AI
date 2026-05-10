package com.devsync.ai.api.dto.pm;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ActivityEntryDto(
        UUID id,
        UUID actorUserId,
        String action,
        String entityType,
        UUID entityId,
        Map<String, Object> payload,
        Instant createdAt) {}
