package com.example.studentmanagement.web.config;

import com.example.studentmanagement.dto.StudentAdminRequest;
import com.example.studentmanagement.model.Course;
import com.example.studentmanagement.repository.StudentRepository;
import com.example.studentmanagement.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds a few sample students on first boot so the admin list isn't empty.
 * No-ops when data already exists (so it's safe with file-based H2 across restarts).
 * Disabled under the {@code test} profile.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final StudentService studentService;
    private final StudentRepository studentRepository;

    @Override
    public void run(String... args) {
        if (studentRepository.count() > 0) {
            return;
        }

        List<StudentAdminRequest> samples = List.of(
                StudentAdminRequest.builder()
                        .firstName("Ada").lastName("Lovelace").email("ada.lovelace@example.com")
                        .dateOfBirth(LocalDate.of(2003, 5, 12))
                        .course(Course.COMPUTER_SCIENCE.name()).enrollmentYear(2024).gpa(3.9).build(),
                StudentAdminRequest.builder()
                        .firstName("Alan").lastName("Turing").email("alan.turing@example.com")
                        .dateOfBirth(LocalDate.of(2002, 6, 23))
                        .course(Course.SOFTWARE_ENGINEERING.name()).enrollmentYear(2024).gpa(3.7).build(),
                StudentAdminRequest.builder()
                        .firstName("Grace").lastName("Hopper").email("grace.hopper@example.com")
                        .dateOfBirth(LocalDate.of(2004, 12, 9))
                        .course(Course.DATA_SCIENCE.name()).enrollmentYear(2025).gpa(3.8).build());

        samples.forEach(studentService::create);
        log.info("Seeded {} sample students", samples.size());
    }
}
