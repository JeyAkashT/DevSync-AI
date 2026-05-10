package com.devsync.ai.repository.pm;

import com.devsync.ai.model.pm.PmComment;
import com.devsync.ai.model.pm.PmCommentSubject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PmCommentRepository extends JpaRepository<PmComment, UUID> {

    Page<PmComment> findBySubjectTypeAndSubjectIdOrderByCreatedAtAsc(
            PmCommentSubject subjectType, UUID subjectId, Pageable pageable);
}
