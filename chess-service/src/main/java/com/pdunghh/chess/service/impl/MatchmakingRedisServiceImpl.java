package com.pdunghh.chess.service.impl;

import java.util.Collections;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.pdunghh.chess.service.MatchmakingRedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchmakingRedisServiceImpl implements MatchmakingRedisService {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<String> matchmakingScript;

    public static final String MATCHMAKING_QUEUE_KEY = "matchmaking:queue";

    @Override
    public String tryMatchOrQueue(UUID userId, int elo, int minElo, int maxElo) {
        String result = redisTemplate.execute(
                matchmakingScript,
                Collections.singletonList(MATCHMAKING_QUEUE_KEY),
                userId.toString(),
                String.valueOf(elo),
                String.valueOf(minElo),
                String.valueOf(maxElo)
        );

        log.info("Lua matchmaking execution for userId: {}, elo: {} -> result: {}", userId, elo, result);
        return result;
    }

    @Override
    public void removeFromQueue(UUID userId) {
        Long removed = redisTemplate.opsForZSet().remove(MATCHMAKING_QUEUE_KEY, userId.toString());
        log.info("Removed userId: {} from matchmaking queue, count: {}", userId, removed);
    }
}
