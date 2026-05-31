package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.StudentAdminRequest;
import com.example.studentmanagement.dto.StudentDto;
import com.example.studentmanagement.dto.StudentRegistrationRequest;
import com.example.studentmanagement.model.Student;

import java.util.List;

public interface StudentService {

    /** Public self-registration: unique-email check, number + enrollment-year assignment, save. */
    StudentDto register(StudentRegistrationRequest request);

    /** Admin create. */
    StudentDto create(StudentAdminRequest request);

    List<StudentDto> findAll();

    StudentDto findById(Long id);

    StudentDto update(Long id, StudentAdminRequest request);

    void delete(Long id);

    /** Raw entities for the Excel export. */
    List<Student> findAllEntities();
}
