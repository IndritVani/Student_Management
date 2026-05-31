package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.StudentAdminRequest;
import com.example.studentmanagement.dto.StudentDto;
import com.example.studentmanagement.dto.StudentRegistrationRequest;
import com.example.studentmanagement.mapper.StudentMapper;
import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.repository.StudentRepository;
import com.example.studentmanagement.service.exception.DuplicateEmailException;
import com.example.studentmanagement.service.exception.StudentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final StudentNumberGenerator studentNumberGenerator;

    @Override
    public StudentDto register(StudentRegistrationRequest request) {
        requireEmailAvailable(request.getEmail());

        Student student = studentMapper.toEntity(request);
        int year = LocalDate.now().getYear();
        student.setEnrollmentYear(year);
        student.setStudentNumber(studentNumberGenerator.generate(year));

        return studentMapper.toDto(studentRepository.save(student));
    }

    @Override
    public StudentDto create(StudentAdminRequest request) {
        requireEmailAvailable(request.getEmail());

        Student student = studentMapper.toEntity(request);
        if (student.getEnrollmentYear() == null) {
            student.setEnrollmentYear(LocalDate.now().getYear());
        }
        student.setStudentNumber(studentNumberGenerator.generate(student.getEnrollmentYear()));

        return studentMapper.toDto(studentRepository.save(student));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDto> findAll() {
        return studentMapper.toDtoList(studentRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDto findById(Long id) {
        return studentMapper.toDto(getEntity(id));
    }

    @Override
    public StudentDto update(Long id, StudentAdminRequest request) {
        Student student = getEntity(id);
        if (studentRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateEmailException(request.getEmail());
        }

        studentMapper.updateEntityFromAdminRequest(request, student);
        if (student.getEnrollmentYear() == null) {
            student.setEnrollmentYear(LocalDate.now().getYear());
        }

        return studentMapper.toDto(studentRepository.save(student));
    }

    @Override
    public void delete(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new StudentNotFoundException(id);
        }
        studentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findAllEntities() {
        return studentRepository.findAll();
    }

    private Student getEntity(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    private void requireEmailAvailable(String email) {
        if (studentRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }
}
