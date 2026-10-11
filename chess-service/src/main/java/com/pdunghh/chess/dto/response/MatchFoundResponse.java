package com.pdunghh.chess.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MatchFoundResponse(
        String roomId,
        PlayerInfo whitePlayer,
        PlayerInfo blackPlayer,
        Instant matchedAt
) {
    public record PlayerInfo(
            UUID userId,
            int elo
    ) {}
}
