package com.university.website.service;

import com.university.website.model.Enrollment;
import com.university.website.model.Subject;

import java.util.List;

public interface EnrollmentService {

    Enrollment enroll(Long studentId, Long subjectId, String academicPeriod);

    void drop(Long studentId, Long subjectId);

    List<Enrollment> findActiveEnrollmentsByStudent(Long studentId);

    List<Subject> findActiveSubjectsByStudent(Long studentId);
}
