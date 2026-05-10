package com.devsync.ai.repository.pm;

import com.devsync.ai.model.pm.PmTask;
import com.devsync.ai.model.pm.PmTaskStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PmTaskRepository extends JpaRepository<PmTask, UUID>, JpaSpecificationExecutor<PmTask> {

    Optional<PmTask> findByIdAndProject_Id(UUID id, UUID projectId);

    List<PmTask> findByProject_IdOrderByPositionAscUpdatedAtAsc(UUID projectId);

    long countByProject_Id(UUID projectId);

    long countByProject_IdAndStatus(UUID projectId, PmTaskStatus status);

    long countByProject_IdAndStatusNot(UUID projectId, PmTaskStatus status);

    long countByProject_IdAndDueDateBeforeAndStatusNot(UUID projectId, LocalDate dueDate, PmTaskStatus status);

    @Query("select t.status, count(t) from PmTask t where t.project.id = :projectId group by t.status")
    List<Object[]> countByStatus(@Param("projectId") UUID projectId);

    @Query("""
            select t.assignee.id,
                   coalesce(t.assignee.fullName, t.assignee.email),
                   count(t),
                   sum(case when t.status <> :doneStatus then 1 else 0 end)
            from PmTask t
            where t.project.id = :projectId and t.assignee is not null
            group by t.assignee.id, t.assignee.fullName, t.assignee.email
            order by count(t) desc
            """)
    List<Object[]> workloadByAssignee(
            @Param("projectId") UUID projectId, @Param("doneStatus") PmTaskStatus doneStatus);
}
