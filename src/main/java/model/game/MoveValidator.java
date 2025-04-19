package model.game; // Or consider model.game or model.logic

import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.enums.PieceType;
import model.pieces.*;     // Import necessary piece types

import java.util.List;

/**
 * Validates chess moves considering game rules beyond basic piece movement,
 * such as checks, castling, and en passant legality based on game history.
 */
public class MoveValidator {

    /**
     * Checks if a given move is fully legal in the current game context.
     *
     * @param move        The move to validate.
     * @param board       The current state of the board.
     * @param currentPlayer The player attempting the move.
     * @param moveHistory The history of moves made so far (needed for en passant, castling).
     * @return true if the move is legal, false otherwise.
     */
    public boolean isMoveLegal(Move move, Board board, Player currentPlayer, List<Move> moveHistory) {
        if (move == null || move.getPieceMoved() == null || move.getStartSquare() == null || move.getEndSquare() == null) {
            System.err.println("Validation Error: Invalid Move object provided.");
            return false; // Basic sanity check
        }

        Piece pieceToMove = move.getPieceMoved();
        Square startSquare = move.getStartSquare();
        Square endSquare = move.getEndSquare();

        // --- Basic Checks ---
        // 1. Is it the correct player's turn?
        if (pieceToMove.getColor() != currentPlayer.getColor()) {
            // System.out.println("Validation Fail: Not current player's piece."); // Optional logging
            return false;
        }

        // 2. Is the move valid according to the piece's basic movement rules?
        //    (Leverage existing getValidMoves, but be aware it might include moves illegal due to check)
        List<Position> basicMoves = pieceToMove.getValidMoves(board, startSquare); // Assuming this signature
        if (!basicMoves.contains(endSquare.getPosition())) {
            // If not a basic move, check if it's a valid special move attempt (castling/en passant)
            // that might not be in the basic list.
            if (!isPotentialSpecialMove(move, board)) {
                // System.out.println("Validation Fail: Not a valid basic or special move target.");
                return false;
            }
        }

        // --- Special Move Validation ---
        // 3. Validate Castling (if attempted)
        if (isCastlingAttempt(move)) {
            if (!isCastlingLegal(move, board, currentPlayer, moveHistory)) {
                // System.out.println("Validation Fail: Illegal castling attempt.");
                return false;
            }
            // 4. Validate En Passant (if attempted)
        } else if (isEnPassantAttempt(move, board)) {
            if (!isEnPassantLegal(move, board, moveHistory)) {
                // System.out.println("Validation Fail: Illegal en passant attempt.");
                return false;
            }
        }
        // Note: Promotion doesn't usually invalidate a move, it's a consequence handled after.

        // --- Check Validation ---
        // 5. Does the move leave the player's own king in check? (Most critical check)
        // System.out.println("Validation Fail: Move leaves king exposed to check.");
        return !leavesKingInCheck(move, board, currentPlayer.getColor());

        // If all checks pass, the move is legal in this context
    }

    // --- Helper Methods for Validation Logic ---

    /**
     * Checks if a square is attacked by any piece of the specified attacker color.
     * Crucial for check detection and castling validation.
     */
    public boolean isSquareAttacked(Position targetPos, Board board, ChessColor attackerChessColor) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Square attackerSquare = board.getSquareAt(new Position(r, c));
                Piece potentialAttacker = attackerSquare.getPiece();

                if (potentialAttacker != null && potentialAttacker.getColor() == attackerChessColor) {
                    // Get basic moves - need to handle pawn attacks specifically
                    List<Position> moves = potentialAttacker.getValidMoves(board, attackerSquare);

                    if (potentialAttacker.getType() == PieceType.PAWN) {
                        // Pawns attack diagonally forward, even if getValidMoves doesn't list captures for this check
                        int direction = (attackerChessColor == ChessColor.WHITE) ? 1 : -1;
                        int attackRow = attackerSquare.getPosition().getRow() + direction;
                        if (attackRow == targetPos.getRow()) { // Must be on the correct row
                            if (Math.abs(attackerSquare.getPosition().getCol() - targetPos.getCol()) == 1) {
                                return true; // Diagonal attack hits the target
                            }
                        }
                    } else {
                        // For other pieces, check if their standard moves hit the target
                        if (moves.contains(targetPos)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Checks if the specified player's king is currently in check. */
    public boolean isKingInCheck(Board board, ChessColor kingChessColor) {
        Position kingPos = findKingPosition(board, kingChessColor);
        if (kingPos == null) {
            System.err.println("CRITICAL: King not found for color " + kingChessColor);
            return false; // Or throw exception - indicates broken state
        }
        ChessColor attackerChessColor = (kingChessColor == ChessColor.WHITE) ? ChessColor.BLACK : ChessColor.WHITE;
        return isSquareAttacked(kingPos, board, attackerChessColor);
    }


    /**
     * Simulates a move temporarily and checks if it results in the player's king being in check.
     */
    private boolean leavesKingInCheck(Move move, Board board, ChessColor playerChessColor) {
        Square start = move.getStartSquare();
        Square end = move.getEndSquare();
        Piece movedPiece = start.getPiece();
        Piece capturedPiece = end.getPiece(); // Store piece at destination (could be null)
        boolean check;

        // --- Simulate the move ---
        // 1. Handle potential en passant capture (remove pawn adjacent to start, same rank as end col)
        Piece capturedEnPassantPawn = null;
        Square capturedEnPassantSquare = null;
        if (isEnPassantAttempt(move, board)) { // Check if it looks like en passant
            int capturedPawnRow = start.getPosition().getRow();
            int capturedPawnCol = end.getPosition().getCol();
            Position capturedPawnPos = new Position(capturedPawnRow, capturedPawnCol);
            // Check if there's actually a pawn there to capture en passant
            Piece potentialPawn = board.getPieceAt(capturedPawnPos);
            if (potentialPawn != null && potentialPawn.getType() == PieceType.PAWN && potentialPawn.getColor() != playerChessColor) {
                capturedEnPassantSquare = board.getSquareAt(capturedPawnPos);
                capturedEnPassantPawn = potentialPawn;
                capturedEnPassantSquare.setPiece(null); // Temporarily remove
            }
        }

        // 2. Move the piece
        end.setPiece(movedPiece);
        start.setPiece(null);
        // If the moved piece was the king, update its position for check detection
        // (findKingPosition will find it in the new spot)

        // --- Check for check in the simulated state ---
        check = isKingInCheck(board, playerChessColor);

        // --- Revert the board state ---
        start.setPiece(movedPiece); // Put moved piece back
        end.setPiece(capturedPiece); // Restore original piece at destination (or null)
        if (capturedEnPassantSquare != null) { // Put back en passant captured pawn if applicable
            capturedEnPassantSquare.setPiece(capturedEnPassantPawn);
        }

        return check;
    }


    // --- Special Move Helpers ---

    private boolean isPotentialSpecialMove(Move move, Board board) {
        return isCastlingAttempt(move) || isEnPassantAttempt(move, board);
    }

    boolean isCastlingAttempt(Move move) {
        Piece piece = move.getPieceMoved();
        // King moves exactly two squares horizontally
        return piece.getType() == PieceType.KING &&
                Math.abs(move.getEndSquare().getPosition().getCol() - move.getStartSquare().getPosition().getCol()) == 2;
    }

    private boolean isCastlingLegal(Move move, Board board, Player currentPlayer, List<Move> moveHistory) {
        // Complex checks needed:
        // 1. King is not in check currently.
        // 2. King and the chosen Rook have not moved previously.
        // 3. Squares between King and Rook are empty.
        // 4. King does not pass through a square that is under attack.
        // 5. King does not land on a square that is under attack.

        Position startPos = move.getStartSquare().getPosition();
        Position endPos = move.getEndSquare().getPosition();
        ChessColor playerChessColor = currentPlayer.getColor();
        ChessColor opponentChessColor = (playerChessColor == ChessColor.WHITE) ? ChessColor.BLACK : ChessColor.WHITE;
        Piece movingKing = move.getPieceMoved(); // Get the actual King piece

        // Ensure the piece being moved is actually a King
        if (!(movingKing instanceof King)) {
            return false; // Should not happen if isCastlingAttempt is correct, but good safety check
        }

        // 1. Check if currently in check
        if (isKingInCheck(board, playerChessColor)) {
            // System.out.println("Castling Fail: King in check"); // Optional debug
            return false;
        }

        // 2. Check if King has moved
        if (((King) movingKing).hasMoved()) {
            // System.out.println("Castling Fail: King has moved"); // Optional debug
            return false;
        }

        // Determine which rook is involved
        int rookCol = (endPos.getCol() > startPos.getCol()) ? 7 : 0; // Kingside or Queenside
        Position rookPos = new Position(startPos.getRow(), rookCol);
        Piece rook = board.getPieceAt(rookPos);

        // Check if Rook exists, is correct type/color, and hasn't moved
        if (!(rook instanceof Rook) || rook.getColor() != playerChessColor || ((Rook) rook).hasMoved()) {
            // System.out.println("Castling Fail: Rook missing, wrong type/color, or has moved"); // Optional debug
            return false;
        }

        // 3. Check if path is clear between King and Rook
        int step = (rookCol == 7) ? 1 : -1;
        for (int c = startPos.getCol() + step; c != rookCol; c += step) {
            if (!board.isEmpty(new Position(startPos.getRow(), c))) {
                // System.out.println("Castling Fail: Path blocked at col " + c); // Optional debug
                return false;
            }
        }

        // 4. & 5. Check if King passes through or lands on attacked square
        // Note: The loop needs to check the squares the *king* moves over/to: start, intermediate, end
        int kingStep = (endPos.getCol() > startPos.getCol()) ? 1 : -1;
        for (int c = startPos.getCol(); c != endPos.getCol() + kingStep; c += kingStep) {
            if (isSquareAttacked(new Position(startPos.getRow(), c), board, opponentChessColor)) {
                // System.out.println("Castling Fail: King path attacked at col " + c); // Optional debug
                return false;
            }
        }

        // Remove the warning and placeholder return
        // System.err.println("Warning: Castling 'hasMoved' check is not fully implemented.");
        // return true; // Placeholder - needs full 'hasMoved' logic

        // If all checks pass, castling is legal
        return true;
    }


    boolean isEnPassantAttempt(Move move, Board board) {
        Piece piece = move.getPieceMoved();
        if (piece.getType() != PieceType.PAWN) return false;

        // Is it a diagonal move?
        boolean isDiagonal = move.getStartSquare().getPosition().getCol() != move.getEndSquare().getPosition().getCol();
        // Is the target square empty?
        boolean targetIsEmpty = board.getSquareAt(move.getEndSquare().getPosition()).isEmpty();

        return isDiagonal && targetIsEmpty;
    }

    boolean isEnPassantLegal(Move move, Board board, List<Move> moveHistory) {
        if (moveHistory.isEmpty()) return false;

        Move lastMove = moveHistory.getLast();
        Piece lastMovedPiece = lastMove.getPieceMoved();
        Square capturingPawnSquare = move.getStartSquare();
        Square targetSquare = move.getEndSquare();
        ChessColor attackerChessColor = move.getPieceMoved().getColor();

        // 1. Last move must have been made by an opponent's pawn.
        if (lastMovedPiece == null || lastMovedPiece.getType() != PieceType.PAWN || lastMovedPiece.getColor() == attackerChessColor) {
            return false;
        }

        // 2. Last move must have been a two-square advance.
        if (Math.abs(lastMove.getEndSquare().getPosition().getRow() - lastMove.getStartSquare().getPosition().getRow()) != 2) {
            return false;
        }

        // 3. The opponent's pawn must have landed directly adjacent (same rank, adjacent column) to the capturing pawn.
        if (lastMove.getEndSquare().getPosition().getRow() != capturingPawnSquare.getPosition().getRow() ||
                Math.abs(lastMove.getEndSquare().getPosition().getCol() - capturingPawnSquare.getPosition().getCol()) != 1) {
            return false;
        }

        // 4. The target square for the capture must be the one "behind" the opponent's pawn.
        int expectedTargetRow = capturingPawnSquare.getPosition().getRow() + ((attackerChessColor == ChessColor.WHITE) ? 1 : -1);
        int expectedTargetCol = lastMove.getEndSquare().getPosition().getCol(); // Target col is same as opponent pawn's landing col

        return targetSquare.getPosition().getRow() == expectedTargetRow && targetSquare.getPosition().getCol() == expectedTargetCol;

        // If all conditions met, it's a legal en passant capture
    }

    // --- Utility ---

    /** Finds the position of the king for the given color. Returns null if not found. */
    private Position findKingPosition(Board board, ChessColor kingChessColor) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece piece = board.getPieceAt(pos);
                if (piece != null && piece.getType() == PieceType.KING && piece.getColor() == kingChessColor) {
                    return pos;
                }
            }
        }
        return null; // Should ideally not happen in a valid game
    }
}
