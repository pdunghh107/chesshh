package com.pdunghh.chess.dto.response;

import java.time.Instant;

public record MoveSuccessResponse(
        String roomId,
        String from,
        String to,
        String promotion,
        String fen,
        String turn,
        int plyCount,
        long whiteTimeRemainingMs,
        long blackTimeRemainingMs,
        String status,
        Instant movedAt
) {}
