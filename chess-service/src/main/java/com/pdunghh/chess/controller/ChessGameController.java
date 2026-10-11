package com.pdunghh.chess.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pdunghh.chess.entity.GameRoomState;
import com.pdunghh.chess.service.ChessGameService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/chess/game")
@RequiredArgsConstructor
public class ChessGameController {

    private final ChessGameService chessGameService;

    /**
     * Edge Case 1: Reconnection & F5 state recovery endpoint
     */
    @GetMapping("/{roomId}")
    public GameRoomState getGameForReconnect(@PathVariable String roomId) {
        log.info("API Reconnect requested for roomId: {}", roomId);
        return chessGameService.getGameForReconnect(roomId);
    }
}
