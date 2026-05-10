package com.devsync.ai.pm;

import com.devsync.ai.exception.ForbiddenException;
import com.devsync.ai.exception.ResourceNotFoundException;
import com.devsync.ai.model.Membership;
import com.devsync.ai.model.MembershipRole;
import com.devsync.ai.model.User;
import com.devsync.ai.model.pm.Project;
import com.devsync.ai.model.pm.ProjTeamRole;
import com.devsync.ai.repository.MembershipRepository;
import com.devsync.ai.repository.pm.ProjectMemberRepository;
import com.devsync.ai.repository.pm.ProjectRepository;
import com.devsync.ai.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PmAuthorizationService {

    static final List<MembershipRole> ORG_ELEVATED = List.of(MembershipRole.OWNER, MembershipRole.ADMIN);

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MembershipRepository membershipRepository;

    public void requireOrgMember(SecurityUser principal, UUID organizationId) {
        membership(organizationId, principal.delegate().getId());
    }

    public Membership membership(UUID organizationId, UUID userId) {
        return membershipRepository
                .findByOrganization_IdAndUser_Id(organizationId, userId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this organization."));
    }

    public Project findProjectOrThrow(UUID projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));
    }

    public Project findProjectInOrgOrThrow(UUID projectId, UUID organizationId) {
        return projectRepository
                .findByIdAndOrganization_Id(projectId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));
    }

    public ProjTeamRole effectiveProjectRole(SecurityUser principal, Project project) {
        User user = principal.delegate();
        if (isApplicationAdmin(principal)) {
            return ProjTeamRole.PROJECT_OWNER;
        }
        UUID orgId = project.getOrganization().getId();
        Membership om = membership(orgId, user.getId());

        if (ORG_ELEVATED.contains(om.getRole())) {
            return ProjTeamRole.PROJECT_OWNER;
        }

        return projectMemberRepository
                .findByProject_IdAndUser_Id(project.getId(), user.getId())
                .map(pm -> pm.getRole())
                .orElseThrow(() -> new ForbiddenException("You are not a member of this project."));
    }

    public void requireAtLeast(SecurityUser principal, Project project, ProjTeamRole minimum) {
        ProjTeamRole effective = effectiveProjectRole(principal, project);
        if (effective.ordinal() < minimum.ordinal()) {
            throw new ForbiddenException("Insufficient permissions for this action.");
        }
    }

    private static boolean isApplicationAdmin(SecurityUser principal) {
        return principal.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
