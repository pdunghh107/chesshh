package com.pdunghh.chess.game.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.pdunghh.chess.game.dto.MakeMoveRequest;
import com.pdunghh.chess.game.service.ChessGameService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChessGameWsController {

    private final ChessGameService chessGameService;

    /**
     * Nhận nước đi từ Client qua STOMP destination: /app/game/{roomId}/move
     */
    @MessageMapping("/game/{roomId}/move")
    public void handleMove(
            @DestinationVariable String roomId,
            @Payload MakeMoveRequest request,
            Principal principal) {

        if (principal == null) {
            log.warn("Unauthorized move attempt in roomId: {}", roomId);
            return;
        }

        UUID userId = UUID.fromString(principal.getName());
        log.info("WebSocket Move received for roomId: {} from userId: {}, move: {} -> {}, ply: {}",
                roomId, userId, request.from(), request.to(), request.expectedPly());

        chessGameService.processMove(roomId, userId, request);
    }
}
