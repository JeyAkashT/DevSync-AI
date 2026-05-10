package com.devsync.ai.pm;

import com.devsync.ai.api.dto.pm.PageEnvelope;
import com.devsync.ai.api.dto.pm.ProjectDetailResponse;
import com.devsync.ai.api.dto.pm.ProjectMemberResponse;
import com.devsync.ai.api.dto.pm.ProjectSummaryResponse;
import com.devsync.ai.api.dto.pm.PmRequests.AddMember;
import com.devsync.ai.api.dto.pm.PmRequests.CreateProject;
import com.devsync.ai.api.dto.pm.PmRequests.UpdateProject;
import com.devsync.ai.exception.ForbiddenException;
import com.devsync.ai.exception.ResourceNotFoundException;
import com.devsync.ai.model.MembershipRole;
import com.devsync.ai.model.Organization;
import com.devsync.ai.model.User;
import com.devsync.ai.model.pm.PmSprintStatus;
import com.devsync.ai.model.pm.Project;
import com.devsync.ai.model.pm.ProjectMember;
import com.devsync.ai.model.pm.ProjectMgmtStatus;
import com.devsync.ai.model.pm.ProjTeamRole;
import com.devsync.ai.repository.MembershipRepository;
import com.devsync.ai.repository.OrganizationRepository;
import com.devsync.ai.repository.UserRepository;
import com.devsync.ai.repository.pm.PmBugRepository;
import com.devsync.ai.repository.pm.PmTaskRepository;
import com.devsync.ai.repository.pm.ProjectMemberRepository;
import com.devsync.ai.repository.pm.ProjectRepository;
import com.devsync.ai.repository.pm.SprintRepository;
import com.devsync.ai.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectPmService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final PmTaskRepository pmTaskRepository;
    private final PmBugRepository pmBugRepository;
    private final SprintRepository sprintRepository;

    private final PmAuthorizationService pmAuthorizationService;
    private final PmAuditLogger pmAuditLogger;

    private static String normalizeKeySeed(String raw) {
        String s = raw.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        return s.length() > 24 ? s.substring(0, 24) : s;
    }

    private static String randomAlnum(int len) {
        String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    private String allocateProjectKey(UUID orgId, CreateProject dto) {
        if (dto.key() != null && !dto.key().isBlank()) {
            String normalized = dto.key().trim().toUpperCase(Locale.ROOT);
            if (!normalized.matches("[A-Z0-9]{2,32}")) {
                throw new ForbiddenException("Project key must be 2–32 alphanumeric characters.");
            }
            if (projectRepository.existsByOrganization_IdAndKeyIgnoreCase(orgId, normalized)) {
                throw new ForbiddenException("Project key already taken in this organization.");
            }
            return normalized;
        }
        String base = normalizeKeySeed(dto.name());
        if (base.length() < 2) {
            base = "PRJ";
        }
        String candidate = base;
        for (int attempt = 0; attempt < 50 && projectRepository.existsByOrganization_IdAndKeyIgnoreCase(orgId, candidate); attempt++) {
            candidate = (base + randomAlnum(4)).substring(0, Math.min(32, base.length() + 4));
        }
        if (candidate.length() < 2) {
            candidate = "PR";
        }
        for (int attempt = 0; attempt < 50 && projectRepository.existsByOrganization_IdAndKeyIgnoreCase(orgId, candidate); attempt++) {
            candidate = randomAlnum(Math.min(8, 32));
        }
        if (projectRepository.existsByOrganization_IdAndKeyIgnoreCase(orgId, candidate)) {
            throw new IllegalStateException("Could not allocate unique project key");
        }
        return candidate.substring(0, Math.min(candidate.length(), 32));
    }

    private void forbidOrgViewerCreatesProject(UUID orgId, User user) {
        var om = membershipRepository.findByOrganization_IdAndUser_Id(orgId, user.getId()).orElseThrow();
        if (om.getRole() == MembershipRole.VIEWER) {
            throw new ForbiddenException("Organization viewers cannot create projects.");
        }
    }

    @Transactional(readOnly = true)
    public PageEnvelope<ProjectSummaryResponse> search(
            UUID orgId, SecurityUser user, Pageable pageable, ProjectMgmtStatus status, String search) {
        pmAuthorizationService.requireOrgMember(user, orgId);

        UUID userId = user.delegate().getId();
        Page<Project> page = projectRepository.searchVisibleForUser(
                orgId, userId, PmAuthorizationService.ORG_ELEVATED, status, search, pageable);
        return PageEnvelope.map(page, PmMappings::toSummary);
    }

    /** Load project scoped to organization (explicit org route). */
    @Transactional(readOnly = true)
    public ProjectDetailResponse getInOrganization(UUID organizationId, UUID projectId, SecurityUser user) {
        Project project = pmAuthorizationService.findProjectInOrgOrThrow(projectId, organizationId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.VIEWER);
        boolean active = sprintRepository.existsByProject_IdAndStatus(projectId, PmSprintStatus.ACTIVE);
        return PmMappings.toDetail(
                project,
                pmTaskRepository.countByProject_Id(projectId),
                pmBugRepository.countByProject_Id(projectId),
                active);
    }

    /** Load project by global id after access check (nested routes without org segment). */
    @Transactional(readOnly = true)
    public ProjectDetailResponse getGlobal(UUID projectId, SecurityUser user) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.VIEWER);
        boolean active = sprintRepository.existsByProject_IdAndStatus(projectId, PmSprintStatus.ACTIVE);
        return PmMappings.toDetail(
                project,
                pmTaskRepository.countByProject_Id(projectId),
                pmBugRepository.countByProject_Id(projectId),
                active);
    }

    @Transactional
    public ProjectDetailResponse create(UUID orgId, CreateProject dto, SecurityUser user) {
        Organization org =
                organizationRepository.findById(orgId).orElseThrow(() -> new ResourceNotFoundException("Organization"));

        pmAuthorizationService.requireOrgMember(user, orgId);
        User actor = user.delegate();
        forbidOrgViewerCreatesProject(orgId, actor);

        String key = allocateProjectKey(orgId, dto);
        Project p = new Project();
        p.setOrganization(org);
        p.setName(dto.name().trim());
        p.setKey(key);
        p.setDescription(dto.description());
        p.setOwner(actor);
        p.setRepositoryUrl(dto.repositoryUrl());
        p.setStatus(ProjectMgmtStatus.ACTIVE);
        Project saved = projectRepository.save(p);

        ProjectMember ownerRow = new ProjectMember();
        ownerRow.setProject(saved);
        ownerRow.setUser(actor);
        ownerRow.setRole(ProjTeamRole.PROJECT_OWNER);
        projectMemberRepository.save(ownerRow);

        pmAuditLogger.log(actor, saved, "CREATE", "PROJECT", saved.getId(), Map.of("name", saved.getName(), "key", saved.getKey()));
        return getGlobal(saved.getId(), user);
    }

    @Transactional
    public ProjectDetailResponse patch(UUID projectId, UpdateProject dto, SecurityUser user) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.PROJECT_ADMIN);
        User actor = user.delegate();

        if (dto.name() != null && !dto.name().isBlank()) {
            project.setName(dto.name().trim());
        }
        if (dto.description() != null) {
            project.setDescription(dto.description());
        }
        if (dto.status() != null) {
            project.setStatus(dto.status());
        }
        if (dto.ownerUserId() != null) {
            User nu = userRepository
                    .findById(dto.ownerUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Owner user"));
            pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.PROJECT_OWNER);
            membershipRepository
                    .findByOrganization_IdAndUser_Id(project.getOrganization().getId(), nu.getId())
                    .orElseThrow(() -> new ForbiddenException("New owner must belong to organization."));
            project.setOwner(nu);
        }
        if (dto.repositoryUrl() != null) {
            project.setRepositoryUrl(dto.repositoryUrl());
        }
        projectRepository.save(project);
        pmAuditLogger.log(actor, project, "UPDATE", "PROJECT", projectId, Map.of());

        return getGlobal(projectId, user);
    }

    @Transactional
    public void delete(UUID projectId, SecurityUser user) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.PROJECT_OWNER);
        pmAuditLogger.log(user.delegate(), project, "DELETE", "PROJECT", projectId, Map.of());
        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> listMembers(UUID projectId, SecurityUser user) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.VIEWER);

        List<ProjectMember> rows =
                projectMemberRepository.findByProject_IdOrderByCreatedAtAsc(projectId);
        return rows.stream().map(PmMappings::toMember).toList();
    }

    @Transactional
    public ProjectMemberResponse addMember(UUID projectId, AddMember req, SecurityUser user) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.PROJECT_ADMIN);

        User target = userRepository.findById(req.userId()).orElseThrow(() -> new ResourceNotFoundException("User"));
        membershipRepository
                .findByOrganization_IdAndUser_Id(project.getOrganization().getId(), target.getId())
                .orElseThrow(() -> new ForbiddenException("User is not part of this organization."));

        if (projectMemberRepository.existsByProject_IdAndUser_Id(projectId, target.getId())) {
            throw new ForbiddenException("User is already part of this project.");
        }

        ProjectMember pm = new ProjectMember();
        pm.setProject(project);
        pm.setUser(target);
        pm.setRole(req.role());
        ProjectMember saved = projectMemberRepository.save(pm);
        pmAuditLogger.log(
                user.delegate(), project, "ADD_MEMBER", "PROJECT_MEMBER", saved.getId(), Map.of("userId", target.getId()));

        return PmMappings.toMember(saved);
    }

    @Transactional
    public void removeMember(UUID projectId, UUID memberUserId, SecurityUser actor) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(actor, project, ProjTeamRole.PROJECT_ADMIN);

        ProjectMember pm = projectMemberRepository
                .findByProject_IdAndUser_Id(projectId, memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Project member"));

        if (pm.getRole() == ProjTeamRole.PROJECT_OWNER
                && projectMemberRepository.countByProject_IdAndRole(projectId, ProjTeamRole.PROJECT_OWNER) <= 1) {
            throw new ForbiddenException("Cannot remove the sole project owner.");
        }

        projectMemberRepository.delete(pm);
        pmAuditLogger.log(
                actor.delegate(), project, "REMOVE_MEMBER", "PROJECT_MEMBER", pm.getId(), Map.of("userId", memberUserId));
    }
}
