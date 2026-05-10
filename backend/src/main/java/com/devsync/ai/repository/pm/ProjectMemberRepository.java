package com.devsync.ai.repository.pm;

import com.devsync.ai.model.pm.ProjectMember;
import com.devsync.ai.model.pm.ProjTeamRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    Optional<ProjectMember> findByProject_IdAndUser_Id(UUID projectId, UUID userId);

    List<ProjectMember> findByProject_IdOrderByCreatedAtAsc(UUID projectId);

    boolean existsByProject_IdAndUser_Id(UUID projectId, UUID userId);

    long countByProject_IdAndRole(UUID projectId, ProjTeamRole role);
}
