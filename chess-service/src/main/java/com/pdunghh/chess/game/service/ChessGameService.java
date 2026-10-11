package com.pdunghh.chess.game.service;

import java.util.UUID;

import com.pdunghh.chess.game.dto.MakeMoveRequest;
import com.pdunghh.chess.game.model.GameRoomState;

public interface ChessGameService {

    GameRoomState createInitialGame(String roomId, UUID whiteUserId, UUID blackUserId, long initialTimeMs);

    GameRoomState getGameForReconnect(String roomId);

    void processMove(String roomId, UUID userId, MakeMoveRequest request);
}
