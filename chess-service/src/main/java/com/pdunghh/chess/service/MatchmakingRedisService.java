package com.pdunghh.chess.service;

import java.util.UUID;

public interface MatchmakingRedisService {

    String tryMatchOrQueue(UUID userId, int elo, int minElo, int maxElo);

    void removeFromQueue(UUID userId);
}
