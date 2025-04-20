package model.game;

import model.board.Board;
import model.enums.PieceType;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.pieces.*;
import model.enums.GameState;

import java.util.ArrayList;
import java.util.List;
// No need for Collectors import anymore

/**
 * Central orchestrator for the chess game logic.
 * This class manages the overall game flow, including:
 * - Maintaining the state of the board.
 * - Tracking the current player.
 * - Managing the game state (ongoing, check, checkmate, stalemate).
 * - Validating and executing moves.
 * - Generating legal moves.
 * - Keeping a history of moves made.
 */
public class ChessGame {
    // The chessboard instance, holding all squares and pieces.
    private final Board board;
    // Array to hold the two players (White and Black).
    private final Player[] players = new Player[2]; // Made final as the players themselves don't change.
    // Reference to the player whose turn it is currently.
    private Player currentPlayer;
    // Enum representing the current status of the game (e.g., ONGOING, CHECK, CHECKMATE_WHITE_WINS).
    private GameState gameState;
    // List to store the sequence of moves made during the game. Useful for history, undo, and en passant validation.
    private final List<Move> moveHistory = new ArrayList<>(); // Made final as the list reference doesn't change.
    // Instance of the MoveValidator, responsible for checking move legality according to chess rules.
    private final MoveValidator moveValidator = new MoveValidator();

    /**
     * Constructor for ChessGame.
     * Initializes the board, creates the players, and starts the first game.
     */
    public ChessGame() {
        // Create a new Board object.
        board = new Board();
        // Create the White player (index 0).
        players[0] = new Player(ChessColor.WHITE);
        // Create the Black player (index 1).
        players[1] = new Player(ChessColor.BLACK);
        // Initialize the game state (setup board, set starting player, etc.).
        startGame(); // Let's start the game by default in the constructor.
    }

    /**
     * Sets up the board for a new game and initializes the game state.
     * This method can be called to start a fresh game or restart the current one.
     */
    public void startGame() {
        // Ensure the board is empty before setting up pieces.
        board.clearBoard();
        // Place all pieces in their standard starting positions.
        board.setupBoard(); // Assumes this places pieces in starting positions.
        // Set the current player to White, who always starts.
        currentPlayer = players[0];
        // Set the initial game state to ONGOING.
        gameState = GameState.ONGOING;
        // Clear the move history from any previous game.
        moveHistory.clear();
        // Reset the 'hasMoved' flag for all Kings and Rooks, which is crucial for castling rights.
        resetPieceMovedStatus();
    }

    /**
     * Resets the 'hasMoved' flag for all Kings and Rooks on the board.
     * This is essential at the start of a new game to restore castling rights.
     */
    private void resetPieceMovedStatus() {
        // Iterate through all rows of the board.
        for (int r = 0; r < 8; r++) {
            // Iterate through all columns of the board.
            for (int c = 0; c < 8; c++) {
                // Get the piece at the current row and column.
                Piece piece = board.getPieceAt(new Position(r, c));
                // Check if the piece is a King.
                if (piece instanceof King) {
                    // If it's a King, cast it and set its hasMoved flag to false.
                    ((King) piece).setHasMoved(false);
                }
                // Check if the piece is a Rook.
                else if (piece instanceof Rook) {
                    // If it's a Rook, cast it and set its hasMoved flag to false.
                    ((Rook) piece).setHasMoved(false);
                }
            }
        }
    }


    /**
     * Validates if a given move is legal within the current game context.
     * It performs basic sanity checks and then delegates the complex rule checking
     * to the MoveValidator instance.
     *
     * @param move The move object to validate.
     * @return true if the move is legal according to all chess rules, false otherwise.
     */
    public boolean validateMove(Move move) {
        // --- Initial Sanity Check ---
        // Ensure the move object itself isn't null and has the essential components defined.
        // This prevents NullPointerExceptions later and catches potential programming errors.
        if (move == null || move.getPieceMoved() == null || move.getStartSquare() == null || move.getEndSquare() == null) {
            // Log an error message indicating an invalid Move object was passed.
            System.err.println("Validation Error: Attempted to validate an incomplete Move object.");
            // Return false as an incomplete move cannot be valid.
            return false;
        }
        // --- Delegate to MoveValidator ---
        // Pass the move, board, current player, and move history to the MoveValidator
        // for comprehensive rule checking (including checks, pins, special moves, etc.).
        return moveValidator.isMoveLegal(move, board, currentPlayer, moveHistory);
    }

    /**
     * Attempts to make a move on the board.
     * 1. Validates the move using `validateMove`.
     * 2. If valid, executes the move on the board using `executeMove`.
     * 3. Records the move in the `moveHistory`.
     * 4. Switches the `currentPlayer`.
     * 5. Updates the `gameState` (checking for checkmate, stalemate, etc.).
     * <p>
     * This method expects a fully constructed Move object, potentially including
     * promotion type if applicable, as created by the UI or move generation logic.
     *
     * @param move The move to attempt (should include promotion type if it's a promotion).
     * @return true if the move was successfully validated and executed, false otherwise.
     */
    public boolean makeMove(Move move) {
        // 1. --- Validate the Move ---
        // Use the validateMove method (which delegates to MoveValidator) to check full legality.
        if (!validateMove(move)) {
            // If the move is invalid, log an error (using System.err is common for errors).
            System.err.println("Invalid move attempted: " + move);
            // Return false to indicate the move was not made.
            return false;
        }

        // 2. --- Execute the Move ---
        // If the move is legal, physically change the board state according to the move details.
        // The executeMove method handles piece placement, captures, and special move side effects.
        // It relies on the flags (isCastling, isEnPassant, isPromotion) already set in the validated Move object.
        executeMove(move);

        // 3. --- Record History ---
        // Add the successfully executed move to the game's history list.
        // This is done *before* switching the player.
        moveHistory.add(move);

        // 4. --- Switch Player ---
        // Change the `currentPlayer` reference to the other player.
        switchPlayer();

        // 5. --- Update Game State ---
        // After the move and player switch, determine the new state of the game
        // for the player whose turn it *now* is (check for check, checkmate, stalemate).
        updateGameState();

        // Return true to indicate the move was successfully processed.
        return true;
    }


    /**
     * Executes the physical changes on the board for a given validated move.
     * This method handles standard piece movement, captures, and the side effects
     * of special moves like en passant and castling, and promotion.
     * It assumes the move has already been fully validated by `validateMove`.
     *
     * @param move The validated legal move object containing all necessary details.
     */
    private void executeMove(Move move) {
        // Get references to the start and end squares from the move object.
        Square startSquare = move.getStartSquare();
        Square endSquare = move.getEndSquare();
        // Get the piece that is being moved.
        Piece movedPiece = move.getPieceMoved();

        // --- Handle Special Move Side Effects ---
        // These actions happen in addition to the main piece movement.

        // 1. En Passant Capture:
        // If the move is flagged as an en passant capture...
        if (move.isEnPassant()) {
            // Calculate the position of the pawn that is *actually* captured.
            // It's on the same row as the capturing pawn's start square,
            // but in the same column as the capturing pawn's destination square.
            int capturedPawnRow = startSquare.getPosition().getRow();
            int capturedPawnCol = endSquare.getPosition().getCol();
            Position capturedPawnPos = new Position(capturedPawnRow, capturedPawnCol);
            // Remove the captured pawn from the board by setting its square to null.
            board.setPieceAt(null, capturedPawnPos);
            // Note: The `pieceCaptured` field within the `Move` object itself should be null for en passant,
            // as the destination square is empty.
        }

        // 2. Castling:
        // If the move is flagged as castling...
        if (move.isCastling()) {
            // Determine the row (rank) where castling occurs (0 for white, 7 for black).
            int row = startSquare.getPosition().getRow();
            // Variables to store the rook's start and end columns.
            int rookStartCol, rookEndCol;
            // Determine if it's Kingside (O-O) or Queenside (O-O-O) castling
            // based on whether the king's destination column is greater than its start column.
            boolean kingside = endSquare.getPosition().getCol() > startSquare.getPosition().getCol();

            if (kingside) { // Kingside castling
                rookStartCol = 7; // Rook starts on h-file (col 7)
                rookEndCol = 5; // Rook moves to f-file (col 5)
            } else { // Queenside castling
                rookStartCol = 0; // Rook starts on a-file (col 0)
                rookEndCol = 3; // Rook moves to d-file (col 3)
            }
            // Create Position objects for the rook's start and end squares.
            Position rookStartPos = new Position(row, rookStartCol);
            Position rookEndPos = new Position(row, rookEndCol);
            // Get the specific Rook piece object from the board.
            Piece rookToMove = board.getPieceAt(rookStartPos);

            // Physically move the rook on the board.
            board.movePiece(rookStartPos, rookEndPos);

            // --- Update Castling Rook's State ---
            // Ensure the rook involved in castling is marked as having moved.
            if (rookToMove instanceof Rook) {
                ((Rook) rookToMove).setHasMoved(true);
                // Also update the rook's internal reference to its new square.
                rookToMove.setCurrentSquare(board.getSquareAt(rookEndPos));
            }
            // The King's `hasMoved` flag will be set later in the standard update section.
        }

        // --- Standard Move Execution ---
        // This handles the primary movement of the piece from start to end square.
        // Note: The `pieceCaptured` field in the `Move` object already holds the piece
        // that was on the `endSquare` before the move (if any). `board.movePiece` handles this.

        // Perform the move on the board model. This typically involves:
        // 1. Setting the start square's piece to null.
        // 2. Setting the end square's piece to the `movedPiece`.
        board.movePiece(startSquare.getPosition(), endSquare.getPosition());
        // Update the piece's internal knowledge of its current square.
        movedPiece.setCurrentSquare(endSquare);


        // --- Handle Promotion ---
        // If the move is flagged as a promotion...
        if (move.isPromotion()) {
            // Get the type of piece the pawn is promoting to from the Move object.
            PieceType promotionType = move.getPromotionPieceType();
            // Fallback: If for some reason the promotion type wasn't specified in the Move object
            // (ideally it should be), default to promoting to a Queen.
            if (promotionType == null) {
                promotionType = PieceType.QUEEN;
                System.err.println("Warning: Executing promotion but promotion type is null in Move object, defaulting to Queen.");
            }

            // Create a new piece instance of the chosen promotion type.
            // It belongs to the same player (color) and is placed on the end square.
            Piece newPiece = switch (promotionType) {
                case ROOK -> new Rook(movedPiece.getColor(), endSquare);
                case BISHOP -> new Bishop(movedPiece.getColor(), endSquare);
                case KNIGHT -> new Knight(movedPiece.getColor(), endSquare);
                default -> new Queen(movedPiece.getColor(), endSquare); // Default to Queen
            };
            // Replace the pawn on the end square with the newly created promoted piece.
            board.setPieceAt(newPiece, endSquare.getPosition());
            // Ensure the new piece knows its current square.
            newPiece.setCurrentSquare(endSquare);

            // --- Update Promoted Rook's State ---
            // If the pawn promoted to a Rook, immediately mark it as having moved
            // (a promoted rook cannot be used for castling later).
            if (newPiece instanceof Rook) {
                ((Rook) newPiece).setHasMoved(true);
            }
        }

        // --- Update Piece State (hasMoved flag) ---
        // After any move (standard, castling, promotion), update the `hasMoved` flag
        // for the primary piece that moved, if it's a King or a Rook.
        // This is crucial for disabling future castling rights for that piece.
        if (movedPiece instanceof King) {
            ((King) movedPiece).setHasMoved(true);
        } else if (movedPiece instanceof Rook) {
            ((Rook) movedPiece).setHasMoved(true);
        }
    }

    /**
     * Switches the `currentPlayer` reference to the other player.
     */
    public void switchPlayer() {
        // Use a ternary operator for concise switching logic.
        // If the current player is players[0] (White), set currentPlayer to players[1] (Black).
        // Otherwise (if current player is Black), set currentPlayer to players[0] (White).
        currentPlayer = (currentPlayer == players[0]) ? players[1] : players[0];
    }

    /**
     * Checks if the *current* player's King is under attack (in check).
     * Delegates the check to the `MoveValidator`.
     *
     * @return true if the player whose turn it currently is is in check, false otherwise.
     */
    public boolean isCheck() {
        // Call the MoveValidator's isKingInCheck method, passing the board
        // and the color of the player whose turn it currently is.
        return moveValidator.isKingInCheck(board, currentPlayer.getColor());
    }

    /**
     * Updates the game's overall state (`gameState`) based on the situation
     * of the player whose turn it is *now*.
     * This method determines if the game has ended in checkmate or stalemate,
     * or if it continues (potentially in check).
     */
    private void updateGameState() {
        // Get the color of the player whose turn it currently is.
        ChessColor chessColorToCheck = currentPlayer.getColor();
        // Check if this player's king is currently under attack.
        boolean inCheck = moveValidator.isKingInCheck(board, chessColorToCheck);
        // Generate all legal moves available to this player in the current position.
        List<Move> legalMoves = generateAllLegalMoves(currentPlayer);

        // Check if the current player has *any* legal moves.
        if (legalMoves.isEmpty()) {
            // If there are no legal moves:
            // Check if the player is currently in check.
            if (inCheck) {
                // If in check and no legal moves -> CHECKMATE.
                // Determine the winner based on the color that was checkmated.
                gameState = (chessColorToCheck == ChessColor.WHITE) ? GameState.CHECKMATE_BLACK_WINS : GameState.CHECKMATE_WHITE_WINS;
            } else {
                // If not in check and no legal moves -> STALEMATE.
                gameState = GameState.STALEMATE;
            }
        } else {
            // If there are legal moves available:
            // The game continues. Set the state to CHECK if the player is in check,
            // otherwise set it back to ONGOING.
            gameState = inCheck ? GameState.CHECK : GameState.ONGOING;
        }

        // TODO: Implement checks for other draw conditions like:
        // - 50-move rule (no pawn move or capture in 50 moves by each player).
        // - Threefold repetition (the same position occurs three times with the same player to move).
        // - Insufficient material (e.g., King vs King, King vs King+Bishop, King vs King+Knight).
    }

    /**
     * Generates a list of all possible legal moves for the specified player
     * in the current board state.
     * This involves:
     * 1. Iterating through all pieces belonging to the player.
     * 2. Generating basic potential moves for each piece.
     * 3. For each potential move, creating a `Move` object.
     * 4. Fully validating each `Move` using `moveValidator.isMoveLegal` (which checks for self-checks).
     * 5. Handling special cases like promotion (generating multiple moves) and castling.
     *
     * @param player The player for whom to generate all legal moves.
     * @return A list containing all `Move` objects that are legal for the player to make.
     */
    public List<Move> generateAllLegalMoves(Player player) {
        // Initialize an empty list to store the legal moves found.
        List<Move> legalMoves = new ArrayList<>();
        // Get the color of the player.
        ChessColor playerChessColor = player.getColor();

        // Iterate through all squares on the board (rows 0-7).
        for (int r = 0; r < 8; r++) {
            // Iterate through all columns (0-7).
            for (int c = 0; c < 8; c++) {
                // Get the square at the current row and column.
                Square startSquare = board.getSquareAt(new Position(r, c));
                // Get the piece on that square (if any).
                Piece piece = startSquare.getPiece();

                // --- Check Piece Ownership ---
                // Only consider moves for pieces that exist and belong to the specified player.
                if (piece != null && piece.getColor() == playerChessColor) {

                    // 1. --- Generate Basic Potential Moves ---
                    // Ask the piece for its list of valid destination *positions* based on its
                    // movement rules (e.g., where a knight can jump, where a pawn can move/capture).
                    // This list might include moves illegal due to check.
                    List<Position> basicDestinations = piece.getValidMoves(board, startSquare);

                    // Iterate through each potential destination position.
                    for (Position endPos : basicDestinations) {
                        // Get the destination square object.
                        Square endSquare = board.getSquareAt(endPos);
                        // Check if there's a piece on the destination square (for potential capture).
                        Piece captured = endSquare.getPiece(); // Can be null.

                        // --- Identify Potential Special Moves (based on basic move generation) ---
                        // Check if this looks like a potential en passant capture:
                        // - Is it a Pawn?
                        // - Is it moving diagonally (column changes)?
                        // - Is the destination square empty?
                        boolean potentialEnPassant = piece.getType() == PieceType.PAWN &&
                                startSquare.getPosition().getCol() != endPos.getCol() &&
                                captured == null;

                        // --- Create Potential Move Object ---
                        // Construct a `Move` object representing this potential move.
                        Move potentialMove;
                        if (potentialEnPassant) {
                            // If it looks like en passant, create the Move object with the enPassant flag set.
                            // The captured piece is null because the target square is empty.
                            // The `moveValidator` will later confirm if it's a *legal* en passant based on history.
                            potentialMove = new Move(startSquare, endSquare, piece, null, null, false, true);
                        } else {
                            // Otherwise, create a standard move or capture Move object.
                            potentialMove = new Move(startSquare, endSquare, piece, captured);
                        }


                        // --- Full Legality Check ---
                        // Use the MoveValidator to perform the complete legality check, including:
                        // - Ensuring the move doesn't leave the player's own king in check.
                        // - Verifying special move conditions (if applicable, like en passant history).
                        if (moveValidator.isMoveLegal(potentialMove, board, player, moveHistory)) {

                            // --- Handle Promotion ---
                            // If the fully validated move is a pawn reaching the final rank...
                            boolean isPromotion = piece.getType() == PieceType.PAWN &&
                                    ((playerChessColor == ChessColor.WHITE && endPos.getRow() == 7) || (playerChessColor == ChessColor.BLACK && endPos.getRow() == 0));

                            if (isPromotion) {
                                // If it's a promotion, generate separate legal Move objects for each possible promotion piece type
                                // (Queen, Rook, Bishop, Knight). The UI will typically ask the user which one they want.
                                legalMoves.add(new Move(startSquare, endSquare, piece, captured, PieceType.QUEEN, false, false));
                                legalMoves.add(new Move(startSquare, endSquare, piece, captured, PieceType.ROOK, false, false));
                                legalMoves.add(new Move(startSquare, endSquare, piece, captured, PieceType.BISHOP, false, false));
                                legalMoves.add(new Move(startSquare, endSquare, piece, captured, PieceType.KNIGHT, false, false));
                            } else {
                                // If it's not a promotion, add the validated standard/capture/en passant move to the list.
                                legalMoves.add(potentialMove);
                            }
                        }
                        // If `moveValidator.isMoveLegal` returned false, the `potentialMove` is discarded.
                    } // End loop through basic destinations

                    // 2. --- Separately Generate and Validate Castling Moves ---
                    // Castling moves (King moving two squares) are often not generated by the King's basic `getValidMoves`.
                    // We need to check for them specifically here.
                    if (piece.getType() == PieceType.KING) {
                        // Check Kingside Castling (O-O) possibility.
                        // Check basic bounds first (ensure target column c+2 is on the board).
                        if (c + 2 < 8) {
                            // Define the King's target position for kingside castling (g1 or g8).
                            Position kingsideTargetPos = new Position(r, c + 2);
                            Square kingsideEndSquare = board.getSquareAt(kingsideTargetPos);
                            // Create a Move object representing the castling attempt.
                            Move kingsideCastle = new Move(startSquare, kingsideEndSquare, piece, null, null, true, false);
                            // Perform the full legality check for this specific castling move.
                            // The `isCastlingAttempt` check here is slightly redundant if `isMoveLegal` handles it, but safe.
                            if (moveValidator.isCastlingAttempt(kingsideCastle) &&
                                    moveValidator.isMoveLegal(kingsideCastle, board, player, moveHistory)) {
                                // If legal, add the castling move to the list.
                                legalMoves.add(kingsideCastle);
                            }
                        }

                        // Check Queenside Castling (O-O-O) possibility.
                        // Check basic bounds first (ensure target column c-2 is on the board).
                        if (c - 2 >= 0) {
                            // Define the King's target position for queenside castling (c1 or c8).
                            Position queensideTargetPos = new Position(r, c - 2);
                            Square queensideEndSquare = board.getSquareAt(queensideTargetPos);
                            // Create a Move object representing the castling attempt.
                            Move queensideCastle = new Move(startSquare, queensideEndSquare, piece, null, null, true, false);
                            // Perform the full legality check for this specific castling move.
                            if (moveValidator.isCastlingAttempt(queensideCastle) &&
                                    moveValidator.isMoveLegal(queensideCastle, board, player, moveHistory)) {
                                // If legal, add the castling move to the list.
                                legalMoves.add(queensideCastle);
                            }
                        }
                    } // End King-specific castling check
                } // End check for piece ownership
            } // End column loop
        } // End row loop

        // Return the complete list of fully validated legal moves.
        return legalMoves;
    }

    // Removed createPromotionMove helper as logic is now integrated into generateAllLegalMoves.

    /**
     * Checks if the game has ended in checkmate.
     *
     * @return true if the current `gameState` indicates checkmate for either player, false otherwise.
     */
    public boolean isCheckmate() {
        // Check if the gameState is one of the checkmate states.
        return gameState == GameState.CHECKMATE_WHITE_WINS || gameState == GameState.CHECKMATE_BLACK_WINS;
    }

    /**
     * Checks if the game has ended in stalemate.
     *
     * @return true if the current `gameState` is STALEMATE, false otherwise.
     */
    public boolean isStalemate() {
        // Check if the gameState is STALEMATE.
        return gameState == GameState.STALEMATE;
    }

    // --- Getters ---
    // Provide public access to certain parts of the game state.

    /**
     * Gets the current board object.
     * @return The Board instance.
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Gets the player whose turn it is currently.
     * @return The current Player object.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Gets the current state of the game (ONGOING, CHECK, etc.).
     * @return The current GameState enum value.
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Gets the array containing both players.
     * @return The array of Player objects.
     */
    public Player[] getPlayers() {
        return players;
    }

    /**
     * Gets the history of moves made in the game.
     * Consider returning an unmodifiable list if you want to prevent external modification.
     *
     * @return A List of Move objects representing the game history.
     */
    public List<Move> getMoveHistory() {
        // Currently returns the mutable list.
        // To return an unmodifiable view: return java.util.Collections.unmodifiableList(moveHistory);
        return moveHistory;
    }
}