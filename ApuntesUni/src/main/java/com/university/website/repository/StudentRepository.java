package com.university.website.repository;

import com.university.website.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentCode(String studentCode);

    Optional<Student> findByInstitutionalEmailIgnoreCase(String email);

    Optional<Student> findByEmailIgnoreCase(String email);
}
