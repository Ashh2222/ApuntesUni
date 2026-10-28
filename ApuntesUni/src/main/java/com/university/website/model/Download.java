package com.university.website.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "downloads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Download {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "download_date", nullable = false)
    @ToString.Include
    private LocalDateTime downloadDate = LocalDateTime.now();

    @Column(name = "ip_address", length = 50)
    @ToString.Include
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    public Download(User user, Note note, String ipAddress, String userAgent) {
        this.user = user;
        this.note = note;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.downloadDate = LocalDateTime.now();
    }

    public LocalDateTime getDate() {
        return this.downloadDate;
    }

    public void setDate(LocalDateTime date) {
        this.downloadDate = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Download download)) return false;
        return id != null && id.equals(download.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
