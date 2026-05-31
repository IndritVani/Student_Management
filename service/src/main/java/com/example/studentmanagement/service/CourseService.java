package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.CourseDto;
import com.example.studentmanagement.model.Course;
import org.springframework.stereotype.Service;

import java.util.List;

/** Exposes the predefined {@link Course} catalogue as DTOs (for dropdowns and the API). */
@Service
public class CourseService {

    public List<CourseDto> findAll() {
        return java.util.Arrays.stream(Course.values())
                .map(c -> new CourseDto(c.name(), c.getDisplayName()))
                .toList();
    }
}
