package com.devsync.ai.rest.pm;

import com.devsync.ai.api.dto.pm.CommentDto;
import com.devsync.ai.api.dto.pm.PageEnvelope;
import com.devsync.ai.api.dto.pm.PmRequests.CreateComment;
import com.devsync.ai.pm.PmCommentAppService;
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
@RequiredArgsConstructor
public class PmCommentsRestController {

    private final PmCommentAppService pmCommentAppService;

    @GetMapping("/api/v1/tasks/{taskId}/comments")
    public PageEnvelope<CommentDto> taskComments(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable =
                PageRequest.of(Math.max(page == null ? 0 : page, 0), Math.min(size == null ? 50 : Math.max(size, 1), 200));
        return pmCommentAppService.forTask(taskId, user, pageable);
    }

    @PostMapping("/api/v1/tasks/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto commentOnTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateComment body,
            @AuthenticationPrincipal SecurityUser user) {
        return pmCommentAppService.createOnTask(taskId, body, user);
    }

    @GetMapping("/api/v1/bugs/{bugId}/comments")
    public PageEnvelope<CommentDto> bugComments(
            @PathVariable UUID bugId,
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable =
                PageRequest.of(Math.max(page == null ? 0 : page, 0), Math.min(size == null ? 50 : Math.max(size, 1), 200));
        return pmCommentAppService.forBug(bugId, user, pageable);
    }

    @PostMapping("/api/v1/bugs/{bugId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto commentOnBug(
            @PathVariable UUID bugId,
            @Valid @RequestBody CreateComment body,
            @AuthenticationPrincipal SecurityUser user) {
        return pmCommentAppService.createOnBug(bugId, body, user);
    }
}
