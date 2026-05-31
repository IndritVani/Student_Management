package com.example.studentmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One selectable course for the dropdown.
 * Example: {@code { "name": "COMPUTER_SCIENCE", "displayName": "Computer Science" }}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    /** Enum constant name &mdash; the option value submitted by forms / the API. */
    private String name;
    /** Human-readable label shown to the user. */
    private String displayName;
}
