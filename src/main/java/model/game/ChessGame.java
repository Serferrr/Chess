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
 */
public class ChessGame {
    private final Board board;
    private final Player[] players = new Player[2]; // Make final if players don't change
    private Player currentPlayer;
    private GameState gameState;
    private final List<Move> moveHistory = new ArrayList<>(); // Use final if list reference doesn't change
    private final MoveValidator moveValidator = new MoveValidator();

    public ChessGame() {
        board = new Board();
        players[0] = new Player(ChessColor.WHITE);
        players[1] = new Player(ChessColor.BLACK);
        // Consider calling startGame here or leaving it explicit
        startGame(); // Let's start the game by default in the constructor
    }

    /**
     * Sets up the board and initializes game state for a new game.
     */
    public void startGame() {
        board.clearBoard();
        board.setupBoard(); // Assumes this places pieces in starting positions
        currentPlayer = players[0]; // White always starts
        gameState = GameState.ONGOING;
        moveHistory.clear(); // Clear history for a new game
        // Reset hasMoved flags for all Kings and Rooks
        resetPieceMovedStatus();
    }

    /**
     * Resets the hasMoved flag for all Kings and Rooks on the board.
     * Should be called at the start of a new game.
     */
    private void resetPieceMovedStatus() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board.getPieceAt(new Position(r, c));
                if (piece instanceof King) {
                    ((King) piece).setHasMoved(false);
                } else if (piece instanceof Rook) {
                    ((Rook) piece).setHasMoved(false);
                }
            }
        }
    }


    /**
     * Validates if a move is legal in the current game context using the MoveValidator.
     * @param move The move to validate.
     * @return true if the move is legal, false otherwise.
     */
    public boolean validateMove(Move move) {
        // Delegate validation to the MoveValidator instance
        // Ensure the move object itself is valid before passing it on
        if (move == null || move.getPieceMoved() == null || move.getStartSquare() == null || move.getEndSquare() == null) {
            System.err.println("Validation Error: Attempted to validate an incomplete Move object.");
            return false;
        }
        return moveValidator.isMoveLegal(move, board, currentPlayer, moveHistory);
    }

    /**
     * Attempts to make a move. Validates the move, executes it, updates state,
     * records history, and switches the player.
     * <p>
     * This method expects a fully constructed Move object, potentially including
     * promotion type if applicable.
     *
     * @param move The move to attempt (should include promotion type if it's a promotion).
     * @return true if the move was successful, false otherwise.
     */
    public boolean makeMove(Move move) {
        // Use the validator to check full legality
        if (!validateMove(move)) {
            System.err.println("Invalid move attempted: " + move); // Use System.err for errors
            return false;
        }

        // If legal, execute the move on the board
        // The executeMove method now relies on the properties already set in the Move object
        executeMove(move);

        // Record the move *before* switching player
        moveHistory.add(move);

        // Switch player
        switchPlayer();

        // Update the game state (check for checkmate, stalemate, etc. for the *new* current player)
        updateGameState();

        return true;
    }


    /**
     * Executes the move on the board, handling captures and special move side effects.
     * Assumes the move has already been validated as legal and the Move object
     * contains all necessary information (like promotion type, castling/en passant flags).
     * @param move The validated legal move to execute.
     */
    private void executeMove(Move move) {
        Square startSquare = move.getStartSquare();
        Square endSquare = move.getEndSquare();
        Piece movedPiece = move.getPieceMoved();

        // --- Handle Special Move Side Effects ---
        // 1. En Passant Capture (remove the *actual* captured pawn)
        if (move.isEnPassant()) { // Rely on the flag set during move creation/validation
            int capturedPawnRow = startSquare.getPosition().getRow();
            int capturedPawnCol = endSquare.getPosition().getCol();
            // The captured pawn is adjacent horizontally to the start square, on the same rank
            Position capturedPawnPos = new Position(capturedPawnRow, capturedPawnCol);
            board.setPieceAt(null, capturedPawnPos); // Remove the pawn
            // Note: pieceCaptured in the Move object should be null for en passant
        }

        // 2. Castling (move the rook)
        if (move.isCastling()) { // Rely on the flag set during move creation/validation
            int row = startSquare.getPosition().getRow();
            int rookStartCol, rookEndCol;
            boolean kingside = endSquare.getPosition().getCol() > startSquare.getPosition().getCol();

            if (kingside) { // Kingside
                rookStartCol = 7;
                rookEndCol = 5; // Rook moves to f1/f8
            } else { // Queenside
                rookStartCol = 0;
                rookEndCol = 3; // Rook moves to d1/d8
            }
            Position rookStartPos = new Position(row, rookStartCol);
            Position rookEndPos = new Position(row, rookEndCol);
            Piece rookToMove = board.getPieceAt(rookStartPos); // Get the specific rook piece

            // Move the rook on the board
            board.movePiece(rookStartPos, rookEndPos);

            // --- Set hasMoved flag for the castling Rook ---
            if (rookToMove instanceof Rook) {
                ((Rook) rookToMove).setHasMoved(true);
                // Update the rook's internal square reference after moving it
                rookToMove.setCurrentSquare(board.getSquareAt(rookEndPos));
            }
            // No need to call move.setCastling(true); it's final
        }

        // --- Standard Move Execution ---
        // The potentially captured piece is already stored in the final move.pieceCaptured field
        // Perform the move on the board (this handles overwriting the destination square)
        board.movePiece(startSquare.getPosition(), endSquare.getPosition());
        // Update the moved piece's internal square reference
        movedPiece.setCurrentSquare(endSquare);


        // --- Handle Promotion ---
        if (move.isPromotion()) { // Rely on the flag/promotionPieceType in the Move object
            PieceType promotionType = move.getPromotionPieceType();
            // Defaulting should ideally happen when the Move is created if needed,
            // but we can keep a fallback here.
            if (promotionType == null) {
                promotionType = PieceType.QUEEN;
                System.err.println("Warning: Executing promotion but promotion type is null in Move object, defaulting to Queen.");
            }

            Piece newPiece = switch (promotionType) {
                case ROOK -> new Rook(movedPiece.getColor(), endSquare);
                case BISHOP -> new Bishop(movedPiece.getColor(), endSquare);
                case KNIGHT -> new Knight(movedPiece.getColor(), endSquare);
                default -> new Queen(movedPiece.getColor(), endSquare); // Default to Queen
            };
            board.setPieceAt(newPiece, endSquare.getPosition()); // Replace pawn with new piece
            newPiece.setCurrentSquare(endSquare); // Ensure new piece knows its square

            // If promoting to Rook, set its hasMoved flag immediately
            if (newPiece instanceof Rook) {
                ((Rook) newPiece).setHasMoved(true);
            }
        }

        // --- Update Piece State (hasMoved flag) ---
        // Set flag for the primary moved piece (King or Rook) after it has moved
        if (movedPiece instanceof King) {
            ((King) movedPiece).setHasMoved(true);
        } else if (movedPiece instanceof Rook) {
            ((Rook) movedPiece).setHasMoved(true);
        }
    }

    /**
     * Switches the current player.
     */
    public void switchPlayer() {
        currentPlayer = (currentPlayer == players[0]) ? players[1] : players[0];
    }

    /**
     * Checks if the current player's King is under attack.
     * @return true if the current player is in check, false otherwise.
     */
    public boolean isCheck() {
        // Delegate to validator, checking the *current* player's king
        return moveValidator.isKingInCheck(board, currentPlayer.getColor());
    }

    /**
     * Updates the game state (ONGOING, CHECK, CHECKMATE, STALEMATE)
     * based on the current player's situation (the player whose turn it is NOW).
     */
    private void updateGameState() {
        // Check the status for the player whose turn it now is
        ChessColor chessColorToCheck = currentPlayer.getColor();
        boolean inCheck = moveValidator.isKingInCheck(board, chessColorToCheck);
        List<Move> legalMoves = generateAllLegalMoves(currentPlayer);

        if (legalMoves.isEmpty()) {
            if (inCheck) {
                // Checkmate
                gameState = (chessColorToCheck == ChessColor.WHITE) ? GameState.CHECKMATE_BLACK_WINS : GameState.CHECKMATE_WHITE_WINS;
            } else {
                // Stalemate
                gameState = GameState.STALEMATE;
            }
        } else {
            // Game continues
            gameState = inCheck ? GameState.CHECK : GameState.ONGOING;
        }

        // TODO: Add checks for other draw conditions (50-move rule, repetition, insufficient material)
    }

    /**
     * Generates all legal moves for the specified player in the current board state.
     * This involves generating basic moves and then validating them fully.
     * @param player The player for whom to generate moves.
     * @return A list of all legal moves.
     */
    public List<Move> generateAllLegalMoves(Player player) {
        List<Move> legalMoves = new ArrayList<>();
        ChessColor playerChessColor = player.getColor();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Square startSquare = board.getSquareAt(new Position(r, c));
                Piece piece = startSquare.getPiece();

                // Only consider pieces belonging to the player
                if (piece != null && piece.getColor() == playerChessColor) {
                    // 1. Generate basic valid moves (including captures)
                    List<Position> basicDestinations = piece.getValidMoves(board, startSquare);

                    for (Position endPos : basicDestinations) {
                        Square endSquare = board.getSquareAt(endPos);
                        Piece captured = endSquare.getPiece(); // Can be null

                        // Determine if it's a potential en passant capture based on piece logic
                        // (Pawn moving diagonally to an empty square)
                        boolean potentialEnPassant = piece.getType() == PieceType.PAWN &&
                                startSquare.getPosition().getCol() != endPos.getCol() &&
                                captured == null;

                        // Create a potential Move object using appropriate constructor
                        Move potentialMove;
                        if (potentialEnPassant) {
                            // Create move assuming it *might* be en passant, validator will confirm
                            potentialMove = new Move(startSquare, endSquare, piece, null, null, false, true);
                        } else {
                            // Standard move or capture
                            potentialMove = new Move(startSquare, endSquare, piece, captured);
                        }


                        // Use the validator to check if it's fully legal (doesn't leave king in check)
                        if (moveValidator.isMoveLegal(potentialMove, board, player, moveHistory)) {
                            // Check for promotion possibilities
                            boolean isPromotion = piece.getType() == PieceType.PAWN &&
                                    ((playerChessColor == ChessColor.WHITE && endPos.getRow() == 7) || (playerChessColor == ChessColor.BLACK && endPos.getRow() == 0));

                            if (isPromotion) {
                                // Add moves for each possible promotion using the full constructor
                                legalMoves.add(new Move(startSquare, endSquare, piece, captured, PieceType.QUEEN, false, false));
                                legalMoves.add(new Move(startSquare, endSquare, piece, captured, PieceType.ROOK, false, false));
                                legalMoves.add(new Move(startSquare, endSquare, piece, captured, PieceType.BISHOP, false, false));
                                legalMoves.add(new Move(startSquare, endSquare, piece, captured, PieceType.KNIGHT, false, false));
                            } else {
                                // Add the validated standard/capture/en passant move
                                legalMoves.add(potentialMove);
                            }
                        }
                    }

                    // 2. Separately generate and validate potential Castling moves for the King
                    if (piece.getType() == PieceType.KING) {
                        // Check Kingside Castling (O-O)
                        if (c + 2 < 8) { // Basic bounds check
                            Position kingsideTargetPos = new Position(r, c + 2);
                            Square kingsideEndSquare = board.getSquareAt(kingsideTargetPos);
                            // Create move assuming it *might* be castling
                            Move kingsideCastle = new Move(startSquare, kingsideEndSquare, piece, null, null, true, false);
                            if (moveValidator.isCastlingAttempt(kingsideCastle) && // Redundant check, but safe
                                    moveValidator.isMoveLegal(kingsideCastle, board, player, moveHistory)) {
                                legalMoves.add(kingsideCastle);
                            }
                        }

                        // Check Queenside Castling (O-O-O)
                        if (c - 2 >= 0) { // Basic bounds check
                            Position queensideTargetPos = new Position(r, c - 2);
                            Square queensideEndSquare = board.getSquareAt(queensideTargetPos);
                            // Create move assuming it *might* be castling
                            Move queensideCastle = new Move(startSquare, queensideEndSquare, piece, null, null, true, false);
                            if (moveValidator.isCastlingAttempt(queensideCastle) && // Redundant check, but safe
                                    moveValidator.isMoveLegal(queensideCastle, board, player, moveHistory)) {
                                legalMoves.add(queensideCastle);
                            }
                        }
                    }
                }
            }
        }
        return legalMoves;
    }

    // Removed createPromotionMove helper as logic is now in generateAllLegalMoves

    /**
     * Checks if the game has ended in checkmate.
     * @return true if the game state is checkmate, false otherwise.
     */
    public boolean isCheckmate() {
        return gameState == GameState.CHECKMATE_WHITE_WINS || gameState == GameState.CHECKMATE_BLACK_WINS;
    }

    /**
     * Checks if the game has ended in stalemate.
     * @return true if the game state is stalemate, false otherwise.
     */
    public boolean isStalemate() {
        return gameState == GameState.STALEMATE;
    }

    // --- Getters ---

    public Board getBoard() {
        return board;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Player[] getPlayers() {
        return players;
    }

    // Optional: Method to get move history
    public List<Move> getMoveHistory() {
        // Return an unmodifiable list to prevent external modification
        return moveHistory;
    }
}