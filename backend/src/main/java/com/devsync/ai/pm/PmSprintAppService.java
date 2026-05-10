package com.devsync.ai.pm;

import com.devsync.ai.api.dto.pm.PageEnvelope;
import com.devsync.ai.api.dto.pm.PmRequests.CreateSprint;
import com.devsync.ai.api.dto.pm.PmRequests.PatchSprint;
import com.devsync.ai.api.dto.pm.SprintDto;
import com.devsync.ai.exception.ResourceNotFoundException;
import com.devsync.ai.model.pm.PmSprintStatus;
import com.devsync.ai.model.pm.Project;
import com.devsync.ai.model.pm.ProjTeamRole;
import com.devsync.ai.model.pm.Sprint;
import com.devsync.ai.repository.pm.SprintRepository;
import com.devsync.ai.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PmSprintAppService {

    private final SprintRepository sprintRepository;
    private final PmAuthorizationService pmAuthorizationService;
    private final PmAuditLogger pmAuditLogger;

    @Transactional(readOnly = true)
    public PageEnvelope<SprintDto> page(UUID projectId, SecurityUser user, Pageable pageable, PmSprintStatus status) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.VIEWER);

        var page =
                status == null
                        ? sprintRepository.findByProject_Id(projectId, pageable)
                        : sprintRepository.findByProject_IdAndStatus(projectId, status, pageable);

        return PageEnvelope.map(page, PmMappings::toSprint);
    }

    @Transactional
    public SprintDto create(UUID projectId, CreateSprint dto, SecurityUser user) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.MEMBER);
        Sprint s = new Sprint();
        s.setProject(project);
        s.setName(dto.name().trim());
        s.setStartDate(dto.startDate());
        s.setEndDate(dto.endDate());
        s.setGoal(dto.goal());
        s.setStatus(dto.status() != null ? dto.status() : PmSprintStatus.PLANNED);
        Sprint saved = sprintRepository.save(s);
        pmAuditLogger.log(
                user.delegate(), project, "CREATE", "SPRINT", saved.getId(), Map.of("name", saved.getName()));
        return PmMappings.toSprint(saved);
    }

    @Transactional
    public SprintDto patch(UUID sprintId, PatchSprint dto, SecurityUser user) {
        Sprint s = sprintRepository.findById(sprintId).orElseThrow(() -> new ResourceNotFoundException("Sprint"));
        Project p = s.getProject();
        pmAuthorizationService.requireAtLeast(user, p, ProjTeamRole.MEMBER);
        if (dto.name() != null && !dto.name().isBlank()) {
            s.setName(dto.name().trim());
        }
        if (dto.goal() != null) {
            s.setGoal(dto.goal());
        }
        if (dto.status() != null) {
            s.setStatus(dto.status());
        }
        if (dto.startDate() != null) {
            s.setStartDate(dto.startDate());
        }
        if (dto.endDate() != null) {
            s.setEndDate(dto.endDate());
        }
        sprintRepository.save(s);
        pmAuditLogger.log(user.delegate(), p, "UPDATE", "SPRINT", sprintId, Map.of());
        return PmMappings.toSprint(s);
    }

    @Transactional
    public void delete(UUID sprintId, SecurityUser user) {
        Sprint s = sprintRepository.findById(sprintId).orElseThrow(() -> new ResourceNotFoundException("Sprint"));
        Project p = s.getProject();
        pmAuthorizationService.requireAtLeast(user, p, ProjTeamRole.PROJECT_ADMIN);
        pmAuditLogger.log(user.delegate(), p, "DELETE", "SPRINT", sprintId, Map.of());
        sprintRepository.delete(s);
    }
}
