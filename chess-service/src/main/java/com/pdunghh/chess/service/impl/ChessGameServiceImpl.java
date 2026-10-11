package com.pdunghh.chess.service.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.pdunghh.chess.dto.request.MakeMoveRequest;
import com.pdunghh.chess.dto.response.GameErrorResponse;
import com.pdunghh.chess.dto.response.MoveSuccessResponse;
import com.pdunghh.chess.engine.board.ChessBoard;
import com.pdunghh.chess.engine.board.FenUtils;
import com.pdunghh.chess.engine.model.Move;
import com.pdunghh.chess.engine.model.PieceType;
import com.pdunghh.chess.engine.model.Square;
import com.pdunghh.chess.engine.rule.ChessRuleEngine;
import com.pdunghh.chess.entity.GameRoomState;
import com.pdunghh.chess.service.ChessGameService;
import com.pdunghh.chess.service.GameRoomRedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChessGameServiceImpl implements ChessGameService {

    private final GameRoomRedisService gameRoomRedisService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public GameRoomState createInitialGame(String roomId, UUID whiteUserId, UUID blackUserId, long initialTimeMs) {
        long now = System.currentTimeMillis();
        GameRoomState state = GameRoomState.builder()
                .roomId(roomId)
                .whiteUserId(whiteUserId)
                .blackUserId(blackUserId)
                .fen(FenUtils.STARTING_FEN)
                .turn("WHITE")
                .whiteTimeRemainingMs(initialTimeMs)
                .blackTimeRemainingMs(initialTimeMs)
                .lastMoveTimestamp(now)
                .plyCount(0)
                .status("ONGOING")
                .build();

        gameRoomRedisService.saveGame(state);
        log.info("Initialized GameRoomState on Redis for roomId: {}", roomId);
        return state;
    }

    @Override
    public GameRoomState getGameForReconnect(String roomId) {
        GameRoomState state = gameRoomRedisService.getGame(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng đấu: " + roomId));

        // Edge Case 1: Tính toán thời gian thực tế đã trừ lag/thời gian suy nghĩ
        if ("ONGOING".equals(state.getStatus())) {
            long now = System.currentTimeMillis();
            long elapsed = now - state.getLastMoveTimestamp();

            if ("WHITE".equals(state.getTurn())) {
                state.setWhiteTimeRemainingMs(Math.max(0, state.getWhiteTimeRemainingMs() - elapsed));
            } else {
                state.setBlackTimeRemainingMs(Math.max(0, state.getBlackTimeRemainingMs() - elapsed));
            }
        }

        return state;
    }

    @Override
    public void processMove(String roomId, UUID userId, MakeMoveRequest request) {
        GameRoomState state = gameRoomRedisService.getGame(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng đấu: " + roomId));

        if (!"ONGOING".equals(state.getStatus())) {
            sendError(roomId, userId, "Trận đấu đã kết thúc", state.getFen(), state.getPlyCount());
            return;
        }

        // 1. Kiểm tra quyền đi của Player
        boolean isWhite = userId.equals(state.getWhiteUserId());
        boolean isBlack = userId.equals(state.getBlackUserId());

        if (!isWhite && !isBlack) {
            sendError(roomId, userId, "Bạn không phải là kỳ thủ trong phòng này", state.getFen(), state.getPlyCount());
            return;
        }

        if (("WHITE".equals(state.getTurn()) && !isWhite) || ("BLACK".equals(state.getTurn()) && !isBlack)) {
            sendError(roomId, userId, "Chưa tới lượt đi của bạn", state.getFen(), state.getPlyCount());
            return;
        }

        // 2. Edge Case 3: Out-of-order Moves (Ply sequence check)
        if (request.expectedPly() != state.getPlyCount() + 1) {
            log.warn("Out-of-order move rejected! expectedPly: {}, serverPly: {}", request.expectedPly(), state.getPlyCount());
            sendError(roomId, userId, "Nước đi không đồng bộ với thế cờ hiện tại. Đang khôi phục lại bàn cờ...", state.getFen(), state.getPlyCount());
            return;
        }

        // 3. Edge Case 2: Timeout Check (Passive)
        long now = System.currentTimeMillis();
        long elapsed = now - state.getLastMoveTimestamp();

        if (isWhite) {
            long remaining = state.getWhiteTimeRemainingMs() - elapsed;
            if (remaining <= 0) {
                handleTimeoutWin(state, "BLACK_WON", "Trắng hết giờ thi đấu");
                return;
            }
            state.setWhiteTimeRemainingMs(remaining);
        } else {
            long remaining = state.getBlackTimeRemainingMs() - elapsed;
            if (remaining <= 0) {
                handleTimeoutWin(state, "WHITE_WON", "Đen hết giờ thi đấu");
                return;
            }
            state.setBlackTimeRemainingMs(remaining);
        }

        // 4. Validate nước đi bằng In-House ChessRuleEngine thuần Java
        ChessBoard board = FenUtils.parse(state.getFen());
        Square from = Square.fromAlgebraic(request.from());
        Square to = Square.fromAlgebraic(request.to());
        PieceType promo = (request.promotion() != null && !request.promotion().isBlank())
                ? PieceType.fromChar(request.promotion().charAt(0))
                : null;

        Move move = new Move(from, to, promo);

        if (!ChessRuleEngine.isLegalMove(board, move)) {
            log.warn("Illegal move attempted in room {}: from {} to {}", roomId, request.from(), request.to());
            sendError(roomId, userId, "Nước đi sai luật cờ vua!", state.getFen(), state.getPlyCount());
            return;
        }

        // 5. Nước đi hợp lệ -> Apply nước đi và sinh chuỗi FEN mới
        ChessRuleEngine.applyMove(board, move);
        String newFen = FenUtils.export(board);

        // Cập nhật State
        state.setFen(newFen);
        state.setTurn(board.getTurn().name());
        state.setPlyCount(state.getPlyCount() + 1);
        state.setLastMoveTimestamp(now);

        gameRoomRedisService.saveGame(state);

        // 6. Broadcast nước đi hợp lệ cho cả 2 bên
        MoveSuccessResponse response = new MoveSuccessResponse(
                roomId,
                request.from(),
                request.to(),
                request.promotion(),
                newFen,
                state.getTurn(),
                state.getPlyCount(),
                state.getWhiteTimeRemainingMs(),
                state.getBlackTimeRemainingMs(),
                state.getStatus(),
                Instant.now()
        );

        messagingTemplate.convertAndSend("/topic/game/" + roomId + "/move", response);
        log.info("Successfully processed move for roomId: {}, from: {} to: {}, new turn: {}", roomId, request.from(), request.to(), state.getTurn());
    }

    private void handleTimeoutWin(GameRoomState state, String status, String message) {
        state.setStatus(status);
        gameRoomRedisService.saveGame(state);

        log.info("Game timeout in roomId: {} -> {}", state.getRoomId(), message);
        messagingTemplate.convertAndSend("/topic/game/" + state.getRoomId() + "/game-over", message);
    }

    private void sendError(String roomId, UUID userId, String error, String currentFen, int currentPly) {
        GameErrorResponse errorResponse = new GameErrorResponse(roomId, error, currentFen, currentPly);
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/game/errors", errorResponse);
    }
}
