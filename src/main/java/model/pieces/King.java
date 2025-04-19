package model.pieces;

import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.enums.PieceType;

import java.util.List;

public class King extends Piece {
    private boolean hasMoved = false;
    public King(ChessColor chessColor, Square square) {
        super(chessColor, PieceType.KING, square);
    }

    @Override
    public List<Position> getValidMoves(Board board, Square square) {
        List<Position> validMoves = new java.util.ArrayList<>();
        int row = square.getPosition().getRow();
        int col = square.getPosition().getCol();

        int[] rowOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colOffsets = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            int newRow = row + rowOffsets[i];
            int newCol = col + colOffsets[i];

            if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
                Square targetSquare = board.getSquareAt(new Position(newRow, newCol));
                if (targetSquare.isEmpty() || targetSquare.getPiece().getColor() != this.getColor()) {
                    validMoves.add(new Position(newRow, newCol));
                }
            }
        }
        return validMoves;

    }
    // --- Add hasMoved getter and setter ---
    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
}