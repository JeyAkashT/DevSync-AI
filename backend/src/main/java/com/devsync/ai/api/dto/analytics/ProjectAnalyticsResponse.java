package com.devsync.ai.api.dto.analytics;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectAnalyticsResponse(
        UUID projectId,
        LocalDate asOfDate,
        KpiSummary kpis,
        List<SeriesPoint> taskStatus,
        List<SeriesPoint> bugStatus,
        List<SeriesPoint> bugSeverity,
        List<SeriesPoint> sprintStatus,
        List<WorkloadPoint> workload) {

    public record KpiSummary(
            long totalTasks,
            long completedTasks,
            long openTasks,
            long overdueTasks,
            double taskCompletionRate,
            long totalBugs,
            long openBugs,
            long criticalBugs,
            double bugResolutionRate,
            long totalSprints,
            long activeSprints) {}

    public record SeriesPoint(String key, String label, long count) {}

    public record WorkloadPoint(UUID userId, String label, long assignedTasks, long openTasks) {}
}
