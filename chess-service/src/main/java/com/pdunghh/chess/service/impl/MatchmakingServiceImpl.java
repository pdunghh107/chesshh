package com.pdunghh.chess.service.impl;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.pdunghh.chess.dto.response.MatchFoundResponse;
import com.pdunghh.chess.dto.response.MatchmakingStatusResponse;
import com.pdunghh.chess.entity.PlayerStats;
import com.pdunghh.chess.repository.PlayerStatsRepository;
import com.pdunghh.chess.service.MatchmakingRedisService;
import com.pdunghh.chess.service.MatchmakingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchmakingServiceImpl implements MatchmakingService {

    private final PlayerStatsRepository playerStatsRepository;
    private final MatchmakingRedisService matchmakingRedisService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final int DEFAULT_ELO = 1200;
    private static final int INITIAL_ELO_WINDOW = 100; // Tìm trong khoảng ±100 Elo

    private final Random random = new Random();

    @Override
    public MatchmakingStatusResponse joinQueue(UUID userId) {
        log.info("Player userId: {} requested to join matchmaking queue", userId);

        // 1. Lấy Elo của player, nếu chưa có tạo tạm 1200
        int playerElo = playerStatsRepository.findByUserId(userId)
                .map(PlayerStats::getElo)
                .orElse(DEFAULT_ELO);

        int minElo = Math.max(0, playerElo - INITIAL_ELO_WINDOW);
        int maxElo = playerElo + INITIAL_ELO_WINDOW;

        // 2. Chạy Lua script trên Redis
        String result = matchmakingRedisService.tryMatchOrQueue(userId, playerElo, minElo, maxElo);

        if (result != null && result.startsWith("MATCHED:")) {
            String opponentIdStr = result.substring("MATCHED:".length());
            UUID opponentId = UUID.fromString(opponentIdStr);

            int opponentElo = playerStatsRepository.findByUserId(opponentId)
                    .map(PlayerStats::getElo)
                    .orElse(DEFAULT_ELO);

            // 3. Tạo phòng đấu ngẫu nhiên và phân định Trắng/Đen
            MatchFoundResponse matchResponse = buildMatchResponse(userId, playerElo, opponentId, opponentElo);

            // 4. Bắn WebSocket thông báo tới cả 2 kỳ thủ
            notifyMatchFound(userId, opponentId, matchResponse);

            return MatchmakingStatusResponse.matched(matchResponse);
        }

        return MatchmakingStatusResponse.queued("Đang tìm đối thủ phù hợp trong hàng đợi...");
    }

    @Override
    public MatchmakingStatusResponse cancelQueue(UUID userId) {
        log.info("Player userId: {} cancelling matchmaking queue", userId);
        matchmakingRedisService.removeFromQueue(userId);
        return MatchmakingStatusResponse.cancelled();
    }

    private MatchFoundResponse buildMatchResponse(UUID p1, int elo1, UUID p2, int elo2) {
        String roomId = "room_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        // Random trắng/đen
        boolean p1IsWhite = random.nextBoolean();

        MatchFoundResponse.PlayerInfo whitePlayer = p1IsWhite
                ? new MatchFoundResponse.PlayerInfo(p1, elo1)
                : new MatchFoundResponse.PlayerInfo(p2, elo2);

        MatchFoundResponse.PlayerInfo blackPlayer = p1IsWhite
                ? new MatchFoundResponse.PlayerInfo(p2, elo2)
                : new MatchFoundResponse.PlayerInfo(p1, elo1);

        return new MatchFoundResponse(roomId, whitePlayer, blackPlayer, Instant.now());
    }

    private void notifyMatchFound(UUID p1, UUID p2, MatchFoundResponse matchResponse) {
        log.info("Notifying match found via WebSocket for roomId: {} between p1: {} and p2: {}",
                matchResponse.roomId(), p1, p2);

        // Gửi tới kênh cá nhân của Player 1
        messagingTemplate.convertAndSendToUser(
                p1.toString(),
                "/queue/matchmaking",
                matchResponse
        );

        // Gửi tới kênh cá nhân của Player 2
        messagingTemplate.convertAndSendToUser(
                p2.toString(),
                "/queue/matchmaking",
                matchResponse
        );
    }
}
