package com.university.website.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(length = 30)
    @ToString.Include
    private String code;

    @NotBlank
    @Column(nullable = false)
    @ToString.Include
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "order_index", nullable = false)
    @ToString.Include
    private int orderIndex = 1;

    @Column(name = "learning_objective", length = 500)
    private String learningObjective;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @OneToMany(mappedBy = "topic")
    private List<Note> notes = new ArrayList<>();

    public Topic(String code, String name, String description, int orderIndex, Subject subject) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.orderIndex = orderIndex;
        this.subject = subject;
    }

    // Métodos de dominio
    public void updateDetails(String name, String description, int orderIndex, String learningObjective) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.description = description;
        this.orderIndex = orderIndex;
        this.learningObjective = learningObjective;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Topic topic)) return false;
        return id != null && id.equals(topic.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
