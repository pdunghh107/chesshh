package com.pdunghh.chess.entity;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRoomState implements Serializable {

    private String roomId;
    private UUID whiteUserId;
    private UUID blackUserId;

    private String fen;
    private String turn; // "WHITE" hoac "BLACK"

    private long whiteTimeRemainingMs;
    private long blackTimeRemainingMs;
    private long lastMoveTimestamp;

    private int plyCount; // Sequence counter chống out-of-order

    private String status; // "ONGOING", "WHITE_WON", "BLACK_WON", "DRAW", "WHITE_TIMEOUT", "BLACK_TIMEOUT"
}
