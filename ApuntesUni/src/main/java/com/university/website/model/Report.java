package com.university.website.model;

import com.university.website.model.enums.ReportReason;
import com.university.website.model.enums.ReportSeverity;
import com.university.website.model.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private ReportReason reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private ReportSeverity severity = ReportSeverity.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private ReportStatus status = ReportStatus.PENDING;

    @Column(length = 1000)
    private String description;

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @Column(name = "created_at", nullable = false)
    @ToString.Include
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_id")
    private User resolvedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    public Report(User reporter, Note note, ReportReason reason, ReportSeverity severity, String description) {
        this.user = reporter;
        this.note = note;
        this.reason = reason;
        this.severity = severity;
        this.description = description;
        this.status = ReportStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Métodos de dominio
    public void assignModerator(User moderator) {
        this.resolvedBy = moderator;
        this.status = ReportStatus.IN_REVIEW;
    }

    public void resolve(String resolutionNotes, User admin) {
        this.resolvedBy = admin;
        this.resolutionNotes = resolutionNotes;
        this.status = ReportStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }

    public void dismiss(String resolutionNotes, User admin) {
        this.resolvedBy = admin;
        this.resolutionNotes = resolutionNotes;
        this.status = ReportStatus.DISMISSED;
        this.resolvedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return ReportStatus.PENDING.equals(this.status);
    }

    public boolean isReviewed() {
        return ReportStatus.RESOLVED.equals(this.status) || ReportStatus.DISMISSED.equals(this.status);
    }

    public void setReviewed(boolean reviewed) {
        if (reviewed && this.status == ReportStatus.PENDING) {
            this.status = ReportStatus.RESOLVED;
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
        if (!(o instanceof Report report)) return false;
        return id != null && id.equals(report.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
