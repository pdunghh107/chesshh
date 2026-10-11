package com.pdunghh.chess.engine.model;

public record Piece(PieceType type, PieceColor color) {

    public char toFenChar() {
        char c = type.getSymbol();
        return color == PieceColor.WHITE ? Character.toUpperCase(c) : c;
    }

    public static Piece fromFenChar(char c) {
        PieceColor color = Character.isUpperCase(c) ? PieceColor.WHITE : PieceColor.BLACK;
        PieceType type = PieceType.fromChar(c);
        return new Piece(type, color);
    }
}
