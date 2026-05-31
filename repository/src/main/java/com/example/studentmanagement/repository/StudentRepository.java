package com.example.studentmanagement.repository;

import com.example.studentmanagement.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmail(String email);

    /** Email-uniqueness check that ignores the student being updated. */
    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByStudentNumber(String studentNumber);

    /** Feeds the student-number generator's sequence. */
    long countByEnrollmentYear(int enrollmentYear);
}
