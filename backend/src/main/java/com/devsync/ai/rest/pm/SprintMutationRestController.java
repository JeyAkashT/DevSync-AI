package com.devsync.ai.rest.pm;

import com.devsync.ai.api.dto.pm.PmRequests.PatchSprint;
import com.devsync.ai.api.dto.pm.SprintDto;
import com.devsync.ai.pm.PmSprintAppService;
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
@RequestMapping("/api/v1/sprints/{sprintId}")
@RequiredArgsConstructor
public class SprintMutationRestController {

    private final PmSprintAppService pmSprintAppService;

    @PatchMapping
    public SprintDto patch(
            @PathVariable UUID sprintId,
            @Valid @RequestBody PatchSprint body,
            @AuthenticationPrincipal SecurityUser user) {
        return pmSprintAppService.patch(sprintId, body, user);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID sprintId, @AuthenticationPrincipal SecurityUser user) {
        pmSprintAppService.delete(sprintId, user);
    }
}
