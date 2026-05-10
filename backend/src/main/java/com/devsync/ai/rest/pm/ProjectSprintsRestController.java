package com.devsync.ai.rest.pm;

import com.devsync.ai.api.dto.pm.PageEnvelope;
import com.devsync.ai.api.dto.pm.PmRequests.CreateSprint;
import com.devsync.ai.api.dto.pm.SprintDto;
import com.devsync.ai.model.pm.PmSprintStatus;
import com.devsync.ai.pm.PmSprintAppService;
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
@RequestMapping("/api/v1/projects/{projectId}/sprints")
@RequiredArgsConstructor
public class ProjectSprintsRestController {

    private final PmSprintAppService pmSprintAppService;

    @GetMapping
    public PageEnvelope<SprintDto> page(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) PmSprintStatus status) {
        Pageable pageable =
                PageRequest.of(Math.max(page == null ? 0 : page, 0), Math.min(size == null ? 25 : Math.max(size, 1), 100));
        return pmSprintAppService.page(projectId, user, pageable, status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SprintDto create(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateSprint body,
            @AuthenticationPrincipal SecurityUser user) {
        return pmSprintAppService.create(projectId, body, user);
    }
}
