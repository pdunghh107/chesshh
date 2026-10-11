package com.pdunghh.chess.engine.model;

import lombok.Getter;

@Getter
public enum PieceType {
    PAWN('p'),
    KNIGHT('n'),
    BISHOP('b'),
    ROOK('r'),
    QUEEN('q'),
    KING('k');

    private final char symbol;

    PieceType(char symbol) {
        this.symbol = symbol;
    }

    public static PieceType fromChar(char c) {
        char lower = Character.toLowerCase(c);
        for (PieceType type : values()) {
            if (type.symbol == lower) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown piece type: " + c);
    }
}
