package com.devsync.ai.analytics;

import com.devsync.ai.api.dto.analytics.ProjectAnalyticsResponse;
import com.devsync.ai.api.dto.analytics.ProjectAnalyticsResponse.KpiSummary;
import com.devsync.ai.api.dto.analytics.ProjectAnalyticsResponse.SeriesPoint;
import com.devsync.ai.api.dto.analytics.ProjectAnalyticsResponse.WorkloadPoint;
import com.devsync.ai.model.pm.PmBugSeverity;
import com.devsync.ai.model.pm.PmBugStatus;
import com.devsync.ai.model.pm.PmSprintStatus;
import com.devsync.ai.model.pm.PmTaskStatus;
import com.devsync.ai.model.pm.Project;
import com.devsync.ai.model.pm.ProjTeamRole;
import com.devsync.ai.pm.PmAuthorizationService;
import com.devsync.ai.repository.pm.PmBugRepository;
import com.devsync.ai.repository.pm.PmTaskRepository;
import com.devsync.ai.repository.pm.SprintRepository;
import com.devsync.ai.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectAnalyticsService {

    private static final List<PmBugStatus> OPEN_BUG_STATUSES =
            List.of(PmBugStatus.OPEN, PmBugStatus.TRIAGED, PmBugStatus.IN_PROGRESS);
    private static final List<PmBugStatus> RESOLVED_BUG_STATUSES =
            List.of(PmBugStatus.RESOLVED, PmBugStatus.CLOSED);
    private static final List<PmBugSeverity> CRITICAL_BUG_SEVERITIES =
            List.of(PmBugSeverity.CRITICAL, PmBugSeverity.BLOCKER);

    private final PmAuthorizationService pmAuthorizationService;
    private final PmTaskRepository pmTaskRepository;
    private final PmBugRepository pmBugRepository;
    private final SprintRepository sprintRepository;

    @Transactional(readOnly = true)
    public ProjectAnalyticsResponse getProjectAnalytics(UUID projectId, SecurityUser user) {
        Project project = pmAuthorizationService.findProjectOrThrow(projectId);
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.VIEWER);

        LocalDate today = LocalDate.now();
        long totalTasks = pmTaskRepository.countByProject_Id(projectId);
        long completedTasks = pmTaskRepository.countByProject_IdAndStatus(projectId, PmTaskStatus.DONE);
        long openTasks = pmTaskRepository.countByProject_IdAndStatusNot(projectId, PmTaskStatus.DONE);
        long overdueTasks =
                pmTaskRepository.countByProject_IdAndDueDateBeforeAndStatusNot(projectId, today, PmTaskStatus.DONE);

        long totalBugs = pmBugRepository.countByProject_Id(projectId);
        long openBugs = pmBugRepository.countByProject_IdAndStatusIn(projectId, OPEN_BUG_STATUSES);
        long resolvedBugs = pmBugRepository.countByProject_IdAndStatusIn(projectId, RESOLVED_BUG_STATUSES);
        long criticalBugs = pmBugRepository.countByProject_IdAndSeverityIn(projectId, CRITICAL_BUG_SEVERITIES);

        long totalSprints = sprintRepository.countByProject_Id(projectId);
        long activeSprints = sprintRepository.countByProject_IdAndStatus(projectId, PmSprintStatus.ACTIVE);

        KpiSummary kpis = new KpiSummary(
                totalTasks,
                completedTasks,
                openTasks,
                overdueTasks,
                percentage(completedTasks, totalTasks),
                totalBugs,
                openBugs,
                criticalBugs,
                percentage(resolvedBugs, totalBugs),
                totalSprints,
                activeSprints);

        return new ProjectAnalyticsResponse(
                projectId,
                today,
                kpis,
                enumSeries(PmTaskStatus.values(), pmTaskRepository.countByStatus(projectId), PmTaskStatus::name),
                enumSeries(PmBugStatus.values(), pmBugRepository.countByStatus(projectId), PmBugStatus::name),
                enumSeries(PmBugSeverity.values(), pmBugRepository.countBySeverity(projectId), PmBugSeverity::name),
                enumSeries(PmSprintStatus.values(), sprintRepository.countByStatus(projectId), PmSprintStatus::name),
                workload(projectId));
    }

    private static double percentage(long part, long total) {
        if (total <= 0) {
            return 0.0;
        }
        return Math.round((part * 1000.0) / total) / 10.0;
    }

    private static <E extends Enum<E>> List<SeriesPoint> enumSeries(
            E[] values, List<Object[]> rows, Function<E, String> labeler) {
        Map<String, Long> counts = rows.stream()
                .collect(Collectors.toMap(r -> ((Enum<?>) r[0]).name(), r -> ((Number) r[1]).longValue()));
        return Arrays.stream(values)
                .map(v -> new SeriesPoint(v.name(), labeler.apply(v), counts.getOrDefault(v.name(), 0L)))
                .toList();
    }

    private List<WorkloadPoint> workload(UUID projectId) {
        return pmTaskRepository.workloadByAssignee(projectId, PmTaskStatus.DONE).stream()
                .map(r -> new WorkloadPoint(
                        (UUID) r[0],
                        (String) r[1],
                        ((Number) r[2]).longValue(),
                        ((Number) r[3]).longValue()))
                .toList();
    }
}
