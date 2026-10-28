package com.university.website.model;

import com.university.website.model.enums.ProfileVisibility;
import com.university.website.model.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @NotBlank
    @Column(nullable = false)
    @ToString.Include
    private String fullName;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    @ToString.Include
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String passwordHash;//contraseña cifrada

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    @ToString.Include
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_visibility", nullable = false)
    private ProfileVisibility profileVisibility = ProfileVisibility.PUBLIC;

    @Column(nullable = false)
    @ToString.Include
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime joinDate = LocalDateTime.now();

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(length = 1000)
    private String bio;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private NotificationSettings notificationSettings;

    public User(String fullName, String email, String passwordHash, UserRole userRole) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.userRole = userRole;
        this.active = true;
        this.joinDate = LocalDateTime.now();
        this.profileVisibility = ProfileVisibility.PUBLIC;
    }

    // Métodos de dominio
    public void changePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("La contraseña cifrada no puede estar vacía.");
        }
        this.passwordHash = newPasswordHash;
    }

    public void updateProfile(String fullName, String bio, ProfileVisibility visibility) {
        if (fullName != null && !fullName.isBlank()) {
            this.fullName = fullName;
        }
        this.bio = bio;
        if (visibility != null) {
            this.profileVisibility = visibility;
        }
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public boolean isAdmin() {
        return UserRole.ADMINISTRATOR.equals(this.userRole);
    }

    public boolean isStudent() {
        return UserRole.STUDENT.equals(this.userRole);
    }

    public boolean isModerator() {
        return UserRole.MODERATOR.equals(this.userRole);
    }

    public boolean isProfessor() {
        return UserRole.PROFESSOR.equals(this.userRole);
    }

    // Equals y HashCode basados en la clave natural inmutable email
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return email != null && email.equalsIgnoreCase(user.getEmail());
    }

    @Override
    public int hashCode() {
        return email != null ? email.toLowerCase().hashCode() : getClass().hashCode();
    }
}
