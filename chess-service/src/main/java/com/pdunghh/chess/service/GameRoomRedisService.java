package com.pdunghh.chess.service;

import java.util.Optional;

import com.pdunghh.chess.entity.GameRoomState;

public interface GameRoomRedisService {

    void saveGame(GameRoomState state);

    Optional<GameRoomState> getGame(String roomId);

    void removeGame(String roomId);
}
