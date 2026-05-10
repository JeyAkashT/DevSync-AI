package com.devsync.ai.repository.pm;

import com.devsync.ai.model.MembershipRole;
import com.devsync.ai.model.pm.Project;
import com.devsync.ai.model.pm.ProjectMgmtStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    Optional<Project> findByIdAndOrganization_Id(UUID id, UUID organizationId);

    boolean existsByOrganization_IdAndKeyIgnoreCase(UUID organizationId, String key);

    @Query(
            """
                    SELECT DISTINCT p FROM Project p
                    WHERE p.organization.id = :orgId
                      AND (
                        EXISTS (
                          SELECT 1 FROM Membership m
                          WHERE m.organization.id = :orgId AND m.user.id = :userId
                            AND m.role IN :orgElevatedRoles)
                        OR EXISTS (
                          SELECT 1 FROM ProjectMember pm
                          WHERE pm.project.id = p.id AND pm.user.id = :userId))
                      AND (:status IS NULL OR p.status = :status)
                      AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
                    """)
    Page<Project> searchVisibleForUser(
            @Param("orgId") UUID orgId,
            @Param("userId") UUID userId,
            @Param("orgElevatedRoles") List<MembershipRole> orgElevatedRoles,
            @Param("status") ProjectMgmtStatus status,
            @Param("search") String search,
            Pageable pageable);
}
