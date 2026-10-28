package com.university.website.model;

import com.university.website.model.enums.ProgramType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "academic_programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class AcademicProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    //codigo unico de la carrera
    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    @ToString.Include
    private String code;


    @NotBlank
    @Column(nullable = false, unique = true)
    @ToString.Include
    private String name;

    @Column(length = 1000)
    private String description;

    @Column
    @ToString.Include
    private String faculty;

    @Column(name = "total_semesters")
    private Integer totalSemesters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private ProgramType type = ProgramType.DEGREE;

    @Column(nullable = false)
    @ToString.Include
    private boolean active = true;

    // Relación reflexiva para carreras afines (ej. Ingeniería de Sistemas <-> Ingeniería de Software)
    @ManyToMany
    @JoinTable(
            name = "academic_program_affinities",
            joinColumns = @JoinColumn(name = "program_id"),
            inverseJoinColumns = @JoinColumn(name = "related_program_id")
    )
    private Set<AcademicProgram> relatedPrograms = new HashSet<>();

    // Asociación con estudiantes adscritos mediante la entidad intermedia
    @OneToMany(mappedBy = "academicProgram")
    private Set<StudentProgram> studentPrograms = new HashSet<>();

    public AcademicProgram(String code, String name, ProgramType type) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.active = true;
    }

    // Métodos de dominio
    public void addRelatedProgram(AcademicProgram program) {
        if (program != null && !program.equals(this)) {
            this.relatedPrograms.add(program);
            program.getRelatedPrograms().add(this);
        }
    }

    public void removeRelatedProgram(AcademicProgram program) {
        if (program != null) {
            this.relatedPrograms.remove(program);
            program.getRelatedPrograms().remove(this);
        }
    }

    public boolean isRelatedTo(AcademicProgram program) {
        return program != null && this.relatedPrograms.contains(program);
    }

    // Equals y HashCode basados en la clave natural inmutable 'code'
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AcademicProgram that)) return false;
        return code != null && code.equalsIgnoreCase(that.getCode());
    }

    @Override
    public int hashCode() {
        return code != null ? code.toLowerCase().hashCode() : getClass().hashCode();
    }
}
