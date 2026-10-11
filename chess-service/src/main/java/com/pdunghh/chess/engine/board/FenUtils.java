package com.pdunghh.chess.engine.board;

import com.pdunghh.chess.engine.model.Piece;
import com.pdunghh.chess.engine.model.PieceColor;
import com.pdunghh.chess.engine.model.Square;

public final class FenUtils {

    public static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private FenUtils() {
    }

    public static ChessBoard parse(String fen) {
        if (fen == null || fen.isBlank()) {
            fen = STARTING_FEN;
        }

        String[] parts = fen.trim().split("\\s+");
        ChessBoard board = new ChessBoard();

        // 1. Piece placement (từ hàng 8 xuống hàng 1)
        String[] ranks = parts[0].split("/");
        if (ranks.length != 8) {
            throw new IllegalArgumentException("Invalid FEN ranks: " + parts[0]);
        }

        for (int r = 0; r < 8; r++) {
            int rankIdx = 7 - r; // r=0 tương ứng rank 7 (hàng 8)
            String rankStr = ranks[r];
            int fileIdx = 0;

            for (char c : rankStr.toCharArray()) {
                if (Character.isDigit(c)) {
                    fileIdx += Character.getNumericValue(c);
                } else {
                    board.setPiece(fileIdx, rankIdx, Piece.fromFenChar(c));
                    fileIdx++;
                }
            }
        }

        // 2. Turn
        if (parts.length > 1) {
            board.setTurn("w".equalsIgnoreCase(parts[1]) ? PieceColor.WHITE : PieceColor.BLACK);
        }

        // 3. Castling rights
        if (parts.length > 2) {
            String castling = parts[2];
            board.setWhiteCanCastleKingside(castling.contains("K"));
            board.setWhiteCanCastleQueenside(castling.contains("Q"));
            board.setBlackCanCastleKingside(castling.contains("k"));
            board.setBlackCanCastleQueenside(castling.contains("q"));
        }

        // 4. En Passant target
        if (parts.length > 3 && !"-".equals(parts[3])) {
            board.setEnPassantTarget(Square.fromAlgebraic(parts[3]));
        }

        // 5. Halfmove clock
        if (parts.length > 4) {
            board.setHalfmoveClock(Integer.parseInt(parts[4]));
        }

        // 6. Fullmove number
        if (parts.length > 5) {
            board.setFullmoveNumber(Integer.parseInt(parts[5]));
        }

        return board;
    }

    public static String export(ChessBoard board) {
        StringBuilder sb = new StringBuilder();

        // 1. Piece placement
        for (int r = 7; r >= 0; r--) {
            int emptyCount = 0;
            for (int f = 0; f < 8; f++) {
                Piece p = board.getPiece(f, r);
                if (p == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount);
                        emptyCount = 0;
                    }
                    sb.append(p.toFenChar());
                }
            }
            if (emptyCount > 0) {
                sb.append(emptyCount);
            }
            if (r > 0) {
                sb.append("/");
            }
        }

        // 2. Turn
        sb.append(" ").append(board.getTurn() == PieceColor.WHITE ? "w" : "b");

        // 3. Castling rights
        sb.append(" ");
        StringBuilder castling = new StringBuilder();
        if (board.isWhiteCanCastleKingside()) castling.append("K");
        if (board.isWhiteCanCastleQueenside()) castling.append("Q");
        if (board.isBlackCanCastleKingside()) castling.append("k");
        if (board.isBlackCanCastleQueenside()) castling.append("q");
        sb.append(castling.isEmpty() ? "-" : castling.toString());

        // 4. En Passant target
        sb.append(" ");
        if (board.getEnPassantTarget() != null) {
            sb.append(board.getEnPassantTarget().toAlgebraic());
        } else {
            sb.append("-");
        }

        // 5. Halfmove & Fullmove
        sb.append(" ").append(board.getHalfmoveClock());
        sb.append(" ").append(board.getFullmoveNumber());

        return sb.toString();
    }
}
