package com.pdunghh.chess.engine.model;

public record Square(int file, int rank) {

    public Square {
        if (file < 0 || file > 7 || rank < 0 || rank > 7) {
            throw new IllegalArgumentException("Square out of bounds: file=" + file + ", rank=" + rank);
        }
    }

    public static Square fromAlgebraic(String s) {
        if (s == null || s.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + s);
        }
        int file = s.charAt(0) - 'a';
        int rank = s.charAt(1) - '1';
        return new Square(file, rank);
    }

    public String toAlgebraic() {
        return "" + (char) ('a' + file) + (char) ('1' + rank);
    }

    public static boolean isValid(int file, int rank) {
        return file >= 0 && file <= 7 && rank >= 0 && rank <= 7;
    }
}
