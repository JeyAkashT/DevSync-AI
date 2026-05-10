package com.devsync.ai.pm;

import com.devsync.ai.api.dto.pm.BugDto;
import com.devsync.ai.api.dto.pm.PageEnvelope;
import com.devsync.ai.api.dto.pm.PmRequests.CreateBug;
import com.devsync.ai.api.dto.pm.PmRequests.PatchBug;
import com.devsync.ai.exception.ResourceNotFoundException;
import com.devsync.ai.model.User;
import com.devsync.ai.model.pm.PmBug;
import com.devsync.ai.model.pm.PmBugSeverity;
import com.devsync.ai.model.pm.PmBugStatus;
import com.devsync.ai.model.pm.PmTask;
import com.devsync.ai.model.pm.Project;
import com.devsync.ai.model.pm.ProjTeamRole;
import com.devsync.ai.pm.spec.PmBugSpecifications;
import com.devsync.ai.repository.UserRepository;
import com.devsync.ai.repository.pm.PmBugRepository;
import com.devsync.ai.repository.pm.PmTaskRepository;
import com.devsync.ai.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PmBugAppService {

    private final PmBugRepository pmBugRepository;
    private final PmTaskRepository pmTaskRepository;
    private final UserRepository userRepository;
    private final PmAuthorizationService pmAuthorizationService;
    private final PmAuditLogger pmAuditLogger;

    private static <T> Specification<T> andNullable(Specification<T> base, Specification<T> optional) {
        if (optional == null) {
            return base;
        }
        return base == null ? optional : base.and(optional);
    }

    @Transactional(readOnly = true)
    public PageEnvelope<BugDto> page(
            UUID projectId, SecurityUser user, Pageable pageable, PmBugStatus status, PmBugSeverity severity, String search) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.VIEWER);

        Specification<PmBug> spec = PmBugSpecifications.inProject(projectId);
        spec = andNullable(spec, PmBugSpecifications.hasStatus(status));
        spec = andNullable(spec, PmBugSpecifications.hasSeverity(severity));
        spec = andNullable(spec, PmBugSpecifications.matchesSearch(search));

        return PageEnvelope.map(pmBugRepository.findAll(spec, pageable), PmMappings::toBug);
    }

    @Transactional
    public BugDto create(UUID projectId, CreateBug dto, SecurityUser user) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.MEMBER);
        User actor = user.delegate();

        PmBug b = new PmBug();
        b.setProject(project);
        b.setTitle(dto.title().trim());
        b.setDescription(dto.description());
        b.setSeverity(dto.severity());
        b.setStatus(dto.status());
        b.setReporter(actor);
        b.setTask(resolveTask(projectId, dto.taskId()));
        b.setAssignee(resolveAssignee(project, dto.assigneeUserId()));

        PmBug saved = pmBugRepository.save(b);
        pmAuditLogger.log(actor, project, "CREATE", "BUG", saved.getId(), Map.of("title", saved.getTitle()));
        return PmMappings.toBug(saved);
    }

    @Transactional
    public BugDto patch(UUID bugId, PatchBug dto, SecurityUser user) {
        PmBug bug = pmBugRepository.findById(bugId).orElseThrow(() -> new ResourceNotFoundException("Bug"));
        Project project = bug.getProject();
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.MEMBER);
        User actor = user.delegate();

        if (dto.title() != null && !dto.title().isBlank()) {
            bug.setTitle(dto.title().trim());
        }
        if (dto.description() != null) {
            bug.setDescription(dto.description());
        }
        if (dto.severity() != null) {
            bug.setSeverity(dto.severity());
        }
        if (dto.status() != null) {
            bug.setStatus(dto.status());
        }
        if (dto.taskId() != null) {
            bug.setTask(resolveTask(project.getId(), dto.taskId()));
        }
        if (dto.assigneeUserId() != null) {
            bug.setAssignee(resolveAssignee(project, dto.assigneeUserId()));
        }

        pmBugRepository.save(bug);
        pmAuditLogger.log(actor, project, "UPDATE", "BUG", bugId, Map.of());
        return PmMappings.toBug(bug);
    }

    @Transactional
    public void delete(UUID bugId, SecurityUser user) {
        PmBug bug = pmBugRepository.findById(bugId).orElseThrow(() -> new ResourceNotFoundException("Bug"));
        Project project = bug.getProject();
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.PROJECT_ADMIN);
        pmAuditLogger.log(user.delegate(), project, "DELETE", "BUG", bugId, Map.of());
        pmBugRepository.delete(bug);
    }

    private PmTask resolveTask(UUID projectId, UUID taskId) {
        if (taskId == null) {
            return null;
        }
        return pmTaskRepository
                .findByIdAndProject_Id(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Task"));
    }

    private User resolveAssignee(Project project, UUID assigneeUserId) {
        if (assigneeUserId == null) {
            return null;
        }
        User u = userRepository.findById(assigneeUserId).orElseThrow(() -> new ResourceNotFoundException("Assignee"));
        pmAuthorizationService.membership(project.getOrganization().getId(), u.getId());
        return u;
    }
}
