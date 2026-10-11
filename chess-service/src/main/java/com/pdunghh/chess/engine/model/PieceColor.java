package com.pdunghh.chess.engine.model;

public enum PieceColor {
    WHITE,
    BLACK;

    public PieceColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
