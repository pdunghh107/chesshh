package com.pdunghh.chess.engine.rule;

import com.pdunghh.chess.engine.board.ChessBoard;
import com.pdunghh.chess.engine.model.Move;
import com.pdunghh.chess.engine.model.Piece;
import com.pdunghh.chess.engine.model.PieceColor;
import com.pdunghh.chess.engine.model.PieceType;
import com.pdunghh.chess.engine.model.Square;

public final class ChessRuleEngine {

    private ChessRuleEngine() {
    }

    /**
     * Kiểm tra toàn diện xem một nước đi có hợp lệ 100% theo luật cờ vua quốc tế hay không.
     */
    public static boolean isLegalMove(ChessBoard board, Move move) {
        Square from = move.from();
        Square to = move.to();

        Piece piece = board.getPiece(from);
        if (piece == null || piece.color() != board.getTurn()) {
            return false; // Không có quân hoặc không đúng lượt đi
        }

        Piece destPiece = board.getPiece(to);
        if (destPiece != null && destPiece.color() == piece.color()) {
            return false; // Không được ăn quân cùng màu của mình
        }

        // 1. Kiểm tra hình học di chuyển và vật cản
        if (!isValidGeometryAndPath(board, piece, from, to, move.promotion())) {
            return false;
        }

        // 2. King Safety: Mô phỏng nước đi và kiểm tra Vua của mình có bị chiếu hay không
        ChessBoard simulated = board.copy();
        applyMove(simulated, move);

        return !isKingInCheck(simulated, piece.color());
    }

    public static boolean isKingInCheck(ChessBoard board, PieceColor color) {
        Square kingSquare = board.findKing(color);
        if (kingSquare == null) {
            return false;
        }
        return isSquareAttacked(board, kingSquare, color.opposite());
    }

    public static boolean isSquareAttacked(ChessBoard board, Square target, PieceColor attackerColor) {
        // Kiểm tra xem có quân nào của attackerColor có thể tấn công vào ô target không
        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                Piece attacker = board.getPiece(f, r);
                if (attacker != null && attacker.color() == attackerColor) {
                    Square from = new Square(f, r);
                    if (canPieceAttackSquare(board, attacker, from, target)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean canPieceAttackSquare(ChessBoard board, Piece piece, Square from, Square to) {
        int deltaFile = to.file() - from.file();
        int deltaRank = to.rank() - from.rank();

        return switch (piece.type()) {
            case PAWN -> {
                int forward = piece.color() == PieceColor.WHITE ? 1 : -1;
                yield deltaRank == forward && Math.abs(deltaFile) == 1;
            }
            case KNIGHT -> (Math.abs(deltaFile) == 1 && Math.abs(deltaRank) == 2)
                    || (Math.abs(deltaFile) == 2 && Math.abs(deltaRank) == 1);
            case BISHOP -> Math.abs(deltaFile) == Math.abs(deltaRank) && isPathClear(board, from, to);
            case ROOK -> (deltaFile == 0 || deltaRank == 0) && isPathClear(board, from, to);
            case QUEEN -> (Math.abs(deltaFile) == Math.abs(deltaRank) || deltaFile == 0 || deltaRank == 0)
                    && isPathClear(board, from, to);
            case KING -> Math.max(Math.abs(deltaFile), Math.abs(deltaRank)) == 1;
        };
    }

    private static boolean isValidGeometryAndPath(ChessBoard board, Piece piece, Square from, Square to, PieceType promotion) {
        int deltaFile = to.file() - from.file();
        int deltaRank = to.rank() - from.rank();

        return switch (piece.type()) {
            case PAWN -> isValidPawnMove(board, piece, from, to, deltaFile, deltaRank, promotion);
            case KNIGHT -> (Math.abs(deltaFile) == 1 && Math.abs(deltaRank) == 2)
                    || (Math.abs(deltaFile) == 2 && Math.abs(deltaRank) == 1);
            case BISHOP -> Math.abs(deltaFile) == Math.abs(deltaRank) && isPathClear(board, from, to);
            case ROOK -> (deltaFile == 0 || deltaRank == 0) && isPathClear(board, from, to);
            case QUEEN -> (Math.abs(deltaFile) == Math.abs(deltaRank) || deltaFile == 0 || deltaRank == 0)
                    && isPathClear(board, from, to);
            case KING -> isValidKingMove(board, piece, from, to, deltaFile, deltaRank);
        };
    }

    private static boolean isValidPawnMove(ChessBoard board, Piece piece, Square from, Square to,
                                           int deltaFile, int deltaRank, PieceType promotion) {
        int forward = piece.color() == PieceColor.WHITE ? 1 : -1;
        int startRank = piece.color() == PieceColor.WHITE ? 1 : 6;
        int promoRank = piece.color() == PieceColor.WHITE ? 7 : 0;

        // Đi thẳng 1 ô
        if (deltaFile == 0 && deltaRank == forward) {
            if (board.getPiece(to) != null) return false;
            return to.rank() != promoRank || promotion != null;
        }

        // Đi thẳng 2 ô ban đầu
        if (deltaFile == 0 && deltaRank == forward * 2 && from.rank() == startRank) {
            Square midSquare = new Square(from.file(), from.rank() + forward);
            return board.getPiece(midSquare) == null && board.getPiece(to) == null;
        }

        // Ăn chéo thông thường
        if (Math.abs(deltaFile) == 1 && deltaRank == forward) {
            Piece targetPiece = board.getPiece(to);
            if (targetPiece != null && targetPiece.color() != piece.color()) {
                return to.rank() != promoRank || promotion != null;
            }

            // En Passant
            if (board.getEnPassantTarget() != null && board.getEnPassantTarget().equals(to)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isValidKingMove(ChessBoard board, Piece piece, Square from, Square to,
                                           int deltaFile, int deltaRank) {
        // Đi thường 1 ô
        if (Math.max(Math.abs(deltaFile), Math.abs(deltaRank)) == 1) {
            return true;
        }

        // Nhập thành: deltaRank == 0 && |deltaFile| == 2
        if (deltaRank == 0 && Math.abs(deltaFile) == 2) {
            return isValidCastling(board, piece.color(), from, to);
        }

        return false;
    }

    private static boolean isValidCastling(ChessBoard board, PieceColor color, Square from, Square to) {
        // Vua không được đang bị chiếu
        if (isSquareAttacked(board, from, color.opposite())) {
            return false;
        }

        int expectedRank = color == PieceColor.WHITE ? 0 : 7;
        if (from.rank() != expectedRank || from.file() != 4) {
            return false;
        }

        boolean kingside = to.file() == 6; // O-O
        boolean queenside = to.file() == 2; // O-O-O

        if (kingside) {
            boolean hasRight = color == PieceColor.WHITE ? board.isWhiteCanCastleKingside() : board.isBlackCanCastleKingside();
            if (!hasRight) return false;

            // Kiểm tra các ô giữa f và g phải trống
            if (board.getPiece(5, expectedRank) != null || board.getPiece(6, expectedRank) != null) return false;

            // Các ô Vua đi qua không được bị chiếu
            if (isSquareAttacked(board, new Square(5, expectedRank), color.opposite()) ||
                isSquareAttacked(board, new Square(6, expectedRank), color.opposite())) {
                return false;
            }

            Piece rook = board.getPiece(7, expectedRank);
            return rook != null && rook.type() == PieceType.ROOK && rook.color() == color;
        }

        if (queenside) {
            boolean hasRight = color == PieceColor.WHITE ? board.isWhiteCanCastleQueenside() : board.isBlackCanCastleQueenside();
            if (!hasRight) return false;

            // Kiểm tra các ô b, c, d phải trống
            if (board.getPiece(1, expectedRank) != null || board.getPiece(2, expectedRank) != null || board.getPiece(3, expectedRank) != null) return false;

            // Ô c và d Vua đi qua không được bị chiếu
            if (isSquareAttacked(board, new Square(2, expectedRank), color.opposite()) ||
                isSquareAttacked(board, new Square(3, expectedRank), color.opposite())) {
                return false;
            }

            Piece rook = board.getPiece(0, expectedRank);
            return rook != null && rook.type() == PieceType.ROOK && rook.color() == color;
        }

        return false;
    }

    private static boolean isPathClear(ChessBoard board, Square from, Square to) {
        int stepFile = Integer.compare(to.file(), from.file());
        int stepRank = Integer.compare(to.rank(), from.rank());

        int curFile = from.file() + stepFile;
        int curRank = from.rank() + stepRank;

        while (curFile != to.file() || curRank != to.rank()) {
            if (board.getPiece(curFile, curRank) != null) {
                return false;
            }
            curFile += stepFile;
            curRank += stepRank;
        }
        return true;
    }

    public static void applyMove(ChessBoard board, Move move) {
        Square from = move.from();
        Square to = move.to();
        Piece movingPiece = board.getPiece(from);

        // Xử lý En Passant capture
        if (movingPiece.type() == PieceType.PAWN && board.getEnPassantTarget() != null && board.getEnPassantTarget().equals(to)) {
            int capturedPawnRank = movingPiece.color() == PieceColor.WHITE ? to.rank() - 1 : to.rank() + 1;
            board.setPiece(to.file(), capturedPawnRank, null);
        }

        // Xử lý Castling di chuyển Xe kèm theo
        if (movingPiece.type() == PieceType.KING && Math.abs(to.file() - from.file()) == 2) {
            int rank = from.rank();
            if (to.file() == 6) { // Kingside
                Piece rook = board.getPiece(7, rank);
                board.setPiece(7, rank, null);
                board.setPiece(5, rank, rook);
            } else if (to.file() == 2) { // Queenside
                Piece rook = board.getPiece(0, rank);
                board.setPiece(0, rank, null);
                board.setPiece(3, rank, rook);
            }
        }

        // Cập nhật En Passant target mới
        if (movingPiece.type() == PieceType.PAWN && Math.abs(to.rank() - from.rank()) == 2) {
            int midRank = (from.rank() + to.rank()) / 2;
            board.setEnPassantTarget(new Square(from.file(), midRank));
        } else {
            board.setEnPassantTarget(null);
        }

        // Hủy quyền nhập thành nếu Vua hoặc Xe di chuyển
        if (movingPiece.type() == PieceType.KING) {
            if (movingPiece.color() == PieceColor.WHITE) {
                board.setWhiteCanCastleKingside(false);
                board.setWhiteCanCastleQueenside(false);
            } else {
                board.setBlackCanCastleKingside(false);
                board.setBlackCanCastleQueenside(false);
            }
        } else if (movingPiece.type() == PieceType.ROOK) {
            if (movingPiece.color() == PieceColor.WHITE) {
                if (from.equals(new Square(0, 0))) board.setWhiteCanCastleQueenside(false);
                if (from.equals(new Square(7, 0))) board.setWhiteCanCastleKingside(false);
            } else {
                if (from.equals(new Square(0, 7))) board.setBlackCanCastleQueenside(false);
                if (from.equals(new Square(7, 7))) board.setBlackCanCastleKingside(false);
            }
        }

        // Di chuyển quân
        Piece finalPiece = (move.promotion() != null && movingPiece.type() == PieceType.PAWN)
                ? new Piece(move.promotion(), movingPiece.color())
                : movingPiece;

        board.setPiece(from, null);
        board.setPiece(to, finalPiece);

        // Đổi lượt đi
        board.setTurn(board.getTurn().opposite());
        if (board.getTurn() == PieceColor.WHITE) {
            board.setFullmoveNumber(board.getFullmoveNumber() + 1);
        }
    }
}
