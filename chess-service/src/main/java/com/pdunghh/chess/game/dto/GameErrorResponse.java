package com.pdunghh.chess.game.dto;

public record GameErrorResponse(
                String roomId,
                String error,
                String currentFen,
                int currentPly) {
}
