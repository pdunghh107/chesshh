package com.pdunghh.chess.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.pdunghh.chess.dto.response.MatchmakingStatusResponse;
import com.pdunghh.chess.service.MatchmakingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MatchmakingWsController {

    private final MatchmakingService matchmakingService;

    @MessageMapping("/matchmaking/join")
    @SendToUser("/queue/matchmaking/status")
    public MatchmakingStatusResponse handleWsJoin(Principal principal) {
        if (principal == null) {
            log.warn("WebSocket join attempt without authenticated principal");
            return MatchmakingStatusResponse.queued("Chưa xác thực người dùng");
        }

        UUID userId = UUID.fromString(principal.getName());
        log.info("WebSocket /app/matchmaking/join received from userId: {}", userId);
        return matchmakingService.joinQueue(userId);
    }

    @MessageMapping("/matchmaking/cancel")
    @SendToUser("/queue/matchmaking/status")
    public MatchmakingStatusResponse handleWsCancel(Principal principal) {
        if (principal == null) {
            return MatchmakingStatusResponse.cancelled();
        }

        UUID userId = UUID.fromString(principal.getName());
        log.info("WebSocket /app/matchmaking/cancel received from userId: {}", userId);
        return matchmakingService.cancelQueue(userId);
    }
}
