package com.example.studentmanagement.service;

import com.example.studentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentNumberGeneratorTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentNumberGenerator generator;

    @Test
    void generate_buildsZeroPaddedSequenceFromCount() {
        when(studentRepository.countByEnrollmentYear(2025)).thenReturn(6L);
        when(studentRepository.existsByStudentNumber("STU-2025-0007")).thenReturn(false);

        assertThat(generator.generate(2025)).isEqualTo("STU-2025-0007");
    }

    @Test
    void generate_skipsNumbersAlreadyTaken() {
        when(studentRepository.countByEnrollmentYear(2025)).thenReturn(0L);
        when(studentRepository.existsByStudentNumber("STU-2025-0001")).thenReturn(true);
        when(studentRepository.existsByStudentNumber("STU-2025-0002")).thenReturn(false);

        assertThat(generator.generate(2025)).isEqualTo("STU-2025-0002");
    }
}
