package com.pdunghh.chess.game.service;

import java.util.Optional;

import com.pdunghh.chess.game.model.GameRoomState;

public interface GameRoomRedisService {

    void saveGame(GameRoomState state);

    Optional<GameRoomState> getGame(String roomId);

    void removeGame(String roomId);
}
