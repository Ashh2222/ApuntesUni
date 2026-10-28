package com.university.website.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    @ToString.Include
    private String content;

    @Column(name = "created_at", nullable = false)
    @ToString.Include
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @ToString.Include
    private boolean deleted = false;

    @Column(nullable = false)
    @ToString.Include
    private boolean edited = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    public Comment(User user, Note note, String content) {
        this.user = user;
        this.note = note;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.deleted = false;
        this.edited = false;
    }

    // Métodos de dominio
    public void edit(String newContent) {
        if (newContent != null && !newContent.isBlank()) {
            this.content = newContent;
            this.edited = true;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void softDelete() {
        this.deleted = true;
        this.content = "[Comentario eliminado]";
        this.updatedAt = LocalDateTime.now();
    }

    public String getText() {
        return this.content;
    }

    public void setText(String text) {
        this.content = text;
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
        if (!(o instanceof Comment comment)) return false;
        return id != null && id.equals(comment.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
