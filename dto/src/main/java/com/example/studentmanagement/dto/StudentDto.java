package com.example.studentmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Full read model returned to the UI and the API.
 * {@code course} is the enum constant name; {@code courseDisplayName} is the label.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDto {
    private Long id;
    private String studentNumber;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private String course;
    private String courseDisplayName;
    private Integer enrollmentYear;
    private Double gpa;
}
