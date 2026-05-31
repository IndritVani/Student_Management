package com.example.studentmanagement.service;

import com.example.studentmanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builds student numbers of the form {@code STU-{year}-{4-digit sequence}}, e.g. {@code STU-2025-0007}.
 *
 * <p>The sequence starts from {@code countByEnrollmentYear(year) + 1}. Because that count can collide
 * after deletions, we probe forward until we find a number that is not yet taken.
 */
@Component
@RequiredArgsConstructor
public class StudentNumberGenerator {

    static final String PREFIX = "STU";

    private final StudentRepository studentRepository;

    public String generate(int year) {
        long next = studentRepository.countByEnrollmentYear(year) + 1;
        String candidate = format(year, next);
        while (studentRepository.existsByStudentNumber(candidate)) {
            next++;
            candidate = format(year, next);
        }
        return candidate;
    }

    private String format(int year, long sequence) {
        return String.format("%s-%d-%04d", PREFIX, year, sequence);
    }
}
