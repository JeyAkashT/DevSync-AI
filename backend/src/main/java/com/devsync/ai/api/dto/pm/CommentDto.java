package com.devsync.ai.api.dto.pm;

import java.time.Instant;
import java.util.UUID;

public record CommentDto(
        UUID id, UUID authorUserId, String authorEmail, String body, UUID parentCommentId, Instant createdAt) {}
