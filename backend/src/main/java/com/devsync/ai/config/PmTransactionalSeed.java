package com.devsync.ai.config;

import com.devsync.ai.model.Membership;
import com.devsync.ai.model.MembershipRole;
import com.devsync.ai.model.Organization;
import com.devsync.ai.model.Role;
import com.devsync.ai.model.User;
import com.devsync.ai.model.pm.ActivityLog;
import com.devsync.ai.model.pm.PmBug;
import com.devsync.ai.model.pm.PmBugSeverity;
import com.devsync.ai.model.pm.PmBugStatus;
import com.devsync.ai.model.pm.PmComment;
import com.devsync.ai.model.pm.PmCommentSubject;
import com.devsync.ai.model.pm.PmSprintStatus;
import com.devsync.ai.model.pm.PmTask;
import com.devsync.ai.model.pm.PmTaskPriority;
import com.devsync.ai.model.pm.PmTaskStatus;
import com.devsync.ai.model.pm.Project;
import com.devsync.ai.model.pm.ProjectMember;
import com.devsync.ai.model.pm.ProjectMgmtStatus;
import com.devsync.ai.model.pm.ProjTeamRole;
import com.devsync.ai.model.pm.Sprint;
import com.devsync.ai.repository.MembershipRepository;
import com.devsync.ai.repository.OrganizationRepository;
import com.devsync.ai.repository.RoleRepository;
import com.devsync.ai.repository.UserRepository;
import com.devsync.ai.repository.pm.ActivityLogRepository;
import com.devsync.ai.repository.pm.PmBugRepository;
import com.devsync.ai.repository.pm.PmCommentRepository;
import com.devsync.ai.repository.pm.PmTaskRepository;
import com.devsync.ai.repository.pm.ProjectMemberRepository;
import com.devsync.ai.repository.pm.ProjectRepository;
import com.devsync.ai.repository.pm.SprintRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "devsync.seed.enabled", havingValue = "true")
public class PmTransactionalSeed {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final PmTaskRepository pmTaskRepository;
    private final PmBugRepository pmBugRepository;
    private final SprintRepository sprintRepository;
    private final PmCommentRepository pmCommentRepository;
    private final ActivityLogRepository activityLogRepository;

    @Value("${devsync.seed.demo-email:pm-demo@devsync.ai}")
    private String demoEmail;

    @Value("${devsync.seed.demo-password:DemoPass123!}")
    private String demoPassword;

    @Value("${devsync.seed.org-slug:devsync-demo}")
    private String demoOrgSlug;

    @Transactional
    public void seedDemoData() {
        if (projectRepository.count() > 0) {
            log.info("PM demo seed skipped (projects already exist).");
            return;
        }

        Organization org = organizationRepository
                .findBySlug(demoOrgSlug)
                .orElseGet(() -> {
                    Organization o = new Organization();
                    o.setName("DevSync Demo Org");
                    o.setSlug(demoOrgSlug);
                    return organizationRepository.save(o);
                });

        Role userRole =
                roleRepository.findByName("USER").orElseThrow(() -> new IllegalStateException("USER role missing"));
        User demo = ensureDemoUser(userRole);

        membershipRepository
                .findByOrganization_IdAndUser_Id(org.getId(), demo.getId())
                .orElseGet(() -> {
                    Membership m = new Membership();
                    m.setOrganization(org);
                    m.setUser(demo);
                    m.setRole(MembershipRole.OWNER);
                    return membershipRepository.save(m);
                });

        Project project = new Project();
        project.setOrganization(org);
        project.setName("Infrastructure Modernization");
        project.setKey("INFMOD");
        project.setDescription("Demo project for DevSync AI PM module.");
        project.setStatus(ProjectMgmtStatus.ACTIVE);
        project.setOwner(demo);
        project = projectRepository.save(project);

        ProjectMember ownerRow = new ProjectMember();
        ownerRow.setProject(project);
        ownerRow.setUser(demo);
        ownerRow.setRole(ProjTeamRole.PROJECT_OWNER);
        projectMemberRepository.save(ownerRow);

        Sprint sprint = new Sprint();
        sprint.setProject(project);
        sprint.setName("Sprint 1 - Foundations");
        sprint.setStartDate(LocalDate.now().minusDays(7));
        sprint.setEndDate(LocalDate.now().plusDays(14));
        sprint.setGoal("Establish delivery cadence.");
        sprint.setStatus(PmSprintStatus.ACTIVE);
        sprint = sprintRepository.save(sprint);

        Sprint completedSprint = new Sprint();
        completedSprint.setProject(project);
        completedSprint.setName("Sprint 0 - Discovery");
        completedSprint.setStartDate(LocalDate.now().minusDays(28));
        completedSprint.setEndDate(LocalDate.now().minusDays(14));
        completedSprint.setGoal("Map deployment and authentication risks.");
        completedSprint.setStatus(PmSprintStatus.COMPLETED);
        completedSprint = sprintRepository.save(completedSprint);

        PmTask t1 = new PmTask();
        t1.setProject(project);
        t1.setSprint(sprint);
        t1.setTitle("Finalize IAM integration");
        t1.setDescription("Wire JWT issuance with org-level RBAC audits.");
        t1.setPriority(PmTaskPriority.HIGH);
        t1.setStatus(PmTaskStatus.IN_PROGRESS);
        t1.setAssignee(demo);
        t1.setDueDate(LocalDate.now().plusDays(5));
        t1.setPosition(1);
        t1 = pmTaskRepository.save(t1);

        PmTask t2 = new PmTask();
        t2.setProject(project);
        t2.setTitle("Bootstrap CI dashboards");
        t2.setPriority(PmTaskPriority.MEDIUM);
        t2.setStatus(PmTaskStatus.BACKLOG);
        t2.setPosition(2);
        t2 = pmTaskRepository.save(t2);

        PmTask t3 = new PmTask();
        t3.setProject(project);
        t3.setSprint(sprint);
        t3.setTitle("Document incident response checklist");
        t3.setPriority(PmTaskPriority.MEDIUM);
        t3.setStatus(PmTaskStatus.TODO);
        t3.setAssignee(demo);
        t3.setDueDate(LocalDate.now().minusDays(2));
        t3.setPosition(3);
        t3 = pmTaskRepository.save(t3);

        PmTask t4 = new PmTask();
        t4.setProject(project);
        t4.setSprint(sprint);
        t4.setTitle("Review deployment topology");
        t4.setPriority(PmTaskPriority.HIGH);
        t4.setStatus(PmTaskStatus.REVIEW);
        t4.setAssignee(demo);
        t4.setDueDate(LocalDate.now().plusDays(3));
        t4.setPosition(4);
        t4 = pmTaskRepository.save(t4);

        PmTask t5 = new PmTask();
        t5.setProject(project);
        t5.setSprint(completedSprint);
        t5.setTitle("Baseline service ownership map");
        t5.setPriority(PmTaskPriority.LOW);
        t5.setStatus(PmTaskStatus.DONE);
        t5.setAssignee(demo);
        t5.setDueDate(LocalDate.now().minusDays(15));
        t5.setPosition(5);
        t5 = pmTaskRepository.save(t5);

        PmBug bug = new PmBug();
        bug.setProject(project);
        bug.setTask(t1);
        bug.setTitle("Session refresh fails intermittently");
        bug.setDescription("Happens during long-running WS connections.");
        bug.setSeverity(PmBugSeverity.CRITICAL);
        bug.setStatus(PmBugStatus.TRIAGED);
        bug.setReporter(demo);
        bug.setAssignee(demo);
        bug = pmBugRepository.save(bug);

        PmBug resolvedBug = new PmBug();
        resolvedBug.setProject(project);
        resolvedBug.setTask(t5);
        resolvedBug.setTitle("Incorrect owner shown in project summary");
        resolvedBug.setDescription("Summary card used stale ownership metadata.");
        resolvedBug.setSeverity(PmBugSeverity.MAJOR);
        resolvedBug.setStatus(PmBugStatus.RESOLVED);
        resolvedBug.setReporter(demo);
        resolvedBug.setAssignee(demo);
        pmBugRepository.save(resolvedBug);

        PmBug blockerBug = new PmBug();
        blockerBug.setProject(project);
        blockerBug.setTask(t3);
        blockerBug.setTitle("Prod deploy gate misses rollback approval");
        blockerBug.setDescription("Rollback approval is required before infra promotion.");
        blockerBug.setSeverity(PmBugSeverity.BLOCKER);
        blockerBug.setStatus(PmBugStatus.OPEN);
        blockerBug.setReporter(demo);
        blockerBug.setAssignee(demo);
        pmBugRepository.save(blockerBug);

        PmComment cTask = new PmComment();
        cTask.setSubjectType(PmCommentSubject.TASK);
        cTask.setSubjectId(t1.getId());
        cTask.setAuthor(demo);
        cTask.setBody("Linked design doc reviewed — ready for implementation spike.");
        pmCommentRepository.save(cTask);

        PmComment cBug = new PmComment();
        cBug.setSubjectType(PmCommentSubject.BUG);
        cBug.setSubjectId(bug.getId());
        cBug.setAuthor(demo);
        cBug.setBody("Captured repro on staging.");
        pmCommentRepository.save(cBug);

        ActivityLog seeded = new ActivityLog();
        seeded.setOrganization(org);
        seeded.setProject(project);
        seeded.setActor(demo);
        seeded.setAction("SEED_COMPLETE");
        seeded.setEntityType("PROJECT");
        seeded.setEntityId(project.getId());
        seeded.setPayload(Map.of("notice", "Demo dataset loaded"));
        activityLogRepository.save(seeded);

        log.info(
                "PM demo seeded: login {} / {} then open org '{}' (slug {}).",
                demoEmail,
                demoPassword,
                org.getName(),
                org.getSlug());
    }

    private User ensureDemoUser(Role userRole) {
        return userRepository
                .findByEmailFetchRoles(demoEmail.toLowerCase())
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(demoEmail.toLowerCase());
                    u.setFullName("PM Demo");
                    u.setExternalAuthSub("seed:" + UUID.randomUUID());
                    u.setPasswordHash(passwordEncoder.encode(demoPassword));
                    u.addRole(userRole);
                    return userRepository.save(u);
                });
    }
}
