package com.example.studentmanagement.web.api;

import com.example.studentmanagement.dto.CourseDto;
import com.example.studentmanagement.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "The predefined course catalogue (public)")
public class CourseApiController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "List the 7 predefined courses")
    public List<CourseDto> list() {
        return courseService.findAll();
    }
}
