package com.devsync.ai.rest.pm;

import com.devsync.ai.api.dto.pm.PageEnvelope;
import com.devsync.ai.api.dto.pm.ProjectDetailResponse;
import com.devsync.ai.api.dto.pm.ProjectSummaryResponse;
import com.devsync.ai.api.dto.pm.PmRequests.CreateProject;
import com.devsync.ai.model.pm.ProjectMgmtStatus;
import com.devsync.ai.pm.ProjectPmService;
import com.devsync.ai.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/projects")
@RequiredArgsConstructor
public class OrgProjectCatalogRestController {

    private final ProjectPmService projectPmService;

    @GetMapping
    public PageEnvelope<ProjectSummaryResponse> list(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) ProjectMgmtStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "updatedAt,DESC") String sortIgnored) {
        Pageable pageable = PageRequest.of(
                Math.max(page == null ? 0 : page, 0),
                Math.min(size == null ? 25 : Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "updatedAt"));
        return projectPmService.search(
                organizationId, user, pageable, status, q == null || q.isBlank() ? null : q.trim());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDetailResponse create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateProject body,
            @AuthenticationPrincipal SecurityUser user) {
        return projectPmService.create(organizationId, body, user);
    }

    @GetMapping("/{projectId}")
    public ProjectDetailResponse getScoped(
            @PathVariable UUID organizationId,
            @PathVariable UUID projectId,
            @AuthenticationPrincipal SecurityUser user) {
        return projectPmService.getInOrganization(organizationId, projectId, user);
    }
}
