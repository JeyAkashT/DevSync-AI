package com.devsync.ai.rest.analytics;

import com.devsync.ai.analytics.ProjectAnalyticsService;
import com.devsync.ai.api.dto.analytics.ProjectAnalyticsResponse;
import com.devsync.ai.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/analytics")
@RequiredArgsConstructor
public class ProjectAnalyticsRestController {

    private final ProjectAnalyticsService projectAnalyticsService;

    @GetMapping
    public ProjectAnalyticsResponse getProjectAnalytics(
            @PathVariable UUID projectId, @AuthenticationPrincipal SecurityUser user) {
        return projectAnalyticsService.getProjectAnalytics(projectId, user);
    }
}
