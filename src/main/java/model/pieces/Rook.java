package model.pieces;

import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.enums.PieceType;

import java.util.List;

public class Rook extends Piece {
    private boolean hasMoved = false;
    public Rook(ChessColor chessColor, Square square) {
        super(chessColor, PieceType.ROOK, square);
    }

    @Override
    public List<Position> getValidMoves(Board board, Square square) {
        return getValidMovesStatic(board,square, this.chessColor);
    }
    static public List<Position> getValidMovesStatic(Board board, Square square, ChessColor chessColor) {
        List<Position> validMoves = new java.util.ArrayList<>(); // Prefer interface type on left, concrete on right

        // Define horizontal and vertical directions
        int[] dx = {0, 0, 1, -1}; // Row changes (Down, Up)
        int[] dy = {1, -1, 0, 0}; // Column changes (Right, Left)

        int startRow = square.getPosition().getRow();
        int startCol = square.getPosition().getCol();

        for (int i = 0; i < 4; i++) { // Iterate through the four directions
            int currentRow = startRow + dx[i];
            int currentCol = startCol + dy[i];

            // Keep moving in the current direction until boundary or piece encountered
            while (currentRow >= 0 && currentRow < 8 && currentCol >= 0 && currentCol < 8) {
                Position currentPosition = new Position(currentRow, currentCol);
                Square targetSquare = board.getSquareAt(currentPosition);

                if (!targetSquare.isEmpty()) {
                    // Square has a piece
                    Piece targetPiece = targetSquare.getPiece();
                    if (targetPiece.getColor() != chessColor) {
                        // It's an opponent's piece - can capture
                        validMoves.add(currentPosition);
                    }
                    // Stop searching in this direction (whether friendly or opponent)
                    break;
                } else {
                    // Square is empty - add as a valid move
                    validMoves.add(currentPosition);
                }

                // Move one step further in the current direction
                currentRow += dx[i];
                currentCol += dy[i];
            }
        }
        return validMoves;
    }
    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

}