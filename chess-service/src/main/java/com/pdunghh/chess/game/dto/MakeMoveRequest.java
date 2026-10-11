package com.pdunghh.chess.game.dto;

public record MakeMoveRequest(
        String from,        // "e2"
        String to,          // "e4"
        String promotion,   // "q", "r", "b", "n" (neu co)
        int expectedPly     // Nuoc di nay phai dung la plyCount + 1
) {}
