package model.pieces;

import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.enums.PieceType;

import java.util.List;

public class Pawn extends Piece {
    public Pawn(ChessColor chessColor, Square square) {
        super(chessColor, PieceType.PAWN, square);
    }

    // Specific methods for pawn logic might be useful here or handled in ChessGame/Move logic
    // e.g., canPromote(Position targetPosition), isEnPassantPossible(...)

    public boolean canPromote(Position targetPosition) {

        if (this.chessColor == ChessColor.WHITE) {
            return targetPosition.getRow() == 7;
        } else {
            return targetPosition.getRow() == 0;
        }
    }

    @Override
    public List<Position> getValidMoves(Board board, Square square) {
        List<Position> validMoves = new java.util.ArrayList<>();
        int currentRow = square.getPosition().getRow();
        int currentCol = square.getPosition().getCol();
        int direction = (this.chessColor == ChessColor.WHITE) ? 1 : -1; // +1 for white, -1 for black

        // 1. Standard single forward move
        int targetRow = currentRow + direction;
        if (targetRow >= 0 && targetRow < 8) {
            Position forwardOne = new Position(targetRow, currentCol);
            if (board.getSquareAt(forwardOne).isEmpty()) {
                validMoves.add(forwardOne);

                // 2. Standard double forward move (only if single move is possible)
                int startRank = (this.chessColor == ChessColor.WHITE) ? 1 : 6;
                if (currentRow == startRank) {
                    int targetRowDouble = currentRow + 2 * direction;
                    Position forwardTwo = new Position(targetRowDouble, currentCol);
                    if (board.getSquareAt(forwardTwo).isEmpty()) {
                        validMoves.add(forwardTwo);
                    }
                }
            }
        }

        // 3. Standard Captures (Diagonal)
        int[] captureCols = {currentCol - 1, currentCol + 1};
        for (int captureCol : captureCols) {
            if (captureCol >= 0 && captureCol < 8 && targetRow >= 0 && targetRow < 8) {
                Position capturePos = new Position(targetRow, captureCol);
                Square captureSquare = board.getSquareAt(capturePos);
                if (!captureSquare.isEmpty() && captureSquare.getPiece().getColor() != this.chessColor) {
                    validMoves.add(capturePos);
                }
            }
        }

        // 4. Potential En Passant Moves (Diagonal move to an EMPTY square on the correct rank)
        int enPassantRank = (this.chessColor == ChessColor.WHITE) ? 4 : 3;
        if (currentRow == enPassantRank) {
            for (int captureCol : captureCols) {
                if (captureCol >= 0 && captureCol < 8) {
                    Position potentialEnPassantTargetPos = new Position(targetRow, captureCol); // Target square is diagonal
                    // Check if the diagonal target square is EMPTY
                    if (board.getSquareAt(potentialEnPassantTargetPos).isEmpty()) {
                        // Also check if the adjacent square HAS an opponent pawn (this pawn *might* have just moved two squares)
                        Position adjacentPawnPos = new Position(currentRow, captureCol);
                        Square adjacentSquare = board.getSquareAt(adjacentPawnPos);
                        if (!adjacentSquare.isEmpty() && adjacentSquare.getPiece().getColor() != this.chessColor && adjacentSquare.getPiece().getType() == PieceType.PAWN) {
                            // Add this as a *potential* en passant move.
                            // The ChessGame class will do the final validation using the last move.
                            validMoves.add(potentialEnPassantTargetPos);
                        }
                    }
                }
            }
        }
        // Note: Promotion logic isn't included here yet, but would check if any move ends on the final rank.

        return validMoves;
    }
}
