package com.university.website.model;

import com.university.website.model.enums.EnrollmentStatus;
import com.university.website.model.enums.StudentProgramStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class Student extends User {

    @Column(name = "student_code", unique = true)
    @ToString.Include
    private String studentCode;

    @Column(name = "institutional_email")
    @ToString.Include
    private String institutionalEmail;

    @Column(name = "entry_year")
    @ToString.Include
    private Integer entryYear;

    @Column(name = "reputation_points", nullable = false)
    @ToString.Include
    private int reputationPoints = 0;

    @Column(name = "verified_student", nullable = false)
    @ToString.Include
    private boolean verifiedStudent = false;

    @Column(name = "academic_interests", length = 500)
    private String academicInterests;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentProgram> studentPrograms = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Enrollment> enrollments = new HashSet<>();

    // Métodos de dominio
    public StudentProgram enrollInProgram(AcademicProgram program, boolean isPrimary, int currentSemester) {
        if (isPrimary) {
            // Si la nueva carrera es la principal, desmarcar cualquier carrera principal previa
            studentPrograms.forEach(StudentProgram::unmarkAsPrimary);
        }
        StudentProgram studentProgram = new StudentProgram(this, program, isPrimary, currentSemester);
        this.studentPrograms.add(studentProgram);
        return studentProgram;
    }

    public void unenrollFromProgram(AcademicProgram program) {
        this.studentPrograms.removeIf(sp -> sp.getAcademicProgram().equals(program));
    }

    public Optional<StudentProgram> getPrimaryProgram() {
        return this.studentPrograms.stream()
                .filter(sp -> sp.isPrimary() && sp.getStatus() == StudentProgramStatus.ACTIVE)
                .findFirst();
    }

    public void addReputation(int points) {
        this.reputationPoints += points;
    }

    public Enrollment enrollInSubject(Subject subject, String academicPeriod) {
        Enrollment enrollment = new Enrollment(this, subject, academicPeriod);
        this.enrollments.add(enrollment);
        return enrollment;
    }

    public void unenrollFromSubject(Subject subject) {
        this.enrollments.removeIf(e -> e.getSubject().equals(subject) && e.getStatus() == EnrollmentStatus.ACTIVE);
    }
}
