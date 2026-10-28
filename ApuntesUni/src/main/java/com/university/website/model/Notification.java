package com.university.website.model;

import com.university.website.model.enums.NotificationType;
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
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private NotificationType type = NotificationType.SYSTEM_ALERT;

    @Column(length = 200)
    @ToString.Include
    private String title;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    @ToString.Include
    private String message;

    @Column(name = "created_at", nullable = false)
    @ToString.Include
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_read", nullable = false)
    @ToString.Include
    private boolean read = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_note_id")
    private Note relatedNote;

    public Notification(User user, NotificationType type, String title, String message, Long referenceId, String referenceType) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    // Métodos de dominio
    public void markAsRead() {
        this.read = true;
    }

    public void markAsUnread() {
        this.read = false;
    }

    public void setType(String typeStr) {
        try {
            this.type = NotificationType.valueOf(typeStr);
        } catch (Exception e) {
            this.type = NotificationType.SYSTEM_ALERT;
        }
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
        if (!(o instanceof Notification that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
