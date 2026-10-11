package com.pdunghh.chess.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pdunghh.chess.entity.PlayerStats;

@Repository
public interface PlayerStatsRepository extends JpaRepository<PlayerStats, UUID> {

    Optional<PlayerStats> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
