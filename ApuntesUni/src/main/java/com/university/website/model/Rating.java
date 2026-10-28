package com.university.website.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "ratings",
    uniqueConstraints = @UniqueConstraint(name = "uk_user_note_rating", columnNames = {"user_id", "note_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Min(1)
    @Max(5)
    @Column(name = "rating_value", nullable = false)
    @ToString.Include
    private int value;

    @Column(name = "feedback_note", length = 255)
    private String feedbackNote;

    @Column(name = "created_at", nullable = false)
    @ToString.Include
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    public Rating(User user, Note note, int value) {
        setValue(value);
        this.user = user;
        this.note = note;
        this.createdAt = LocalDateTime.now();
    }

    public Rating(User user, Note note, int value, String feedbackNote) {
        this(user, note, value);
        this.feedbackNote = feedbackNote;
    }

    // Métodos de dominio
    public void updateValue(int newValue, String feedback) {
        if (newValue < 1 || newValue > 5) {
            throw new IllegalArgumentException("La calificación debe estar comprendida entre 1 y 5 estrellas.");
        }
        this.value = newValue;
        this.feedbackNote = feedback;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getDate() {
        return this.createdAt;
    }

    public void setDate(LocalDateTime date) {
        this.createdAt = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rating rating)) return false;
        return id != null && id.equals(rating.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
