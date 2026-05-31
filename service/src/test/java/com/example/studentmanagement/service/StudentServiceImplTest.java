package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.StudentAdminRequest;
import com.example.studentmanagement.dto.StudentDto;
import com.example.studentmanagement.dto.StudentRegistrationRequest;
import com.example.studentmanagement.mapper.StudentMapper;
import com.example.studentmanagement.model.Course;
import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.repository.StudentRepository;
import com.example.studentmanagement.service.exception.DuplicateEmailException;
import com.example.studentmanagement.service.exception.StudentNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private StudentNumberGenerator studentNumberGenerator;

    @InjectMocks
    private StudentServiceImpl service;

    @Test
    void register_assignsNumberAndCurrentYearThenSaves() {
        StudentRegistrationRequest request = StudentRegistrationRequest.builder()
                .firstName("Ada").lastName("Lovelace").email("ada@example.com")
                .course(Course.COMPUTER_SCIENCE.name()).build();

        Student entity = new Student();
        StudentDto dto = StudentDto.builder().studentNumber("STU-2025-0001").build();

        when(studentRepository.existsByEmail("ada@example.com")).thenReturn(false);
        when(studentMapper.toEntity(request)).thenReturn(entity);
        when(studentNumberGenerator.generate(anyInt())).thenReturn("STU-2025-0001");
        when(studentRepository.save(entity)).thenReturn(entity);
        when(studentMapper.toDto(entity)).thenReturn(dto);

        StudentDto result = service.register(request);

        assertThat(result).isSameAs(dto);
        assertThat(entity.getStudentNumber()).isEqualTo("STU-2025-0001");
        assertThat(entity.getEnrollmentYear()).isEqualTo(LocalDate.now().getYear());
        verify(studentRepository).save(entity);
    }

    @Test
    void register_duplicateEmail_throwsAndDoesNotSave() {
        StudentRegistrationRequest request = StudentRegistrationRequest.builder()
                .email("taken@example.com").build();
        when(studentRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(studentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void create_usesRequestYearAndGeneratesNumber() {
        StudentAdminRequest request = StudentAdminRequest.builder()
                .firstName("Grace").lastName("Hopper").email("grace@example.com")
                .course(Course.DATA_SCIENCE.name()).enrollmentYear(2024).gpa(3.8).build();

        Student entity = Student.builder().enrollmentYear(2024).build();
        StudentDto dto = StudentDto.builder().studentNumber("STU-2024-0005").build();

        when(studentRepository.existsByEmail("grace@example.com")).thenReturn(false);
        when(studentMapper.toEntity(request)).thenReturn(entity);
        when(studentNumberGenerator.generate(2024)).thenReturn("STU-2024-0005");
        when(studentRepository.save(entity)).thenReturn(entity);
        when(studentMapper.toDto(entity)).thenReturn(dto);

        StudentDto result = service.create(request);

        assertThat(result.getStudentNumber()).isEqualTo("STU-2024-0005");
        assertThat(entity.getStudentNumber()).isEqualTo("STU-2024-0005");
        verify(studentNumberGenerator).generate(2024);
    }

    @Test
    void findById_missing_throwsNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(StudentNotFoundException.class);
    }

    @Test
    void update_appliesChanges() {
        StudentAdminRequest request = StudentAdminRequest.builder()
                .firstName("New").lastName("Name").email("new@example.com")
                .course(Course.CYBERSECURITY.name()).enrollmentYear(2023).build();

        Student existing = Student.builder().id(1L).email("old@example.com").enrollmentYear(2023).build();
        StudentDto dto = StudentDto.builder().id(1L).email("new@example.com").build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false);
        when(studentRepository.save(existing)).thenReturn(existing);
        when(studentMapper.toDto(existing)).thenReturn(dto);

        StudentDto result = service.update(1L, request);

        assertThat(result).isSameAs(dto);
        verify(studentMapper).updateEntityFromAdminRequest(request, existing);
        verify(studentRepository).save(existing);
    }

    @Test
    void update_duplicateEmail_throws() {
        StudentAdminRequest request = StudentAdminRequest.builder()
                .email("dupe@example.com").enrollmentYear(2023).build();
        Student existing = Student.builder().id(1L).email("old@example.com").build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.existsByEmailAndIdNot("dupe@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(DuplicateEmailException.class);
        verify(studentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void delete_existing_deletes() {
        when(studentRepository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(studentRepository).deleteById(1L);
    }

    @Test
    void delete_missing_throwsNotFound() {
        when(studentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(StudentNotFoundException.class);
        verify(studentRepository, never()).deleteById(99L);
    }
}
