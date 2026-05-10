package com.devsync.ai.rest.pm;

import com.devsync.ai.api.dto.pm.ProjectMemberResponse;
import com.devsync.ai.api.dto.pm.PmRequests.AddMember;
import com.devsync.ai.pm.ProjectPmService;
import com.devsync.ai.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMembersRestController {

    private final ProjectPmService projectPmService;

    @GetMapping
    public List<ProjectMemberResponse> list(
            @PathVariable UUID projectId, @AuthenticationPrincipal SecurityUser user) {
        return projectPmService.listMembers(projectId, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectMemberResponse add(
            @PathVariable UUID projectId,
            @Valid @RequestBody AddMember body,
            @AuthenticationPrincipal SecurityUser user) {
        return projectPmService.addMember(projectId, body, user);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal SecurityUser user) {
        projectPmService.removeMember(projectId, userId, user);
    }
}
