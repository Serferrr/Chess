package model.game;

import model.enums.ChessColor;

/**
 * Represents a player in the chess game.
 */
public class Player {
    private final ChessColor chessColor;
    // Optional: Track captured pieces
    // private List<Piece> capturedPieces;

    public Player(ChessColor chessColor) {
        this.chessColor = chessColor;
    }

    public ChessColor getColor() {
        return chessColor;
    }

    // Optional methods if tracking captures
    // public void addCapturedPiece(Piece piece) { ... }
    // public List<Piece> getCapturedPieces() { ... }
}