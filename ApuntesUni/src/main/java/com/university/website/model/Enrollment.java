package com.university.website.model;

import com.university.website.model.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(
    name = "enrollments",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_student_subject_period",
        columnNames = {"student_id", "subject_id", "academic_period"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(nullable = false)
    @ToString.Include
    private LocalDate enrollmentDate = LocalDate.now();

    @Column(name = "academic_period", nullable = false, length = 20)
    @ToString.Include
    private String academicPeriod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "academic_year")
    private Integer academicYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    public Enrollment(Student student, Subject subject, String academicPeriod) {
        this.student = student;
        this.subject = subject;
        this.academicPeriod = academicPeriod;
        this.enrollmentDate = LocalDate.now();
        this.status = EnrollmentStatus.ACTIVE;
        this.academicYear = LocalDate.now().getYear();
    }

    // Métodos de dominio
    public void drop() {
        this.status = EnrollmentStatus.DROPPED;
    }

    public void complete() {
        this.status = EnrollmentStatus.COMPLETED;
    }

    public boolean isActive() {
        return EnrollmentStatus.ACTIVE.equals(this.status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enrollment that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
