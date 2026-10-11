package com.pdunghh.chess.dto.response;

public record GameErrorResponse(
        String roomId,
        String error,
        String currentFen,
        int currentPly
) {}
