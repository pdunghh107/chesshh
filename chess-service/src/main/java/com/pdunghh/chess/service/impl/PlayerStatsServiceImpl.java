package com.pdunghh.chess.service.impl;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pdunghh.chess.entity.PlayerStats;
import com.pdunghh.chess.repository.PlayerStatsRepository;
import com.pdunghh.chess.service.PlayerStatsService;
import com.pdunghh.shared.event.UserRegisteredEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerStatsServiceImpl implements PlayerStatsService {

    private final PlayerStatsRepository playerStatsRepository;

    @Override
    @Transactional
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Processing UserRegisteredEvent for userId: {}", event.userId());

        if (playerStatsRepository.existsByUserId(event.userId())) {
            log.warn("PlayerStats already exists for userId: {}. Skipping record creation.", event.userId());
            return;
        }

        PlayerStats playerStats = PlayerStats.builder()
                .userId(event.userId())
                .elo(1200)
                .peakElo(1200)
                .gamesPlayed(0)
                .wins(0)
                .losses(0)
                .draws(0)
                .build();

        playerStatsRepository.save(Objects.requireNonNull(playerStats));
        log.info("Successfully created default PlayerStats for userId: {}", event.userId());
    }
}
