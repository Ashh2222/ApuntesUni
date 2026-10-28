package com.university.website.model;

import com.university.website.model.enums.StudentProgramStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(
    name = "student_programs",
    uniqueConstraints = @UniqueConstraint(name = "uk_student_program", columnNames = {"student_id", "academic_program_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class StudentProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_program_id", nullable = false)
    private AcademicProgram academicProgram;

    @Column(nullable = false)
    @ToString.Include
    private LocalDate enrollmentDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private StudentProgramStatus status = StudentProgramStatus.ACTIVE;

    @Column(name = "is_primary", nullable = false)
    @ToString.Include
    private boolean primaryProgram;

    @Column(name = "current_semester", nullable = false)
    @ToString.Include
    private int currentSemester = 1;

    @Column(name = "academic_plan_year")
    private Integer academicPlanYear;

    public StudentProgram(Student student, AcademicProgram academicProgram, boolean primaryProgram, int currentSemester) {
        this.student = student;
        this.academicProgram = academicProgram;
        this.primaryProgram = primaryProgram;
        this.currentSemester = currentSemester;
        this.enrollmentDate = LocalDate.now();
        this.status = StudentProgramStatus.ACTIVE;
    }

    // Métodos de dominio
    public void markAsPrimary() {
        this.primaryProgram = true;
    }

    public boolean isPrimary() {
        return this.primaryProgram;
    }

    public void unmarkAsPrimary() {
        this.primaryProgram = false;
    }

    public void updateStatus(StudentProgramStatus newStatus) {
        if (newStatus != null) {
            this.status = newStatus;
        }
    }

    public void advanceSemester() {
        this.currentSemester++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentProgram that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
