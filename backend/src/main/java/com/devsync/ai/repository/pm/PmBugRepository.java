package com.devsync.ai.repository.pm;

import com.devsync.ai.model.pm.PmBug;
import com.devsync.ai.model.pm.PmBugSeverity;
import com.devsync.ai.model.pm.PmBugStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PmBugRepository extends JpaRepository<PmBug, UUID>, JpaSpecificationExecutor<PmBug> {

    Optional<PmBug> findByIdAndProject_Id(UUID id, UUID projectId);

    long countByProject_Id(UUID projectId);

    long countByProject_IdAndStatusIn(UUID projectId, Collection<PmBugStatus> statuses);

    long countByProject_IdAndSeverityIn(UUID projectId, Collection<PmBugSeverity> severities);

    @Query("select b.status, count(b) from PmBug b where b.project.id = :projectId group by b.status")
    List<Object[]> countByStatus(@Param("projectId") UUID projectId);

    @Query("select b.severity, count(b) from PmBug b where b.project.id = :projectId group by b.severity")
    List<Object[]> countBySeverity(@Param("projectId") UUID projectId);
}
