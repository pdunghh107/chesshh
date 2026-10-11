package com.pdunghh.chess.game.service.impl;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdunghh.chess.game.model.GameRoomState;
import com.pdunghh.chess.game.service.GameRoomRedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameRoomRedisServiceImpl implements GameRoomRedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String GAME_STATE_KEY_PREFIX = "game:state:";
    private static final long GAME_TTL_HOURS = 2;

    @Override
    public void saveGame(GameRoomState state) {
        try {
            String key = GAME_STATE_KEY_PREFIX + state.getRoomId();
            String json = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(key, json, GAME_TTL_HOURS, TimeUnit.HOURS);
            log.info("Saved game state for roomId: {}, turn: {}, ply: {}", state.getRoomId(), state.getTurn(), state.getPlyCount());
        } catch (Exception e) {
            log.error("Failed to save GameRoomState to Redis for roomId: {}", state.getRoomId(), e);
            throw new RuntimeException("Lỗi lưu trạng thái bàn cờ vào Redis", e);
        }
    }

    @Override
    public Optional<GameRoomState> getGame(String roomId) {
        try {
            String key = GAME_STATE_KEY_PREFIX + roomId;
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, GameRoomState.class));
        } catch (Exception e) {
            log.error("Failed to read GameRoomState from Redis for roomId: {}", roomId, e);
            return Optional.empty();
        }
    }

    @Override
    public void removeGame(String roomId) {
        String key = GAME_STATE_KEY_PREFIX + roomId;
        redisTemplate.delete(key);
        log.info("Removed GameRoomState from Redis for roomId: {}", roomId);
    }
}
