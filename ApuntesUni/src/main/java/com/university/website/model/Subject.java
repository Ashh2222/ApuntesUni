package com.university.website.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "subjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    @ToString.Include
    private String code;

    @NotBlank
    @Column(nullable = false)
    @ToString.Include
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    @ToString.Include
    private int credits = 3;

    @Column(nullable = false)
    @ToString.Include
    private boolean active = true;

    // Relación N:M con carreras (una materia puede pertenecer a varios programas académicos)
    @ManyToMany
    @JoinTable(
            name = "subject_academic_programs",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "academic_program_id")
    )
    private Set<AcademicProgram> academicPrograms = new HashSet<>();

    // Relación recursiva N:M para prerrequisitos académicos (ej. Programación I es prerrequisito de Programación II)
    @ManyToMany
    @JoinTable(
            name = "subject_prerequisites",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_id")
    )
    private Set<Subject> prerequisiteSubjects = new HashSet<>();

    // Temas académicos en los que se desglosa la materia
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Topic> topics = new ArrayList<>();

    // Inscripciones activas o históricas de estudiantes
    @OneToMany(mappedBy = "subject")
    private Set<Enrollment> enrollments = new HashSet<>();

    // Apuntes asociados a esta materia
    @OneToMany(mappedBy = "subject")
    private List<Note> notes = new ArrayList<>();

    public Subject(String code, String name, int credits) {
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.active = true;
    }

    // Métodos de dominio
    public void addAcademicProgram(AcademicProgram program) {
        if (program != null) {
            this.academicPrograms.add(program);
        }
    }

    public void removeAcademicProgram(AcademicProgram program) {
        if (program != null) {
            this.academicPrograms.remove(program);
        }
    }

    public void addPrerequisite(Subject prerequisite) {
        if (prerequisite != null && !prerequisite.equals(this)) {
            this.prerequisiteSubjects.add(prerequisite);
        }
    }

    public void removePrerequisite(Subject prerequisite) {
        if (prerequisite != null) {
            this.prerequisiteSubjects.remove(prerequisite);
        }
    }

    public void addTopic(Topic topic) {
        if (topic != null) {
            this.topics.add(topic);
            topic.setSubject(this);
        }
    }

    public void removeTopic(Topic topic) {
        if (topic != null) {
            this.topics.remove(topic);
            topic.setSubject(null);
        }
    }

    public void addNote(Note note) {
        if (note != null) {
            this.notes.add(note);
            note.setSubject(this);
        }
    }

    // Equals y HashCode basados en la clave natural inmutable 'code'
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subject subject)) return false;
        return code != null && code.equalsIgnoreCase(subject.getCode());
    }

    @Override
    public int hashCode() {
        return code != null ? code.toLowerCase().hashCode() : getClass().hashCode();
    }
}
