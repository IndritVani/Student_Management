package com.example.studentmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Public registration form. No id, no studentNumber, no gpa &mdash; those are
 * assigned/managed by the system or the admin.
 * {@code course} carries the {@link com.example.studentmanagement.dto.CourseDto#getName() enum name}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRegistrationRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Please choose a course")
    private String course;
}
