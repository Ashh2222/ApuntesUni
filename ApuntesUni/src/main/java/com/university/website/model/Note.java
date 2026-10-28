package com.university.website.model;

import com.university.website.model.enums.FileFormat;
import com.university.website.model.enums.NoteStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @NotBlank
    @Column(nullable = false)
    @ToString.Include
    private String title;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @Column(name = "original_file_name")
    @ToString.Include
    private String originalFileName;

    @NotBlank
    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "file_size_bytes", nullable = false)
    @ToString.Include
    private Long fileSizeBytes = 0L;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "page_count", nullable = false)
    @ToString.Include
    private int pageCount = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private NoteStatus status = NoteStatus.PUBLISHED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private FileFormat format;

    @Column(name = "views_count", nullable = false)
    @ToString.Include
    private int viewsCount = 0;

    @Column(name = "tags", length = 255)
    private String tags;

    @Column(name = "upload_date", nullable = false)
    @ToString.Include
    private LocalDateTime uploadDate = LocalDateTime.now();

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    // Colecciones de interacción y moderación
    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "note")
    private List<Download> downloads = new ArrayList<>();

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();

    public Note(String title, String description, String originalFileName, String storagePath,
                Long fileSizeBytes, String mimeType, FileFormat format, int pageCount,
                User uploader, Subject subject, Topic topic) {
        this.title = title;
        this.description = description;
        this.originalFileName = originalFileName;
        this.storagePath = storagePath;
        this.fileSizeBytes = fileSizeBytes;
        this.mimeType = mimeType;
        this.format = format;
        this.pageCount = pageCount;
        this.uploader = uploader;
        this.subject = subject;
        this.topic = topic;
        this.status = NoteStatus.PUBLISHED;
        this.uploadDate = LocalDateTime.now();
    }

    // Métodos de dominio
    public void publish() {
        this.status = NoteStatus.PUBLISHED;
        this.lastModifiedDate = LocalDateTime.now();
    }

    public void approve() {
        this.status = NoteStatus.PUBLISHED;
        this.lastModifiedDate = LocalDateTime.now();
    }

    public void reject() {
        this.status = NoteStatus.REJECTED;
        this.lastModifiedDate = LocalDateTime.now();
    }

    public void incrementViews() {
        this.viewsCount++;
    }

    public void hide() {
        this.status = NoteStatus.HIDDEN;
        this.lastModifiedDate = LocalDateTime.now();
    }

    public void archive() {
        this.status = NoteStatus.ARCHIVED;
        this.lastModifiedDate = LocalDateTime.now();
    }

    public void restore() {
        this.status = NoteStatus.PUBLISHED;
        this.lastModifiedDate = LocalDateTime.now();
    }

    public void delete() {
        this.status = NoteStatus.HIDDEN;
        this.lastModifiedDate = LocalDateTime.now();
    }

    public void updateMetadata(String title, String description, Topic topic) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        this.description = description;
        this.topic = topic;
        this.lastModifiedDate = LocalDateTime.now();
    }

    public boolean isPublished() {
        return NoteStatus.PUBLISHED.equals(this.status) || NoteStatus.VISIBLE.equals(this.status);
    }

    public float getSizeMB() {
        return fileSizeBytes != null ? (float) fileSizeBytes / (1024 * 1024) : 0f;
    }

    public int getDownloadsCount() {
        return downloads != null ? downloads.size() : 0;
    }

    public double getAverageRating() {
        if (ratings == null || ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream()
                .mapToInt(Rating::getValue)
                .average()
                .orElse(0.0);
    }

    public int getRatingsCount() {
        return ratings != null ? ratings.size() : 0;
    }

    public String getStorageKey() {
        return this.storagePath;
    }

    public void setStorageKey(String storageKey) {
        this.storagePath = storageKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note note)) return false;
        return id != null && id.equals(note.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
