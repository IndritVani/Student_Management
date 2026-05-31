package com.example.studentmanagement.service.exception;

/** Thrown when an email is already registered. Mapped to HTTP 400 / a form field error. */
public class DuplicateEmailException extends RuntimeException {

    private final String email;

    public DuplicateEmailException(String email) {
        super("Email is already registered: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
