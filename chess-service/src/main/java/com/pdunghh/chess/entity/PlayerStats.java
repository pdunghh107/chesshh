package com.pdunghh.chess.entity;

import java.util.UUID;

import com.pdunghh.shared.persistent.DateAuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "player_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStats extends DateAuditableEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Builder.Default
    @Column(nullable = false)
    private int elo = 1200;

    @Builder.Default
    @Column(name = "peak_elo", nullable = false)
    private int peakElo = 1200;

    @Builder.Default
    @Column(name = "games_played", nullable = false)
    private int gamesPlayed = 0;

    @Builder.Default
    @Column(nullable = false)
    private int wins = 0;

    @Builder.Default
    @Column(nullable = false)
    private int losses = 0;

    @Builder.Default
    @Column(nullable = false)
    private int draws = 0;
}
