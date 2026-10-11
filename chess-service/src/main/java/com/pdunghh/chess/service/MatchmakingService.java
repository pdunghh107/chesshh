package com.pdunghh.chess.service;

import java.util.UUID;

import com.pdunghh.chess.dto.response.MatchmakingStatusResponse;

public interface MatchmakingService {

    MatchmakingStatusResponse joinQueue(UUID userId);

    MatchmakingStatusResponse cancelQueue(UUID userId);
}
