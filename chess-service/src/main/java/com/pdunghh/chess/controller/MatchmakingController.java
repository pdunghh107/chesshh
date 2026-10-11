package com.pdunghh.chess.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pdunghh.chess.dto.response.MatchmakingStatusResponse;
import com.pdunghh.chess.service.MatchmakingService;
import com.pdunghh.shared.security.RequestContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/chess/matchmaking")
@RequiredArgsConstructor
public class MatchmakingController {

    private final MatchmakingService matchmakingService;

    @PostMapping("/join")
    public MatchmakingStatusResponse joinQueue() {
        UUID userId = RequestContext.getUserId();
        log.info("API REST join matchmaking requested by userId: {}", userId);
        return matchmakingService.joinQueue(userId);
    }

    @PostMapping("/cancel")
    public MatchmakingStatusResponse cancelQueue() {
        UUID userId = RequestContext.getUserId();
        log.info("API REST cancel matchmaking requested by userId: {}", userId);
        return matchmakingService.cancelQueue(userId);
    }
}
