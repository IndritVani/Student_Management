package com.example.studentmanagement.mapper;

import com.example.studentmanagement.dto.StudentAdminRequest;
import com.example.studentmanagement.dto.StudentDto;
import com.example.studentmanagement.dto.StudentRegistrationRequest;
import com.example.studentmanagement.model.Course;
import com.example.studentmanagement.model.Student;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the real MapStruct-generated implementation ({@code StudentMapperImpl}),
 * which proves the Lombok + MapStruct annotation-processor wiring is correct.
 */
class StudentMapperTest {

    private final StudentMapper mapper = new StudentMapperImpl();

    @Test
    void toDto_mapsFieldsAndCourseDisplayName() {
        Student entity = Student.builder()
                .id(1L)
                .studentNumber("STU-2025-0001")
                .firstName("Ada").lastName("Lovelace")
                .email("ada@example.com")
                .dateOfBirth(LocalDate.of(2003, 5, 12))
                .course(Course.COMPUTER_SCIENCE)
                .enrollmentYear(2025)
                .gpa(3.9)
                .build();

        StudentDto dto = mapper.toDto(entity);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStudentNumber()).isEqualTo("STU-2025-0001");
        assertThat(dto.getFirstName()).isEqualTo("Ada");
        assertThat(dto.getEmail()).isEqualTo("ada@example.com");
        assertThat(dto.getCourse()).isEqualTo("COMPUTER_SCIENCE");
        assertThat(dto.getCourseDisplayName()).isEqualTo("Computer Science");
        assertThat(dto.getEnrollmentYear()).isEqualTo(2025);
        assertThat(dto.getGpa()).isEqualTo(3.9);
    }

    @Test
    void toEntity_fromRegistration_mapsCourseStringToEnumAndLeavesGeneratedFieldsNull() {
        StudentRegistrationRequest request = StudentRegistrationRequest.builder()
                .firstName("Alan").lastName("Turing")
                .email("alan@example.com")
                .dateOfBirth(LocalDate.of(2002, 6, 23))
                .course("SOFTWARE_ENGINEERING")
                .build();

        Student entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getStudentNumber()).isNull();
        assertThat(entity.getEnrollmentYear()).isNull();
        assertThat(entity.getGpa()).isNull();
        assertThat(entity.getCourse()).isEqualTo(Course.SOFTWARE_ENGINEERING);
        assertThat(entity.getFirstName()).isEqualTo("Alan");
    }

    @Test
    void updateEntityFromAdminRequest_keepsIdAndStudentNumber() {
        Student entity = Student.builder()
                .id(7L).studentNumber("STU-2024-0007")
                .firstName("Old").lastName("Name").email("old@example.com")
                .course(Course.DATA_SCIENCE).enrollmentYear(2024).build();

        StudentAdminRequest request = StudentAdminRequest.builder()
                .firstName("New").lastName("Name").email("new@example.com")
                .course("CYBERSECURITY").enrollmentYear(2025).gpa(3.5).build();

        mapper.updateEntityFromAdminRequest(request, entity);

        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getStudentNumber()).isEqualTo("STU-2024-0007");
        assertThat(entity.getFirstName()).isEqualTo("New");
        assertThat(entity.getEmail()).isEqualTo("new@example.com");
        assertThat(entity.getCourse()).isEqualTo(Course.CYBERSECURITY);
        assertThat(entity.getEnrollmentYear()).isEqualTo(2025);
        assertThat(entity.getGpa()).isEqualTo(3.5);
    }
}
