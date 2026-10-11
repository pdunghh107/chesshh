package com.pdunghh.chess.service;

import java.util.UUID;

import com.pdunghh.chess.dto.request.MakeMoveRequest;
import com.pdunghh.chess.entity.GameRoomState;

public interface ChessGameService {

    GameRoomState createInitialGame(String roomId, UUID whiteUserId, UUID blackUserId, long initialTimeMs);

    GameRoomState getGameForReconnect(String roomId);

    void processMove(String roomId, UUID userId, MakeMoveRequest request);
}
