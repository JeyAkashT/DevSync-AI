package com.devsync.ai.rest.pm;

import com.devsync.ai.api.dto.pm.TaskDto;
import com.devsync.ai.api.dto.pm.PmRequests.PatchTask;
import com.devsync.ai.pm.PmTaskAppService;
import com.devsync.ai.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}")
@RequiredArgsConstructor
public class TaskMutationRestController {

    private final PmTaskAppService pmTaskAppService;

    @PatchMapping
    public TaskDto patch(
            @PathVariable UUID taskId,
            @Valid @RequestBody PatchTask body,
            @AuthenticationPrincipal SecurityUser user) {
        return pmTaskAppService.patch(taskId, body, user);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID taskId, @AuthenticationPrincipal SecurityUser user) {
        pmTaskAppService.delete(taskId, user);
    }
}
