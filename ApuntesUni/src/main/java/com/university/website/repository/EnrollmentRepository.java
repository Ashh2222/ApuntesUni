package com.university.website.repository;

import com.university.website.model.Enrollment;
import com.university.website.model.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudent_Id(Long studentId);

    List<Enrollment> findByStudent_IdAndStatus(Long studentId, EnrollmentStatus status);

    Optional<Enrollment> findByStudent_IdAndSubject_IdAndAcademicPeriod(Long studentId, Long subjectId, String academicPeriod);

    boolean existsByStudent_IdAndSubject_IdAndStatus(Long studentId, Long subjectId, EnrollmentStatus status);
}
