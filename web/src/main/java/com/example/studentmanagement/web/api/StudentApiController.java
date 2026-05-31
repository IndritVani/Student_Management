package com.example.studentmanagement.web.api;

import com.example.studentmanagement.dto.StudentAdminRequest;
import com.example.studentmanagement.dto.StudentDto;
import com.example.studentmanagement.excel.StudentExcelExporter;
import com.example.studentmanagement.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Admin CRUD over students + Excel export")
public class StudentApiController {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final StudentService studentService;
    private final StudentExcelExporter excelExporter;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a student (admin)")
    public StudentDto create(@Valid @RequestBody StudentAdminRequest request) {
        return studentService.create(request);
    }

    @GetMapping
    @Operation(summary = "List all students (admin)")
    public List<StudentDto> list() {
        return studentService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one student by id (admin)")
    public StudentDto get(@PathVariable Long id) {
        return studentService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a student (admin)")
    public StudentDto update(@PathVariable Long id, @Valid @RequestBody StudentAdminRequest request) {
        return studentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a student (admin)")
    public void delete(@PathVariable Long id) {
        studentService.delete(id);
    }

    @GetMapping("/export")
    @Operation(summary = "Export all students as an .xlsx file (admin)")
    public ResponseEntity<byte[]> export() {
        byte[] body = excelExporter.export(studentService.findAllEntities());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"students.xlsx\"")
                .contentType(MediaType.parseMediaType(XLSX_CONTENT_TYPE))
                .body(body);
    }
}
