package com.devsync.ai.pm;

import com.devsync.ai.api.dto.pm.CommentDto;
import com.devsync.ai.api.dto.pm.PageEnvelope;
import com.devsync.ai.api.dto.pm.PmRequests.CreateComment;
import com.devsync.ai.exception.ResourceNotFoundException;
import com.devsync.ai.model.pm.PmBug;
import com.devsync.ai.model.pm.PmComment;
import com.devsync.ai.model.pm.PmCommentSubject;
import com.devsync.ai.model.pm.PmTask;
import com.devsync.ai.model.pm.Project;
import com.devsync.ai.model.pm.ProjTeamRole;
import com.devsync.ai.repository.pm.PmBugRepository;
import com.devsync.ai.repository.pm.PmCommentRepository;
import com.devsync.ai.repository.pm.PmTaskRepository;
import com.devsync.ai.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PmCommentAppService {

    private final PmCommentRepository pmCommentRepository;
    private final PmTaskRepository pmTaskRepository;
    private final PmBugRepository pmBugRepository;
    private final PmAuthorizationService pmAuthorizationService;
    private final PmAuditLogger pmAuditLogger;

    @Transactional(readOnly = true)
    public PageEnvelope<CommentDto> forTask(UUID taskId, SecurityUser user, Pageable pageable) {
        PmTask task = pmTaskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task"));
        pmAuthorizationService.requireAtLeast(user, task.getProject(), ProjTeamRole.VIEWER);

        var page =
                pmCommentRepository.findBySubjectTypeAndSubjectIdOrderByCreatedAtAsc(
                        PmCommentSubject.TASK, taskId, pageable);
        return PageEnvelope.map(page, PmMappings::toComment);
    }

    @Transactional(readOnly = true)
    public PageEnvelope<CommentDto> forBug(UUID bugId, SecurityUser user, Pageable pageable) {
        PmBug bug = pmBugRepository.findById(bugId).orElseThrow(() -> new ResourceNotFoundException("Bug"));
        pmAuthorizationService.requireAtLeast(user, bug.getProject(), ProjTeamRole.VIEWER);

        var page =
                pmCommentRepository.findBySubjectTypeAndSubjectIdOrderByCreatedAtAsc(
                        PmCommentSubject.BUG, bugId, pageable);
        return PageEnvelope.map(page, PmMappings::toComment);
    }

    @Transactional
    public CommentDto createOnTask(UUID taskId, CreateComment dto, SecurityUser user) {
        PmTask task = pmTaskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task"));
        Project project = task.getProject();
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.MEMBER);

        PmComment c = new PmComment();
        c.setSubjectType(PmCommentSubject.TASK);
        c.setSubjectId(taskId);
        c.setAuthor(user.delegate());
        c.setBody(dto.body().trim());
        c.setParentComment(resolveParent(taskId, dto.parentCommentId()));

        pmCommentRepository.save(c);
        pmAuditLogger.log(user.delegate(), project, "COMMENT", "TASK", taskId, Map.of("commentId", c.getId()));

        return PmMappings.toComment(c);
    }

    @Transactional
    public CommentDto createOnBug(UUID bugId, CreateComment dto, SecurityUser user) {
        PmBug bug = pmBugRepository.findById(bugId).orElseThrow(() -> new ResourceNotFoundException("Bug"));
        Project project = bug.getProject();
        pmAuthorizationService.requireAtLeast(user, project, ProjTeamRole.MEMBER);

        PmComment c = new PmComment();
        c.setSubjectType(PmCommentSubject.BUG);
        c.setSubjectId(bugId);
        c.setAuthor(user.delegate());
        c.setBody(dto.body().trim());
        c.setParentComment(resolveParentBug(bugId, dto.parentCommentId()));

        pmCommentRepository.save(c);
        pmAuditLogger.log(user.delegate(), project, "COMMENT", "BUG", bugId, Map.of("commentId", c.getId()));

        return PmMappings.toComment(c);
    }

    private PmComment resolveParent(UUID taskId, UUID parentId) {
        if (parentId == null) {
            return null;
        }
        PmComment parent = pmCommentRepository.findById(parentId).orElseThrow(() -> new ResourceNotFoundException("Comment"));
        if (!parent.getSubjectType().equals(PmCommentSubject.TASK) || !parent.getSubjectId().equals(taskId)) {
            throw new ResourceNotFoundException("Comment thread");
        }
        return parent;
    }

    private PmComment resolveParentBug(UUID bugId, UUID parentId) {
        if (parentId == null) {
            return null;
        }
        PmComment parent = pmCommentRepository.findById(parentId).orElseThrow(() -> new ResourceNotFoundException("Comment"));
        if (!parent.getSubjectType().equals(PmCommentSubject.BUG) || !parent.getSubjectId().equals(bugId)) {
            throw new ResourceNotFoundException("Comment thread");
        }
        return parent;
    }
}
