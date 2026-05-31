package com.example.studentmanagement.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Admin add/edit form &mdash; everything editable.
 * {@code course} carries the {@link com.example.studentmanagement.dto.CourseDto#getName() enum name}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAdminRequest {

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

    @NotNull(message = "Enrollment year is required")
    @Min(value = 1900, message = "Enrollment year looks too small")
    @Max(value = 2100, message = "Enrollment year looks too large")
    private Integer enrollmentYear;

    @DecimalMin(value = "0.0", message = "GPA cannot be below 0.0")
    @DecimalMax(value = "4.0", message = "GPA cannot be above 4.0")
    private Double gpa;
}
