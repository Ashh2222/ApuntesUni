package com.university.website.service.impl;

import com.university.website.exception.BadRequestException;
import com.university.website.exception.ResourceNotFoundException;
import com.university.website.model.Enrollment;
import com.university.website.model.Student;
import com.university.website.model.Subject;
import com.university.website.model.enums.EnrollmentStatus;
import com.university.website.repository.EnrollmentRepository;
import com.university.website.repository.StudentRepository;
import com.university.website.repository.SubjectRepository;
import com.university.website.service.EnrollmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    public EnrollmentServiceImpl(
            EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository,
            SubjectRepository subjectRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public Enrollment enroll(Long studentId, Long subjectId, String academicPeriod) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado: " + studentId));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada: " + subjectId));

        if (enrollmentRepository.existsByStudent_IdAndSubject_IdAndStatus(studentId, subjectId, EnrollmentStatus.ACTIVE)) {
            throw new BadRequestException("Ya estás inscrito activamente en la materia: " + subject.getName());
        }

        Enrollment enrollment = new Enrollment(student, subject, academicPeriod != null ? academicPeriod : "2026-1");
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public void drop(Long studentId, Long subjectId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudent_IdAndStatus(studentId, EnrollmentStatus.ACTIVE);
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getSubject().getId().equals(subjectId)) {
                enrollment.drop();
                enrollmentRepository.save(enrollment);
                return;
            }
        }
        throw new ResourceNotFoundException("No se encontró inscripción activa para dar de baja.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> findActiveEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudent_IdAndStatus(studentId, EnrollmentStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> findActiveSubjectsByStudent(Long studentId) {
        return enrollmentRepository.findByStudent_IdAndStatus(studentId, EnrollmentStatus.ACTIVE)
                .stream()
                .map(Enrollment::getSubject)
                .toList();
    }
}
