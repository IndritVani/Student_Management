package com.example.studentmanagement.service.exception;

/** Thrown when a student lookup by id finds nothing. Mapped to HTTP 404 at the web layer. */
public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(Long id) {
        super("Student not found with id " + id);
    }
}
