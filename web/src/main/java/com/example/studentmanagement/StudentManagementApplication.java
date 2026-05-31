package com.example.studentmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point. Because it sits in {@code com.example.studentmanagement}, component
 * scanning, entity scanning and repository scanning automatically cover every module
 * ({@code .model}, {@code .dto}, {@code .mapper}, {@code .repository}, {@code .excel},
 * {@code .service}, {@code .web}).
 */
@SpringBootApplication
public class StudentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentManagementApplication.class, args);
    }
}
