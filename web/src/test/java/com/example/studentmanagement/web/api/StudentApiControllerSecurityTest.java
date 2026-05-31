package com.example.studentmanagement.web.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Drives the real application context through the security filter chain to verify the rules from
 * {@code SecurityConfig}: {@code /api/courses} is public, {@code /api/students} needs an admin.
 * Uses the {@code test} profile (no data seeding).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentApiControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void courses_arePublic() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[0].name").value("COMPUTER_SCIENCE"))
                .andExpect(jsonPath("$[0].displayName").value("Computer Science"));
    }

    @Test
    void students_withoutAuth_redirectToLogin() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void students_asAdmin_ok() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
