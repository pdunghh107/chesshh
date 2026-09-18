package com.pdunghh.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pdunghh.auth.entity.RefreshTokenEntity;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
    
    List<RefreshTokenEntity> findAllByUserIdAndRevokedAtIsNull(UUID userId);
}
