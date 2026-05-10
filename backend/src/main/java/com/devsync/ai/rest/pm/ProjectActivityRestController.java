package com.devsync.ai.rest.pm;

import com.devsync.ai.api.dto.pm.ActivityEntryDto;
import com.devsync.ai.api.dto.pm.PageEnvelope;
import com.devsync.ai.pm.PmActivityReadService;
import com.devsync.ai.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/activity")
@RequiredArgsConstructor
public class ProjectActivityRestController {

    private final PmActivityReadService pmActivityReadService;

    @GetMapping
    public PageEnvelope<ActivityEntryDto> page(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable =
                PageRequest.of(Math.max(page == null ? 0 : page, 0), Math.min(size == null ? 30 : Math.max(size, 1), 100));
        return pmActivityReadService.page(projectId, user, pageable);
    }
}
