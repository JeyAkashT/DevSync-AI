package com.devsync.ai.repository.pm;

import com.devsync.ai.model.pm.PmSprintStatus;
import com.devsync.ai.model.pm.Sprint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SprintRepository extends JpaRepository<Sprint, UUID> {

    Page<Sprint> findByProject_Id(UUID projectId, Pageable pageable);

    Page<Sprint> findByProject_IdAndStatus(UUID projectId, PmSprintStatus status, Pageable pageable);

    Optional<Sprint> findByIdAndProject_Id(UUID id, UUID projectId);

    boolean existsByProject_IdAndStatus(UUID projectId, PmSprintStatus status);

    long countByProject_Id(UUID projectId);

    long countByProject_IdAndStatus(UUID projectId, PmSprintStatus status);

    @Query("select s.status, count(s) from Sprint s where s.project.id = :projectId group by s.status")
    List<Object[]> countByStatus(@Param("projectId") UUID projectId);
}
