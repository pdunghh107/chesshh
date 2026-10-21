package com.pdunghh.auth.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.pdunghh.auth.exception.AuthException;
import com.pdunghh.shared.persistent.DateAuditableEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends DateAuditableEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public void checkUserActive() {
        if (!this.isActive) {
            throw AuthException.accountDeactivated();
        }
    }
}
