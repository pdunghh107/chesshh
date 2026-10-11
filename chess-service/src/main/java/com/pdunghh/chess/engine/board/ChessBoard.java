package com.pdunghh.chess.engine.board;

import com.pdunghh.chess.engine.model.Piece;
import com.pdunghh.chess.engine.model.PieceColor;
import com.pdunghh.chess.engine.model.PieceType;
import com.pdunghh.chess.engine.model.Square;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChessBoard {

    // Ma trận [rank][file]: rank 0 = hàng 1 (Trắng), rank 7 = hàng 8 (Đen)
    private final Piece[][] grid = new Piece[8][8];

    private PieceColor turn = PieceColor.WHITE;

    private boolean whiteCanCastleKingside = true;
    private boolean whiteCanCastleQueenside = true;
    private boolean blackCanCastleKingside = true;
    private boolean blackCanCastleQueenside = true;

    private Square enPassantTarget = null;
    private int halfmoveClock = 0;
    private int fullmoveNumber = 1;

    public Piece getPiece(Square square) {
        return grid[square.rank()][square.file()];
    }

    public Piece getPiece(int file, int rank) {
        return grid[rank][file];
    }

    public void setPiece(Square square, Piece piece) {
        grid[square.rank()][square.file()] = piece;
    }

    public void setPiece(int file, int rank, Piece piece) {
        grid[rank][file] = piece;
    }

    public Square findKing(PieceColor color) {
        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                Piece p = grid[r][f];
                if (p != null && p.type() == PieceType.KING && p.color() == color) {
                    return new Square(f, r);
                }
            }
        }
        return null;
    }

    public ChessBoard copy() {
        ChessBoard copy = new ChessBoard();
        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                copy.grid[r][f] = this.grid[r][f];
            }
        }
        copy.turn = this.turn;
        copy.whiteCanCastleKingside = this.whiteCanCastleKingside;
        copy.whiteCanCastleQueenside = this.whiteCanCastleQueenside;
        copy.blackCanCastleKingside = this.blackCanCastleKingside;
        copy.blackCanCastleQueenside = this.blackCanCastleQueenside;
        copy.enPassantTarget = this.enPassantTarget;
        copy.halfmoveClock = this.halfmoveClock;
        copy.fullmoveNumber = this.fullmoveNumber;
        return copy;
    }
}
