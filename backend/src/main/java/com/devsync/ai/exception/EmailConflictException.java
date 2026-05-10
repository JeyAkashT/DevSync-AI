package com.devsync.ai.exception;

public class EmailConflictException extends RuntimeException {

    public EmailConflictException(String email) {
        super("Email already registered: " + email);
    }
}
