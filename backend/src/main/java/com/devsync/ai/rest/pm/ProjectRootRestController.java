package com.devsync.ai.rest.pm;

import com.devsync.ai.api.dto.pm.ProjectDetailResponse;
import com.devsync.ai.api.dto.pm.PmRequests.UpdateProject;
import com.devsync.ai.pm.ProjectPmService;
import com.devsync.ai.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
@RequiredArgsConstructor
public class ProjectRootRestController {

    private final ProjectPmService projectPmService;

    @GetMapping
    public ProjectDetailResponse get(
            @PathVariable UUID projectId, @AuthenticationPrincipal SecurityUser user) {
        return projectPmService.getGlobal(projectId, user);
    }

    @PatchMapping
    public ProjectDetailResponse patch(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProject body,
            @AuthenticationPrincipal SecurityUser user) {
        return projectPmService.patch(projectId, body, user);
    }

    @DeleteMapping
    public void delete(@PathVariable UUID projectId, @AuthenticationPrincipal SecurityUser user) {
        projectPmService.delete(projectId, user);
    }
}
