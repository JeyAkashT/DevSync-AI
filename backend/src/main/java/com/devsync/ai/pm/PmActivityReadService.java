package com.devsync.ai.pm;

import com.devsync.ai.api.dto.pm.ActivityEntryDto;
import com.devsync.ai.api.dto.pm.PageEnvelope;
import com.devsync.ai.model.pm.Project;
import com.devsync.ai.model.pm.ProjTeamRole;
import com.devsync.ai.repository.pm.ActivityLogRepository;
import com.devsync.ai.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PmActivityReadService {

    private final ActivityLogRepository activityLogRepository;
    private final PmAuthorizationService pmAuthorizationService;

    @Transactional(readOnly = true)
    public PageEnvelope<ActivityEntryDto> page(UUID projectId, SecurityUser user, Pageable pageable) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.VIEWER);
        var page = activityLogRepository.findByProject_IdOrderByCreatedAtDesc(projectId, pageable);
        return PageEnvelope.map(page, PmMappings::toActivity);
    }
}
