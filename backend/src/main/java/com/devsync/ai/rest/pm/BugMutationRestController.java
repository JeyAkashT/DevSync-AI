package com.devsync.ai.rest.pm;

import com.devsync.ai.api.dto.pm.BugDto;
import com.devsync.ai.api.dto.pm.PmRequests.PatchBug;
import com.devsync.ai.pm.PmBugAppService;
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
@RequestMapping("/api/v1/bugs/{bugId}")
@RequiredArgsConstructor
public class BugMutationRestController {

    private final PmBugAppService pmBugAppService;

    @PatchMapping
    public BugDto patch(
            @PathVariable UUID bugId,
            @Valid @RequestBody PatchBug body,
            @AuthenticationPrincipal SecurityUser user) {
        return pmBugAppService.patch(bugId, body, user);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID bugId, @AuthenticationPrincipal SecurityUser user) {
        pmBugAppService.delete(bugId, user);
    }
}
