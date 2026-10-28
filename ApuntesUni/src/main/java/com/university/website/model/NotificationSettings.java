package com.university.website.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "notification_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class NotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "email_notifications", nullable = false)
    @ToString.Include
    private boolean emailNotifications = true;

    @Column(name = "comment_alerts", nullable = false)
    @ToString.Include
    private boolean commentAlerts = true;

    @Column(name = "rating_alerts", nullable = false)
    @ToString.Include
    private boolean ratingAlerts = true;

    @Column(name = "report_alerts", nullable = false)
    @ToString.Include
    private boolean reportAlerts = true;

    @Column(name = "subject_alerts", nullable = false)
    @ToString.Include
    private boolean subjectAlerts = true;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public NotificationSettings(User user) {
        this.user = user;
        this.emailNotifications = true;
        this.commentAlerts = true;
        this.ratingAlerts = true;
        this.reportAlerts = true;
        this.subjectAlerts = true;
    }

    // Métodos de dominio
    public void enableAll() {
        this.emailNotifications = true;
        this.commentAlerts = true;
        this.ratingAlerts = true;
        this.reportAlerts = true;
        this.subjectAlerts = true;
    }

    public void disableAll() {
        this.emailNotifications = false;
        this.commentAlerts = false;
        this.ratingAlerts = false;
        this.reportAlerts = false;
        this.subjectAlerts = false;
    }

    public void updatePreferences(boolean email, boolean comment, boolean rating, boolean report, boolean subject) {
        this.emailNotifications = email;
        this.commentAlerts = comment;
        this.ratingAlerts = rating;
        this.reportAlerts = report;
        this.subjectAlerts = subject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationSettings that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
