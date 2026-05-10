package com.devsync.ai.rest.pm;

import com.devsync.ai.api.dto.pm.BugDto;
import com.devsync.ai.api.dto.pm.PageEnvelope;
import com.devsync.ai.api.dto.pm.PmRequests.CreateBug;
import com.devsync.ai.model.pm.PmBugSeverity;
import com.devsync.ai.model.pm.PmBugStatus;
import com.devsync.ai.pm.PmBugAppService;
import com.devsync.ai.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/v1/projects/{projectId}/bugs")
@RequiredArgsConstructor
public class ProjectBugsRestController {

    private final PmBugAppService pmBugAppService;

    @GetMapping
    public PageEnvelope<BugDto> page(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) PmBugStatus status,
            @RequestParam(required = false) PmBugSeverity severity,
            @RequestParam(required = false) String q) {
        Pageable pageable =
                PageRequest.of(Math.max(page == null ? 0 : page, 0), Math.min(size == null ? 25 : Math.max(size, 1), 100));
        return pmBugAppService.page(projectId, user, pageable, status, severity, q);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BugDto create(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateBug body,
            @AuthenticationPrincipal SecurityUser user) {
        return pmBugAppService.create(projectId, body, user);
    }
}
