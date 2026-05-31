package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.CourseDto;
import com.example.studentmanagement.model.Course;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CourseServiceTest {

    private final CourseService courseService = new CourseService();

    @Test
    void findAll_returnsAllSevenCoursesWithDisplayNames() {
        List<CourseDto> courses = courseService.findAll();

        assertThat(courses).hasSize(Course.values().length);
        assertThat(courses).extracting(CourseDto::getName)
                .contains("COMPUTER_SCIENCE", "DATA_SCIENCE", "ELECTRICAL_ENGINEERING");
        assertThat(courses).filteredOn(c -> c.getName().equals("COMPUTER_SCIENCE"))
                .extracting(CourseDto::getDisplayName)
                .containsExactly("Computer Science");
    }
}
