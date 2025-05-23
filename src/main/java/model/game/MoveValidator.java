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
 * This class acts as the central authority for determining if a proposed move
 * adheres to all the rules of chess in the current game context.
 */
public class MoveValidator {

    /**
     * Checks if a given move is fully legal in the current game context.
     * This is the main public method that orchestrates various validation checks.
     *
     * @param move        The move object containing details like start/end squares and the piece moved.
     * @param board       The current state of the chessboard.
     * @param currentPlayer The player whose turn it is currently.
     * @param moveHistory The list of moves made previously in the game (crucial for en passant and castling rights).
     * @return true if the move is fully legal according to all chess rules, false otherwise.
     */
    public boolean isMoveLegal(Move move, Board board, Player currentPlayer, List<Move> moveHistory) {
        // --- Initial Sanity Checks ---
        // First, ensure the Move object itself is minimally valid (not null, has required components).
        if (move == null || move.getPieceMoved() == null || move.getStartSquare() == null || move.getEndSquare() == null) {
            // Log an error if the move object is malformed. This indicates a potential programming error elsewhere.
            System.err.println("Validation Error: Invalid Move object provided.");
            return false; // An incomplete move cannot be legal.
        }

        // Extract frequently used information from the move object for readability.
        Piece pieceToMove = move.getPieceMoved();
        Square startSquare = move.getStartSquare();
        Square endSquare = move.getEndSquare();

        // --- Basic Rule Checks ---

        // 1. Check Player Turn: Verify that the piece being moved belongs to the player whose turn it is.
        if (pieceToMove.getColor() != currentPlayer.getColor()) {
            // Optional logging for debugging why a move failed.
            // System.out.println("Validation Fail: Not current player's piece.");
            return false; // Cannot move opponent's piece.
        }

        // 2. Check Basic Piece Movement Rules:
        //    - Get the list of theoretically possible destination positions based on the piece's movement rules
        //      (e.g., a Rook moves horizontally/vertically, a Knight moves in an 'L').
        //    - Note: This list might include moves that are illegal because they would leave the king in check.
        //            That specific check (leaving the king safe) is performed later.
        List<Position> basicMoves = pieceToMove.getValidMoves(board, startSquare); // Assumes Piece subclasses implement this correctly.

        // Check if the move's intended destination is among the basic valid moves for that piece.
        if (!basicMoves.contains(endSquare.getPosition())) {
            // If the destination is NOT in the basic move list, it might still be a special move
            // like castling or en passant, which have unique target squares not always generated by basic rules.
            // We perform a quick check to see if it *looks* like a special move attempt.
            if (!isPotentialSpecialMove(move, board)) {
                // If it's not in the basic list AND doesn't look like a special move, it's definitely illegal.
                // Optional logging:
                // System.out.println("Validation Fail: Not a valid basic or special move target.");
                return false;
            }
            // If it *does* look like a special move, we proceed to the specific validation for those moves.
        }

        // --- Special Move Validation ---
        // These checks handle rules that go beyond simple piece movement patterns.

        // 3. Validate Castling: If the move looks like a castling attempt (King moving two squares).
        if (isCastlingAttempt(move)) {
            // Perform the detailed castling legality checks (king/rook haven't moved, path clear, no checks).
            if (!isCastlingLegal(move, board, currentPlayer)) {
                // Optional logging:
                // System.out.println("Validation Fail: Illegal castling attempt.");
                return false; // Castling rules are violated.
            }
            // If castling is legal, we skip the en passant check and proceed to the final check validation.
        }
        // 4. Validate En Passant: If the move looks like an en passant attempt (pawn diagonal to empty square).
        //    This check is only performed if it wasn't identified as a castling attempt.
        else if (isEnPassantAttempt(move, board)) {
            // Perform the detailed en passant legality checks (based on the immediately preceding move).
            if (!isEnPassantLegal(move, moveHistory)) {
                // Optional logging:
                // System.out.println("Validation Fail: Illegal en passant attempt.");
                return false; // En passant rules are violated.
            }
            // If en passant is legal, proceed to the final check validation.
        }
        // Note on Promotion: Pawn promotion itself doesn't make a move illegal.
        // The move to the final rank is validated like any other pawn move (or capture).
        // The act of promotion is handled *after* the move is confirmed legal and executed.

        // --- Final Check Validation ---

        // 5. Check for Self-Check: The most critical rule - ensure the move does NOT leave the player's own king in check.
        //    This is done by temporarily simulating the move on the board and then checking if the king is attacked.
        // Optional logging:
        // System.out.println("Validation Fail: Move leaves king exposed to check.");
        // The function returns true if the king is *not* left in check.
        return !leavesKingInCheck(move, board, currentPlayer.getColor());

        // If the move passed all the above checks (player turn, basic movement or special move validity,
        // and doesn't leave the king in check), then it is fully legal.
    }

    // --- Helper Methods for Validation Logic ---

    /**
     * Checks if a specific square (targetPos) is under attack by any piece belonging to the attacker's color.
     * This is fundamental for detecting checks on the king and validating castling paths.
     *
     * @param targetPos The position of the square to check for attacks.
     * @param board     The current board state.
     * @param attackerChessColor The color of the pieces that might be attacking the square.
     * @return true if any piece of the attacker's color can legally move to or attack the target square, false otherwise.
     */
    public boolean isSquareAttacked(Position targetPos, Board board, ChessColor attackerChessColor) {
        // Iterate through every square on the board to find potential attacking pieces.
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Square attackerSquare = board.getSquareAt(new Position(r, c));
                Piece potentialAttacker = attackerSquare.getPiece();

                // Check if there's a piece on the square and if it belongs to the attacking color.
                if (potentialAttacker != null && potentialAttacker.getColor() == attackerChessColor) {

                    // --- Special Handling for Pawn Attacks ---
                    // Pawn attacks are unique: they attack diagonally forward, which might not be
                    // included in their standard `getValidMoves` if the target square is empty.
                    // We must check pawn attacks explicitly here.
                    if (potentialAttacker.getType() == PieceType.PAWN) {
                        // Determine the forward direction based on the pawn's color.
                        int direction = (attackerChessColor == ChessColor.WHITE) ? 1 : -1;
                        // Calculate the row the pawn would attack.
                        int attackRow = attackerSquare.getPosition().getRow() + direction;

                        // Check if the target square is on the correct row for a pawn attack.
                        if (attackRow == targetPos.getRow()) {
                            // Check if the target square is exactly one column away (diagonal).
                            if (Math.abs(attackerSquare.getPosition().getCol() - targetPos.getCol()) == 1) {
                                return true; // Pawn's diagonal attack hits the target square.
                            }
                        }
                    }
                    // --- Handling for Other Pieces ---
                    else {
                        // For all pieces other than pawns, their standard valid moves list
                        // accurately represents the squares they attack.
                        List<Position> moves = potentialAttacker.getValidMoves(board, attackerSquare);
                        // Check if the target position is in the list of valid moves for this piece.
                        if (moves.contains(targetPos)) {
                            return true; // The piece attacks the target square.
                        }
                    }
                }
            }
        }
        // If no piece of the attacker's color was found to be attacking the target square after checking all pieces, return false.
        return false;
    }

    /**
     * Checks if the king of the specified color is currently under attack (in check).
     *
     * @param board          The current board state.
     * @param kingChessColor The color of the king to check.
     * @return true if the king is in check, false otherwise.
     */
    public boolean isKingInCheck(Board board, ChessColor kingChessColor) {
        // First, locate the position of the king we need to check.
        Position kingPos = findKingPosition(board, kingChessColor);

        // If the king is not found on the board, this indicates a critical error in the game state.
        if (kingPos == null) {
            throw new IllegalStateException("CRITICAL: King not found for color " + kingChessColor + " on board.");
        }

        // Determine the color of the pieces that would be attacking this king.
        ChessColor attackerChessColor = (kingChessColor == ChessColor.WHITE) ? ChessColor.BLACK : ChessColor.WHITE;

        // Use the isSquareAttacked helper method to see if any opponent piece attacks the king's current position.
        return isSquareAttacked(kingPos, board, attackerChessColor);
    }


    /**
     * Simulates a given move on the board temporarily to determine if it would
     * leave the moving player's king in check. This does NOT permanently change the board state.
     *
     * @param move             The move to simulate.
     * @param board            The current board state (will be temporarily modified and reverted).
     * @param playerChessColor The color of the player making the move.
     * @return true if the move results in the player's own king being in check, false otherwise.
     */
    private boolean leavesKingInCheck(Move move, Board board, ChessColor playerChessColor) {
        // Get references to the start and end squares involved in the move.
        Square start = move.getStartSquare();
        Square end = move.getEndSquare();
        // Get the piece being moved.
        Piece movedPiece = start.getPiece();
        // Store the piece currently occupying the destination square (could be null if empty).
        // This is needed to revert the board later.
        Piece capturedPiece = end.getPiece();
        // Variable to store the result (whether the king is in check after the simulated move).
        boolean check;

        // --- Simulate the move ---
        // This section temporarily modifies the board object.

        // 1. Handle En Passant Simulation: If the move is an en passant attempt,
        //    we need to temporarily remove the *actual* captured pawn, which is
        //    adjacent to the start square, not on the end square.
        Piece capturedEnPassantPawn = null; // Stores the pawn captured via en passant, if any.
        Square capturedEnPassantSquare = null; // Stores the square of the pawn captured via en passant.

        // Check if the move looks like a pawn moving diagonally to an empty square.
        if (isEnPassantAttempt(move, board)) {
            // Calculate the position of the pawn that *would* be captured en passant.
            // It's on the same row as the moving pawn's start square,
            // but in the same column as the moving pawn's end square.
            int capturedPawnRow = start.getPosition().getRow();
            int capturedPawnCol = end.getPosition().getCol();
            Position capturedPawnPos = new Position(capturedPawnRow, capturedPawnCol);

            // Verify that there is indeed an opponent's pawn at that position.
            // This check ensures we only simulate valid en passant captures.
            Piece potentialPawn = board.getPieceAt(capturedPawnPos);
            if (potentialPawn != null && potentialPawn.getType() == PieceType.PAWN && potentialPawn.getColor() != playerChessColor) {
                // If valid, store the pawn and its square for later restoration.
                capturedEnPassantSquare = board.getSquareAt(capturedPawnPos);
                capturedEnPassantPawn = potentialPawn;
                // Temporarily remove the captured pawn from the board for the simulation.
                capturedEnPassantSquare.setPiece(null);
            }
            // If the adjacent square didn't contain the expected opponent pawn,
            // it wasn't a valid en passant, so we don't remove anything here.
            // The move would likely fail later validation anyway.
        }

        // 2. Perform the Standard Piece Movement:
        //    Place the moving piece onto the destination square.
        end.setPiece(movedPiece);
        //    Remove the moving piece from its starting square.
        start.setPiece(null);
        // Note: If the `movedPiece` is the King, `findKingPosition` used within
        // `isKingInCheck` will now find it at the `end` square during the check.

        // --- Check for Check in Simulated State ---
        // Now that the board reflects the state *after* the move,
        // check if the player's king is under attack in this new configuration.
        check = isKingInCheck(board, playerChessColor);

        // --- Revert the Board State ---
        // Crucially, undo the temporary changes made during the simulation
        // to restore the board to its original state before this method was called.

        // 1. Put the moved piece back onto its original starting square.
        start.setPiece(movedPiece);
        // 2. Restore the piece (or null) that was originally on the destination square.
        end.setPiece(capturedPiece);
        // 3. If an en passant capture was simulated, put the captured pawn back on its square.
        if (capturedEnPassantSquare != null) {
            capturedEnPassantSquare.setPiece(capturedEnPassantPawn);
        }

        // Return the result: true if the simulated move resulted in check, false otherwise.
        return check;
    }


    // --- Special Move Helper Methods ---

    /**
     * Performs a quick check to see if a move *could potentially* be a special move
     * (castling or en passant) based on its basic characteristics, without
     * checking the full legality yet. Used in `isMoveLegal` to decide if
     * further special move validation is needed when a move isn't in the basic move list.
     *
     * @param move  The move to check.
     * @param board The current board state.
     * @return true if the move involves a King moving two squares or a Pawn moving diagonally to an empty square, false otherwise.
     */
    private boolean isPotentialSpecialMove(Move move, Board board) {
        // A move might be special if it's either a castling attempt OR an en passant attempt.
        return isCastlingAttempt(move) || isEnPassantAttempt(move, board);
    }

    /**
     * Checks if a move looks like a castling attempt.
     * This is a preliminary check based only on the piece type and movement distance.
     * Full legality is checked by `isCastlingLegal`.
     *
     * @param move The move to check.
     * @return true if the move involves the King moving exactly two squares horizontally, false otherwise.
     */
    boolean isCastlingAttempt(Move move) {
        Piece piece = move.getPieceMoved();
        // Castling is defined as the King moving exactly two squares horizontally.
        return piece.getType() == PieceType.KING &&
                Math.abs(move.getEndSquare().getPosition().getCol() - move.getStartSquare().getPosition().getCol()) == 2;
    }

    /**
     * Performs a comprehensive check for the legality of a castling move.
     * Assumes `isCastlingAttempt` was likely true for this move.
     * Verifies all specific conditions required for castling.
     *
     * @param move        The castling move attempt (King moving two squares).
     * @param board       The current board state.
     * @param currentPlayer The player attempting to castle.
     * @return true if all castling conditions are met, false otherwise.
     */
    private boolean isCastlingLegal(Move move, Board board, Player currentPlayer) {
        // Recap of Castling Conditions:
        // 1. The King must not currently be in check.
        // 2. The King and the chosen Rook must not have moved previously in the game.
        // 3. All squares between the King and the chosen Rook must be empty.
        // 4. The King must not pass through any square that is under attack by an opponent's piece.
        // 5. The King must not land on a square that is under attack by an opponent's piece.

        // Get positions and colors for easier reference.
        Position startPos = move.getStartSquare().getPosition(); // King's starting position (e1 or e8)
        Position endPos = move.getEndSquare().getPosition();     // King's ending position (g1/c1 or g8/c8)
        ChessColor playerChessColor = currentPlayer.getColor();
        ChessColor opponentChessColor = (playerChessColor == ChessColor.WHITE) ? ChessColor.BLACK : ChessColor.WHITE;
        Piece movingKing = move.getPieceMoved(); // Get the actual King piece object.

        // Safety check: Ensure the piece attempting to castle is actually a King.
        if (!(movingKing instanceof King)) {
            // This should ideally not happen if `isCastlingAttempt` was called first, but it's a good safeguard.
            return false;
        }

        // Condition 1: Check if the King is currently in check.
        if (isKingInCheck(board, playerChessColor)) {
            // Optional logging:
            // System.out.println("Castling Fail: King in check");
            return false; // Cannot castle out of check.
        }

        // Condition 2: Check if the King has moved before.
        // This relies on the `hasMoved` flag being correctly maintained in the King class.
        if (((King) movingKing).hasMoved()) {
            // Optional logging:
            // System.out.println("Castling Fail: King has moved");
            return false; // King has lost castling rights.
        }

        // Determine which Rook is involved based on the direction the King is moving.
        // If end column > start column, it's Kingside (column 7 rook). Otherwise, Queenside (column 0 rook).
        int rookCol = (endPos.getCol() > startPos.getCol()) ? 7 : 0;
        // Construct the position of the involved Rook.
        Position rookPos = new Position(startPos.getRow(), rookCol);
        // Get the piece object at the Rook's starting position.
        Piece rook = board.getPieceAt(rookPos);

        // Condition 2 (continued): Check the Rook.
        // - Must be a Rook.
        // - Must be the same color as the King.
        // - Must not have moved before (check its `hasMoved` flag).
        if (!(rook instanceof Rook) || rook.getColor() != playerChessColor || ((Rook) rook).hasMoved()) {
            // Optional logging:
            // System.out.println("Castling Fail: Rook missing, wrong type/color, or has moved");
            return false; // Rook is missing, wrong piece, wrong color, or has lost castling rights.
        }

        // Condition 3: Check if the path between the King and the Rook is clear.
        // Determine the direction to step through the columns (1 for Kingside, -1 for Queenside).
        int step = (rookCol == 7) ? 1 : -1;
        // Iterate through the columns strictly *between* the King's start column and the Rook's column.
        for (int c = startPos.getCol() + step; c != rookCol; c += step) {
            // Check if the square at the current intermediate column is empty.
            if (!board.isEmpty(new Position(startPos.getRow(), c))) {
                // Optional logging:
                // System.out.println("Castling Fail: Path blocked at col " + c);
                return false; // Path is blocked.
            }
        }

        // Conditions 4 & 5: Check if the King passes through or lands on an attacked square.
        // Determine the direction the King steps (1 for Kingside, -1 for Queenside).
        int kingStep = (endPos.getCol() > startPos.getCol()) ? 1 : -1;
        // Iterate through all squares the King occupies during the castle:
        // its starting square, the intermediate square it passes over, and its final destination square.
        // The loop condition `c != endPos.getCol() + kingStep` ensures the loop includes the end position.
        for (int c = startPos.getCol(); c != endPos.getCol() + kingStep; c += kingStep) {
            // Check if the current square in the King's path is attacked by the opponent.
            if (isSquareAttacked(new Position(startPos.getRow(), c), board, opponentChessColor)) {
                // Optional logging:
                // System.out.println("Castling Fail: King path attacked at col " + c);
                return false; // King passes through or lands on an attacked square.
            }
        }

        // If all the above conditions (1 through 5) have been met, the castling move is legal.
        return true;
    }


    /**
     * Checks if a move looks like an en passant capture attempt.
     * This is a preliminary check: it verifies if a Pawn is trying to move
     * diagonally to an *empty* square. Full legality is checked by `isEnPassantLegal`.
     *
     * @param move  The move to check.
     * @param board The current board state.
     * @return true if the move is a Pawn moving diagonally to an empty square, false otherwise.
     */
    boolean isEnPassantAttempt(Move move, Board board) {
        Piece piece = move.getPieceMoved();
        // En passant can only be performed by a Pawn.
        if (piece.getType() != PieceType.PAWN) return false;

        // En passant involves a diagonal move (column changes).
        boolean isDiagonal = move.getStartSquare().getPosition().getCol() != move.getEndSquare().getPosition().getCol();
        // The key characteristic: the destination square *must* be empty for en passant.
        boolean targetIsEmpty = board.getSquareAt(move.getEndSquare().getPosition()).isEmpty();

        // If it's a diagonal pawn move to an empty square, it *might* be en passant.
        return isDiagonal && targetIsEmpty;
    }

    /**
     * Performs a comprehensive check for the legality of an en passant capture.
     * Assumes `isEnPassantAttempt` was likely true for this move.
     * Verifies the conditions based on the immediately preceding move in the game history.
     *
     * @param move        The en passant move attempt (Pawn moving diagonally to an empty square).
     * @param moveHistory The list of previous moves, essential for checking the last move.
     * @return true if all en passant conditions are met, false otherwise.
     */
    boolean isEnPassantLegal(Move move, List<Move> moveHistory) {
        // En passant requires a previous move to have occurred.
        if (moveHistory.isEmpty()) return false;

        // Get the *immediately* preceding move from the history.
        Move lastMove = moveHistory.getLast(); // Assumes List allows efficient access to the last element.
        Piece lastMovedPiece = lastMove.getPieceMoved();
        // Get the square where the pawn attempting the capture currently is.
        Square capturingPawnSquare = move.getStartSquare();
        // Get the (empty) square the capturing pawn is moving to.
        Square targetSquare = move.getEndSquare();
        // Get the color of the pawn attempting the capture.
        ChessColor attackerChessColor = move.getPieceMoved().getColor();

        // Condition 1: The last move must have been made by an opponent's pawn.
        if (lastMovedPiece == null || lastMovedPiece.getType() != PieceType.PAWN || lastMovedPiece.getColor() == attackerChessColor) {
            return false; // Last move wasn't by an opponent's pawn.
        }

        // Condition 2: The opponent's pawn's last move must have been a two-square advance from its starting rank.
        if (Math.abs(lastMove.getEndSquare().getPosition().getRow() - lastMove.getStartSquare().getPosition().getRow()) != 2) {
            return false; // Last move wasn't a two-square pawn advance.
        }

        // Condition 3: The opponent's pawn must have landed on a square directly adjacent horizontally
        //              to the square where the capturing pawn currently resides.
        // Check if they are on the same row.
        if (lastMove.getEndSquare().getPosition().getRow() != capturingPawnSquare.getPosition().getRow()) {
            return false; // Opponent pawn didn't land on the same rank.
        }
        // Check if they are in adjacent columns.
        if (Math.abs(lastMove.getEndSquare().getPosition().getCol() - capturingPawnSquare.getPosition().getCol()) != 1) {
            return false; // Opponent pawn didn't land in an adjacent column.
        }

        // Condition 4: The target square for the capturing pawn must be the square directly "behind"
        //              the square where the opponent's pawn landed (as if it had only moved one square).
        // Calculate the expected row for the en passant target square.
        int expectedTargetRow = capturingPawnSquare.getPosition().getRow() + ((attackerChessColor == ChessColor.WHITE) ? 1 : -1);
        // The expected column is the same as the column where the opponent's pawn landed.
        int expectedTargetCol = lastMove.getEndSquare().getPosition().getCol();

        // Check if the actual target square of the move matches the calculated expected en passant target square.
        return targetSquare.getPosition().getRow() == expectedTargetRow && targetSquare.getPosition().getCol() == expectedTargetCol;

        // If all the above conditions are met, the en passant capture is legal.
    }

    // --- Utility ---

    /**
     * Finds the current position of the king for the specified color on the board.
     *
     * @param board          The current board state.
     * @param kingChessColor The color of the king to find.
     * @return The Position object representing the king's location, or null if the king is not found (which indicates an invalid game state).
     */
    private Position findKingPosition(Board board, ChessColor kingChessColor) {
        // Iterate through all squares on the board.
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece piece = board.getPieceAt(pos);
                // Check if the square contains a piece, if it's a King, and if it matches the desired color.
                if (piece != null && piece.getType() == PieceType.KING && piece.getColor() == kingChessColor) {
                    // If found, return its position immediately.
                    return pos;
                }
            }
        }
        // If the loops complete without finding the king, return null.
        // This situation should ideally not occur during a valid chess game.
        return null;
    }
}