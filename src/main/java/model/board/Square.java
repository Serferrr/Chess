package model.board;

import model.pieces.Piece;

/**
 * Represents a single square on the chessboard.
 */
public class Square {
    private final Position position;
    private Piece piece; // Can be null if the square is empty

    // Constructor needed (e.g., Square(Position position))
    public Square(Position position, Piece piece) {
        this.position = position;
        this.piece = piece;
    }

    public Position getPosition() {
        return position;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public boolean isEmpty() {
        return piece == null;
    }
}