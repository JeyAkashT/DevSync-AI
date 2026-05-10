package com.devsync.ai.rest.pm;

import com.devsync.ai.api.dto.pm.PageEnvelope;
import com.devsync.ai.api.dto.pm.TaskDto;
import com.devsync.ai.api.dto.pm.PmRequests.CreateTask;
import com.devsync.ai.model.pm.PmTaskStatus;
import com.devsync.ai.pm.PmTaskAppService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class ProjectTasksRestController {

    private final PmTaskAppService pmTaskAppService;

    @GetMapping
    public PageEnvelope<TaskDto> page(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) PmTaskStatus status,
            @RequestParam(required = false) UUID assigneeUserId,
            @RequestParam(required = false) UUID sprintId,
            @RequestParam(required = false) Boolean unassignedSprintOnly,
            @RequestParam(required = false) String q) {
        Pageable pageable =
                PageRequest.of(Math.max(page == null ? 0 : page, 0), Math.min(size == null ? 25 : Math.max(size, 1), 100));
        return pmTaskAppService.page(
                projectId, user, pageable, status, assigneeUserId, sprintId, unassignedSprintOnly, q);
    }

    @GetMapping("/board")
    public List<TaskDto> board(@PathVariable UUID projectId, @AuthenticationPrincipal SecurityUser user) {
        return pmTaskAppService.board(projectId, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto create(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTask body,
            @AuthenticationPrincipal SecurityUser user) {
        return pmTaskAppService.create(projectId, body, user);
    }
}
