package com.example.studentmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: boots the full application context (component scan across all modules,
 * MapStruct mapper bean, JPA/H2, security) to prove everything wires together.
 */
@SpringBootTest
@ActiveProfiles("test")
class StudentManagementApplicationTests {

    @Test
    void contextLoads() {
    }
}
