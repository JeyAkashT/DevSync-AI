package com.devsync.ai.rest.analytics;

import com.devsync.ai.analytics.ProjectAnalyticsService;
import com.devsync.ai.api.dto.analytics.ProjectAnalyticsResponse;
import com.devsync.ai.api.dto.analytics.ProjectAnalyticsResponse.KpiSummary;
import com.devsync.ai.api.dto.analytics.ProjectAnalyticsResponse.SeriesPoint;
import com.devsync.ai.security.JwtAuthenticationFilter;
import com.devsync.ai.security.SecurityUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = ProjectAnalyticsRestController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class),
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    })
@Import(ProjectAnalyticsRestControllerTest.StubAnalyticsConfig.class)
class ProjectAnalyticsRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void projectAnalyticsReturnsKpis() throws Exception {
        UUID projectId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/projects/{projectId}/analytics", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.kpis.totalTasks").value(12))
                .andExpect(jsonPath("$.taskStatus[0].key").value("DONE"));
    }

    @TestConfiguration
    static class StubAnalyticsConfig {
        @Bean
        ProjectAnalyticsService projectAnalyticsService() {
            return new ProjectAnalyticsService(null, null, null, null) {
                @Override
                public ProjectAnalyticsResponse getProjectAnalytics(UUID projectId, SecurityUser user) {
                    return new ProjectAnalyticsResponse(
                            projectId,
                            LocalDate.of(2026, 5, 10),
                            new KpiSummary(12, 5, 7, 2, 41.7, 4, 3, 1, 25.0, 2, 1),
                            List.of(new SeriesPoint("DONE", "DONE", 5)),
                            List.of(),
                            List.of(),
                            List.of(),
                            List.of());
                }
            };
        }
    }
}
