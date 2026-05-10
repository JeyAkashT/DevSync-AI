package com.devsync.ai.pm;

import com.devsync.ai.model.User;
import com.devsync.ai.model.pm.ActivityLog;
import com.devsync.ai.model.pm.Project;
import com.devsync.ai.repository.pm.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PmAuditLogger {

    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void log(User actor, Project project, String action, String entityType, UUID entityId, Map<String, Object> payload) {
        ActivityLog row = new ActivityLog();
        row.setOrganization(project.getOrganization());
        row.setProject(project);
        row.setActor(actor);
        row.setAction(action);
        row.setEntityType(entityType);
        row.setEntityId(entityId);
        row.setPayload(payload);
        activityLogRepository.save(row);
    }
}
