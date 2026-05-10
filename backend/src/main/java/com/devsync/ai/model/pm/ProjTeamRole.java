package com.devsync.ai.model.pm;

/**
 * Lowest ordinal ({@link #VIEWER}) grants least privilege; ascending for {@link Comparable} checks via
 * {@link Enum#ordinal()}.
 */
public enum ProjTeamRole {
    VIEWER,
    MEMBER,
    PROJECT_ADMIN,
    PROJECT_OWNER
}
