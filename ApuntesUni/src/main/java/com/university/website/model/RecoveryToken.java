package com.university.website.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "recovery_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class RecoveryToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    @ToString.Include
    private String token;

    @Column(name = "creation_date", nullable = false)
    @ToString.Include
    private LocalDateTime creationDate = LocalDateTime.now();

    @Column(name = "expiration_date", nullable = false)
    @ToString.Include
    private LocalDateTime expirationDate;

    @Column(nullable = false)
    @ToString.Include
    private boolean used = false;

    @Column(name = "ip_address_request", length = 50)
    private String ipAddressRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public RecoveryToken(String token, User user, int expirationMinutes) {
        this.token = token;
        this.user = user;
        this.creationDate = LocalDateTime.now();
        this.expirationDate = this.creationDate.plusMinutes(expirationMinutes);
        this.used = false;
    }

    // Métodos de dominio
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expirationDate);
    }

    public boolean validate() {
        return !this.used && !this.isExpired();
    }

    public void markAsUsed() {
        this.used = true;
    }

    // Equals y HashCode basados en la clave natural inmutable 'token'
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecoveryToken that)) return false;
        return token != null && token.equals(that.getToken());
    }

    @Override
    public int hashCode() {
        return token != null ? token.hashCode() : getClass().hashCode();
    }
}
