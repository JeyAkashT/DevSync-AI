package com.devsync.ai.pm;

import com.devsync.ai.api.dto.pm.PageEnvelope;
import com.devsync.ai.api.dto.pm.TaskDto;
import com.devsync.ai.api.dto.pm.PmRequests.CreateTask;
import com.devsync.ai.api.dto.pm.PmRequests.PatchTask;
import com.devsync.ai.exception.ResourceNotFoundException;
import com.devsync.ai.model.User;
import com.devsync.ai.model.pm.PmTask;
import com.devsync.ai.model.pm.PmTaskPriority;
import com.devsync.ai.model.pm.PmTaskStatus;
import com.devsync.ai.model.pm.Project;
import com.devsync.ai.model.pm.ProjTeamRole;
import com.devsync.ai.model.pm.Sprint;
import com.devsync.ai.pm.spec.PmTaskSpecifications;
import com.devsync.ai.repository.UserRepository;
import com.devsync.ai.repository.pm.PmTaskRepository;
import com.devsync.ai.repository.pm.SprintRepository;
import com.devsync.ai.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PmTaskAppService {

    private final PmTaskRepository pmTaskRepository;
    private final SprintRepository sprintRepository;
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
    public PageEnvelope<TaskDto> page(
            UUID projectId,
            SecurityUser user,
            Pageable pageable,
            PmTaskStatus status,
            UUID assigneeUserId,
            UUID sprintId,
            Boolean unassignedSprintOnly,
            String search) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.VIEWER);

        Specification<PmTask> spec = PmTaskSpecifications.inProject(projectId);
        spec = andNullable(spec, PmTaskSpecifications.hasStatus(status));
        spec = andNullable(spec, PmTaskSpecifications.hasAssignee(assigneeUserId));
        if (Boolean.TRUE.equals(unassignedSprintOnly)) {
            spec = spec.and(PmTaskSpecifications.unassignedSprint());
        } else if (sprintId != null) {
            spec = spec.and(PmTaskSpecifications.hasSprint(sprintId));
        }
        spec = andNullable(spec, PmTaskSpecifications.matchesSearch(search));

        return PageEnvelope.map(pmTaskRepository.findAll(spec, pageable), PmMappings::toTask);
    }

    @Transactional(readOnly = true)
    public List<TaskDto> board(UUID projectId, SecurityUser user) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.VIEWER);
        return pmTaskRepository.findByProject_IdOrderByPositionAscUpdatedAtAsc(projectId).stream()
                .map(PmMappings::toTask)
                .toList();
    }

    @Transactional
    public TaskDto create(UUID projectId, CreateTask dto, SecurityUser user) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.MEMBER);
        User actor = user.delegate();

        PmTask t = new PmTask();
        t.setProject(project);
        t.setTitle(dto.title().trim());
        t.setDescription(dto.description());
        t.setPriority(dto.priority());
        t.setStatus(dto.status());
        t.setDueDate(dto.dueDate());
        t.setPosition(dto.position() != null ? dto.position() : 0);
        attachSprint(t, projectId, dto.sprintId());
        attachAssignee(t, projectId, dto.assigneeUserId());

        PmTask saved = pmTaskRepository.save(t);
        pmAuditLogger.log(actor, project, "CREATE", "TASK", saved.getId(), Map.of("title", saved.getTitle()));
        return PmMappings.toTask(saved);
    }

    @Transactional
    public TaskDto patch(UUID taskId, PatchTask dto, SecurityUser user) {
        PmTask task = pmTaskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task"));
        Project project = task.getProject();
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.MEMBER);
        User actor = user.delegate();

        if (dto.title() != null && !dto.title().isBlank()) {
            task.setTitle(dto.title().trim());
        }
        if (dto.description() != null) {
            task.setDescription(dto.description());
        }
        if (dto.priority() != null) {
            task.setPriority(dto.priority());
        }
        if (dto.status() != null) {
            task.setStatus(dto.status());
        }
        if (dto.dueDate() != null) {
            task.setDueDate(dto.dueDate());
        }
        if (dto.position() != null) {
            task.setPosition(dto.position());
        }
        if (dto.assigneeUserId() != null) {
            attachAssignee(task, project.getId(), dto.assigneeUserId());
        }
        if (Boolean.TRUE.equals(dto.detachSprint())) {
            task.setSprint(null);
        } else if (dto.sprintId() != null) {
            attachSprint(task, project.getId(), dto.sprintId());
        }

        pmTaskRepository.save(task);
        pmAuditLogger.log(actor, project, "UPDATE", "TASK", taskId, Map.of());
        return PmMappings.toTask(task);
    }

    @Transactional
    public void delete(UUID taskId, SecurityUser user) {
        PmTask task = pmTaskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task"));
        Project project = task.getProject();
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.PROJECT_ADMIN);
        pmAuditLogger.log(user.delegate(), project, "DELETE", "TASK", taskId, Map.of());
        pmTaskRepository.delete(task);
    }

    private void attachAssignee(PmTask t, UUID projectId, UUID assigneeId) {
        if (assigneeId == null) {
            t.setAssignee(null);
            return;
        }
        User assignee = userRepository.findById(assigneeId).orElseThrow(() -> new ResourceNotFoundException("Assignee"));
        pmAuthorizationService.membership(t.getProject().getOrganization().getId(), assignee.getId());
        t.setAssignee(assignee);
    }

    private void attachSprint(PmTask t, UUID projectId, UUID sprintId) {
        if (sprintId == null) {
            t.setSprint(null);
            return;
        }
        Sprint sp = sprintRepository
                .findByIdAndProject_Id(sprintId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint"));
        t.setSprint(sp);
    }
}
