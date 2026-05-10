package com.devsync.ai.repository.pm;

import com.devsync.ai.model.pm.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    Page<ActivityLog> findByProject_IdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);
}
