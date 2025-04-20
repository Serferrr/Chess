package model.game;

import model.enums.ChessColor;

/**
 * Represents a player in the chess game.
 */
public class Player {
    private final ChessColor chessColor;

    public Player(ChessColor chessColor) {
        this.chessColor = chessColor;
    }

    public ChessColor getColor() {
        return chessColor;
    }
}