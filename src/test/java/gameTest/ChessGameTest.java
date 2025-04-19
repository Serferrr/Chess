package gameTest;

import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.enums.GameState;
import model.enums.PieceType;
import model.game.ChessGame;
import model.game.Move;
import model.game.Player;
import model.pieces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChessGameTest {

    private ChessGame game;
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;

    @BeforeEach
    void setUp() {
        game = new ChessGame(); // Creates and starts the game
        board = game.getBoard();
        whitePlayer = game.getPlayers()[0];
        blackPlayer = game.getPlayers()[1];
    }

    @Test
    @DisplayName("Game Initialization Test")
    void testGameInitialization() {
        assertNotNull(board, "Board should be initialized.");
        assertEquals(ChessColor.WHITE, whitePlayer.getColor(), "Player 1 should be White.");
        assertEquals(ChessColor.BLACK, blackPlayer.getColor(), "Player 2 should be Black.");
        assertEquals(whitePlayer, game.getCurrentPlayer(), "Current player should be White at the start.");
        assertEquals(GameState.ONGOING, game.getGameState(), "Game state should be ONGOING at the start.");
        assertTrue(game.getMoveHistory().isEmpty(), "Move history should be empty at the start.");

        // Check a few key piece placements
        assertInstanceOf(Rook.class, board.getPieceAt(new Position(0, 0)), "White Rook should be at a1.");
        assertEquals(ChessColor.WHITE, board.getPieceAt(new Position(0, 0)).getColor(), "Piece at a1 should be White.");
        assertInstanceOf(Pawn.class, board.getPieceAt(new Position(1, 3)), "White Pawn should be at d2.");
        assertEquals(ChessColor.WHITE, board.getPieceAt(new Position(1, 3)).getColor(), "Piece at d2 should be White.");
        assertInstanceOf(King.class, board.getPieceAt(new Position(7, 4)), "Black King should be at e8.");
        assertEquals(ChessColor.BLACK, board.getPieceAt(new Position(7, 4)).getColor(), "Piece at e8 should be Black.");

        // Check hasMoved flags are reset (spot check King and Rook)
        Piece whiteKing = board.getPieceAt(new Position(0, 4));
        Piece whiteRookA1 = board.getPieceAt(new Position(0, 0));
        Piece blackKing = board.getPieceAt(new Position(7, 4));
        Piece blackRookH8 = board.getPieceAt(new Position(7, 7));

        assertInstanceOf(King.class, whiteKing);
        assertInstanceOf(Rook.class, whiteRookA1);
        assertInstanceOf(King.class, blackKing);
        assertInstanceOf(Rook.class, blackRookH8);

        assertFalse(((King) whiteKing).hasMoved(), "White King should not have moved initially.");
        assertFalse(((Rook) whiteRookA1).hasMoved(), "White Rook a1 should not have moved initially.");
        assertFalse(((King) blackKing).hasMoved(), "Black King should not have moved initially.");
        assertFalse(((Rook) blackRookH8).hasMoved(), "Black Rook h8 should not have moved initially.");
    }

    @Test
    @DisplayName("Start Game Resets State")
    void testStartGameResetsState() {
        // Make a move first
        makeSimpleMove(new Position(1, 4), new Position(3, 4)); // White e2-e4
        assertEquals(1, game.getMoveHistory().size());
        assertEquals(blackPlayer, game.getCurrentPlayer());

        // Start a new game
        game.startGame();

        // Verify state is reset
        assertEquals(whitePlayer, game.getCurrentPlayer(), "Current player should reset to White.");
        assertEquals(GameState.ONGOING, game.getGameState(), "Game state should reset to ONGOING.");
        assertTrue(game.getMoveHistory().isEmpty(), "Move history should be cleared.");
        // Re-check a piece position to ensure board is reset
        assertInstanceOf(Pawn.class, board.getPieceAt(new Position(1, 4)), "White Pawn should be back at e2.");
        assertNull(board.getPieceAt(new Position(3, 4)), "e4 should be empty after reset.");
        // Re-check hasMoved flag
        Piece whiteKing = board.getPieceAt(new Position(0, 4));
        assertInstanceOf(King.class, whiteKing);
        assertFalse(((King) whiteKing).hasMoved(), "White King hasMoved should be false after reset.");
    }


    @Test
    @DisplayName("Switch Player Test")
    void testSwitchPlayer() {
        assertEquals(whitePlayer, game.getCurrentPlayer());
        game.switchPlayer();
        assertEquals(blackPlayer, game.getCurrentPlayer());
        game.switchPlayer();
        assertEquals(whitePlayer, game.getCurrentPlayer());
    }

    @Test
    @DisplayName("Make Valid Standard Move")
    void testMakeValidStandardMove() {
        Position startPos = new Position(1, 4); // e2
        Position endPos = new Position(3, 4);   // e4
        Square startSquare = board.getSquareAt(startPos);
        Square endSquare = board.getSquareAt(endPos);
        Piece pawn = startSquare.getPiece();

        Move move = new Move(startSquare, endSquare, pawn, null); // Standard pawn move

        assertTrue(game.makeMove(move), "Valid move should return true.");
        assertEquals(blackPlayer, game.getCurrentPlayer(), "Player should switch to Black.");
        assertEquals(1, game.getMoveHistory().size(), "Move history should contain one move.");
        assertEquals(move, game.getMoveHistory().getFirst(), "Move history should contain the correct move.");
        assertNull(board.getPieceAt(startPos), "Start square should be empty.");
        assertEquals(pawn, board.getPieceAt(endPos), "Piece should be at the end square.");
        assertEquals(endSquare, pawn.getCurrentSquare(), "Piece's internal square reference should be updated.");
        assertEquals(GameState.ONGOING, game.getGameState(), "Game state should remain ONGOING.");
    }

    @Test
    @DisplayName("Make Valid Capture Move")
    void testMakeValidCaptureMove() {
        // Setup: White Pawn e4, Black Pawn d5
        makeSimpleMove(new Position(1, 4), new Position(3, 4)); // White e2-e4
        makeSimpleMove(new Position(6, 3), new Position(4, 3)); // Black d7-d5

        // White captures d5 with e4 pawn
        Position startPos = new Position(3, 4); // e4
        Position endPos = new Position(4, 3);   // d5 (capture)
        Square startSquare = board.getSquareAt(startPos);
        Square endSquare = board.getSquareAt(endPos);
        Piece whitePawn = startSquare.getPiece();
        Piece blackPawn = endSquare.getPiece(); // The piece to be captured

        assertNotNull(whitePawn);
        assertNotNull(blackPawn);
        assertEquals(ChessColor.WHITE, whitePawn.getColor());
        assertEquals(ChessColor.BLACK, blackPawn.getColor());

        Move captureMove = new Move(startSquare, endSquare, whitePawn, blackPawn);

        assertTrue(game.makeMove(captureMove), "Valid capture move should return true.");
        assertEquals(blackPlayer, game.getCurrentPlayer(), "Player should switch back to Black.");
        assertEquals(3, game.getMoveHistory().size(), "Move history should contain three moves.");
        assertEquals(captureMove, game.getMoveHistory().get(2), "Move history should contain the capture move.");
        assertNull(board.getPieceAt(startPos), "Start square (e4) should be empty.");
        assertEquals(whitePawn, board.getPieceAt(endPos), "White pawn should be at the end square (d5).");
        assertEquals(endSquare, whitePawn.getCurrentSquare(), "Piece's internal square reference should be updated.");
        assertEquals(GameState.ONGOING, game.getGameState(), "Game state should remain ONGOING.");
    }


    @Test
    @DisplayName("Make Invalid Move - Wrong Turn")
    void testMakeInvalidMoveWrongTurn() {
        Position startPos = new Position(6, 4); // Black e7
        Position endPos = new Position(4, 4);   // Black e5 (attempted on White's turn)
        Square startSquare = board.getSquareAt(startPos);
        Square endSquare = board.getSquareAt(endPos);
        Piece blackPawn = startSquare.getPiece();

        Move move = new Move(startSquare, endSquare, blackPawn, null);

        assertFalse(game.validateMove(move), "Move validation should fail for wrong turn.");
        assertFalse(game.makeMove(move), "Making the invalid move should return false.");
        assertEquals(whitePlayer, game.getCurrentPlayer(), "Player should remain White.");
        assertTrue(game.getMoveHistory().isEmpty(), "Move history should remain empty.");
        assertEquals(blackPawn, board.getPieceAt(startPos), "Black pawn should not have moved.");
        assertNull(board.getPieceAt(endPos), "End square should remain empty.");
    }

    @Test
    @DisplayName("Make Invalid Move - Illegal Piece Movement")
    void testMakeInvalidMoveIllegalMovement() {
        Position startPos = new Position(0, 1); // White Knight b1
        Position endPos = new Position(1, 1);   // b2 (illegal for knight)
        Square startSquare = board.getSquareAt(startPos);
        Square endSquare = board.getSquareAt(endPos);
        Piece knight = startSquare.getPiece();

        Move move = new Move(startSquare, endSquare, knight, null);

        assertFalse(game.validateMove(move), "Move validation should fail for illegal movement.");
        assertFalse(game.makeMove(move), "Making the invalid move should return false.");
        assertEquals(whitePlayer, game.getCurrentPlayer(), "Player should remain White.");
        assertTrue(game.getMoveHistory().isEmpty(), "Move history should remain empty.");
        assertEquals(knight, board.getPieceAt(startPos), "Knight should not have moved.");
        assertInstanceOf(Pawn.class, board.getPieceAt(endPos), "End square should still contain pawn."); // Pawn at b2
    }

    @Test
    @DisplayName("Make Move Resulting in Check")
    void testMakeMoveResultingInCheck() {
        // Setup: A sequence that leads to check
        // 1. e4
        makeSimpleMove(new Position(1, 4), new Position(3, 4)); // White e2-e4
        // 2. e5
        makeSimpleMove(new Position(6, 4), new Position(4, 4)); // Black e7-e5
        // 3. Qh5 - This move attacks the Black King along the h5-e8 diagonal (blocked by e5 pawn)
        //          and attacks the e5 pawn directly. It does NOT check yet.
        // Let's try Bc4 instead for a direct attack setup on f7
        // 3. Bc4
        makeSimpleMove(new Position(0, 5), new Position(3, 2)); // White Bf1-c4

        // Now it's Black's turn. Let's make a move that allows White to check.
        // 4. Nf6 (A common move, but blocks the checkmate threat on f7 for now)
        makeSimpleMove(new Position(7, 6), new Position(5, 5)); // Black Ng8-f6

        // Now White can play Qe2, checking the king along the e-file if the e-pawn moves.
        // Or White plays Qf3, setting up attack on f7.
        // Let's try a different check: White moves Queen to check directly.
        // Reset and try: 1.e4 2.f5?? 3.Qh5+
        game.startGame(); // Reset to initial state

        // 1. e4
        makeSimpleMove(new Position(1, 4), new Position(3, 4)); // White e2-e4
        // 2. f5?? (A weak move exposing the king)
        makeSimpleMove(new Position(6, 5), new Position(4, 5)); // Black f7-f5

        // 3. Qh5+ (Checks the Black King on e8)
        Position startPos = new Position(0, 3); // White Qd1
        Position endPos = new Position(4, 7);   // White Qh5
        Square startSquare = board.getSquareAt(startPos);
        Square endSquare = board.getSquareAt(endPos);
        Piece whiteQueen = startSquare.getPiece();
        Move checkMove = new Move(startSquare, endSquare, whiteQueen, null);

        // Make the checking move
        assertTrue(game.makeMove(checkMove), "Checking move Qh5+ should be valid.");

        // Assertions after the check
        assertEquals(blackPlayer, game.getCurrentPlayer(), "Player should switch to Black.");
        assertEquals(GameState.CHECK, game.getGameState(), "Game state should be CHECK after Qh5+.");
        assertTrue(game.isCheck(), "isCheck should return true when Black is in check."); // isCheck checks the CURRENT player
    }

    @Test
    @DisplayName("Make Move Resulting in Checkmate (Fool's Mate)")
    void testMakeMoveResultingInCheckmate() {
        // Fool's Mate sequence
        makeSimpleMove(new Position(1, 5), new Position(2, 5)); // White f2-f3
        makeSimpleMove(new Position(6, 4), new Position(4, 4)); // Black e7-e5
        makeSimpleMove(new Position(1, 6), new Position(3, 6)); // White g2-g4
        // Black Queen delivers checkmate
        Position startPos = new Position(7, 3); // Black Qd8
        Position endPos = new Position(3, 7);   // Black Qh4#
        Square startSquare = board.getSquareAt(startPos);
        Square endSquare = board.getSquareAt(endPos);
        Piece blackQueen = startSquare.getPiece();
        Move checkmateMove = new Move(startSquare, endSquare, blackQueen, null);

        assertTrue(game.makeMove(checkmateMove), "Checkmate move should be valid.");
        assertEquals(whitePlayer, game.getCurrentPlayer(), "Current player should switch to White (but game is over).");
        assertEquals(GameState.CHECKMATE_BLACK_WINS, game.getGameState(), "Game state should be CHECKMATE_BLACK_WINS.");
        assertTrue(game.isCheckmate(), "isCheckmate should return true.");
        assertFalse(game.isStalemate(), "isStalemate should return false.");

        // Verify no legal moves for White
        List<Move> whiteLegalMoves = game.generateAllLegalMoves(whitePlayer);
        assertTrue(whiteLegalMoves.isEmpty(), "White should have no legal moves in checkmate.");
    }

    @Test
    @DisplayName("Make Move Resulting in Stalemate")
    void testMakeMoveResultingInStalemate() {
        // Setup a specific stalemate position
        board.clearBoard(); // Clear board first

        // Position BEFORE stalemating move:
        // k . . . . . . .  (Black King a8)
        // . . . . . . . .
        // . . K . . . . .  (White King c6)
        // . . . . . . . .
        // . . . . . . . .
        // . . Q . . . . .  (White Queen c2) <- Start position for Queen
        // . . . . . . . .
        // . . . . . . . .

        // Place pieces
        King blackKing = new King(ChessColor.BLACK, board.getSquareAt(new Position(0, 7))); // h1
        King whiteKing = new King(ChessColor.WHITE, board.getSquareAt(new Position(7, 0))); // a8
        Queen whiteQueen = new Queen(ChessColor.WHITE, board.getSquareAt(new Position(1, 4))); // e2

        board.setPieceAt(blackKing, new Position(0, 7)); // h1
        board.setPieceAt(whiteKing, new Position(7, 0)); // a8
        board.setPieceAt(whiteQueen, new Position(1, 4)); // e2

        // Ensure it's White's turn (it should be by default after setup)
        assertEquals(whitePlayer, game.getCurrentPlayer(), "Setup Check: Should be White's turn.");
        game.getMoveHistory().clear(); // Clear history from potential setup moves if any

        // --- Define the stalemating move: White moves Queen c2 to c7 ---
        Position startPos = new Position(1, 4); // e2
        Position endPos = new Position(1, 5);   // f2
        Square startSquare = board.getSquareAt(startPos);
        Square endSquare = board.getSquareAt(endPos);
        Piece queen = startSquare.getPiece();
        // Ensure the queen exists before creating the move
        assertNotNull(queen, "Queen should be present at the start position for the stalemating move.");
        Move stalemateMove = new Move(startSquare, endSquare, queen, null);

        // --- Perform the move and assert ---
        assertTrue(game.makeMove(stalemateMove), "Stalemating move Qf2 should be valid.");
        assertEquals(blackPlayer, game.getCurrentPlayer(), "Current player should switch to Black.");

        // *** The Key Assertion ***
        assertEquals(GameState.STALEMATE, game.getGameState(), "Game state should be STALEMATE.");

        // Other related assertions
        assertFalse(game.isCheckmate(), "isCheckmate should return false.");
        assertTrue(game.isStalemate(), "isStalemate should return true.");
        // Double-check the king is NOT in check for stalemate
        assertFalse(game.isCheck(), "Black king should not be in check in stalemate.");

        // Verify no legal moves for Black
        List<Move> blackLegalMoves = game.generateAllLegalMoves(blackPlayer);
        assertTrue(blackLegalMoves.isEmpty(), "Black should have no legal moves in stalemate.");
    }

    @Test
    @DisplayName("Validate Move - Incomplete Move Object")
    void testValidateMoveIncomplete() {
        assertThrows(NullPointerException.class,
                () -> {new Move(null, null, null, null);},
                "Incomplete move object should throw NullPointerException.");
    }

    @Test
    @DisplayName("Generate All Legal Moves - Starting Position")
    void testGenerateAllLegalMovesStart() {
        List<Move> whiteMoves = game.generateAllLegalMoves(whitePlayer);
        // 8 pawns * 2 moves each = 16
        // 2 knights * 2 moves each = 4
        // Total = 20
        assertEquals(20, whiteMoves.size(), "White should have 20 legal moves at the start.");

        // Make one move and check Black's moves
        makeSimpleMove(new Position(1, 4), new Position(3, 4)); // White e2-e4
        List<Move> blackMoves = game.generateAllLegalMoves(blackPlayer);
        assertEquals(20, blackMoves.size(), "Black should have 20 legal moves after White's e4.");
    }

    @Test
    @DisplayName("Make Kingside Castling Move")
    void testMakeKingsideCastling() {
        // Setup: Clear path for White Kingside castling
        board.setPieceAt(null, new Position(0, 5)); // Remove Bishop f1
        board.setPieceAt(null, new Position(0, 6)); // Remove Knight g1

        Position kingStartPos = new Position(0, 4); // e1
        Position kingEndPos = new Position(0, 6);   // g1
        Square kingStartSquare = board.getSquareAt(kingStartPos);
        Square kingEndSquare = board.getSquareAt(kingEndPos);
        Piece king = kingStartSquare.getPiece();
        assertInstanceOf(King.class, king);
        assertFalse(((King) king).hasMoved());

        Position rookStartPos = new Position(0, 7); // h1
        Piece rook = board.getPieceAt(rookStartPos);
        assertInstanceOf(Rook.class, rook);
        assertFalse(((Rook) rook).hasMoved());

        // Create the castling move (King moves two squares)
        Move castleMove = new Move(kingStartSquare, kingEndSquare, king, null, null, true, false);

        assertTrue(game.validateMove(castleMove), "Kingside castling move should be valid.");
        assertTrue(game.makeMove(castleMove), "Making kingside castling move should succeed.");

        // Verify positions
        assertNull(board.getPieceAt(kingStartPos), "King start square (e1) should be empty.");
        assertNull(board.getPieceAt(rookStartPos), "Rook start square (h1) should be empty.");
        assertEquals(king, board.getPieceAt(kingEndPos), "King should be at g1.");
        assertEquals(rook, board.getPieceAt(new Position(0, 5)), "Rook should be at f1."); // Rook's final position

        // Verify hasMoved flags
        assertTrue(((King) king).hasMoved(), "King hasMoved flag should be true after castling.");
        assertTrue(((Rook) rook).hasMoved(), "Rook hasMoved flag should be true after castling.");

        assertEquals(blackPlayer, game.getCurrentPlayer(), "Player should switch to Black.");
        assertEquals(1, game.getMoveHistory().size(), "Move history should contain one move.");
        assertTrue(game.getMoveHistory().getFirst().isCastling(), "Move in history should be marked as castling.");
    }

    @Test
    @DisplayName("Make Queenside Castling Move")
    void testMakeQueensideCastling() {
        // Setup: Clear path for White Queenside castling
        board.setPieceAt(null, new Position(0, 1)); // Remove Knight b1
        board.setPieceAt(null, new Position(0, 2)); // Remove Bishop c1
        board.setPieceAt(null, new Position(0, 3)); // Remove Queen d1

        Position kingStartPos = new Position(0, 4); // e1
        Position kingEndPos = new Position(0, 2);   // c1
        Square kingStartSquare = board.getSquareAt(kingStartPos);
        Square kingEndSquare = board.getSquareAt(kingEndPos);
        Piece king = kingStartSquare.getPiece();
        assertInstanceOf(King.class, king);
        assertFalse(((King) king).hasMoved());

        Position rookStartPos = new Position(0, 0); // a1
        Piece rook = board.getPieceAt(rookStartPos);
        assertInstanceOf(Rook.class, rook);
        assertFalse(((Rook) rook).hasMoved());

        // Create the castling move (King moves two squares)
        Move castleMove = new Move(kingStartSquare, kingEndSquare, king, null, null, true, false);

        assertTrue(game.validateMove(castleMove), "Queenside castling move should be valid.");
        assertTrue(game.makeMove(castleMove), "Making queenside castling move should succeed.");

        // Verify positions
        assertNull(board.getPieceAt(kingStartPos), "King start square (e1) should be empty.");
        assertNull(board.getPieceAt(rookStartPos), "Rook start square (a1) should be empty.");
        assertEquals(king, board.getPieceAt(kingEndPos), "King should be at c1.");
        assertEquals(rook, board.getPieceAt(new Position(0, 3)), "Rook should be at d1."); // Rook's final position

        // Verify hasMoved flags
        assertTrue(((King) king).hasMoved(), "King hasMoved flag should be true after castling.");
        assertTrue(((Rook) rook).hasMoved(), "Rook hasMoved flag should be true after castling.");

        assertEquals(blackPlayer, game.getCurrentPlayer(), "Player should switch to Black.");
        assertEquals(1, game.getMoveHistory().size(), "Move history should contain one move.");
        assertTrue(game.getMoveHistory().getFirst().isCastling(), "Move in history should be marked as castling.");
    }

    @Test
    @DisplayName("Make En Passant Move")
    void testMakeEnPassantMove() {
        // Setup for en passant:
        // 1. White pawn to e4
        makeSimpleMove(new Position(1, 4), new Position(3, 4)); // e2-e4
        // 2. Black dummy move (e.g., h6)
        makeSimpleMove(new Position(6, 7), new Position(5, 7)); // h7-h6
        // 3. White pawn to e5
        makeSimpleMove(new Position(3, 4), new Position(4, 4)); // e4-e5
        // 4. Black pawn moves d7-d5 (next to white pawn)
        makeSimpleMove(new Position(6, 3), new Position(4, 3)); // d7-d5

        // Now White e5 pawn can capture Black d5 pawn en passant by moving to d6
        Position whitePawnStartPos = new Position(4, 4); // e5
        Position whitePawnEndPos = new Position(5, 3);   // d6 (en passant target)
        Position blackPawnActualPos = new Position(4, 3); // d5 (pawn to be captured)

        Square startSquare = board.getSquareAt(whitePawnStartPos);
        Square endSquare = board.getSquareAt(whitePawnEndPos); // Target square is empty
        Piece whitePawn = startSquare.getPiece();
        Piece blackPawn = board.getPieceAt(blackPawnActualPos); // The pawn that just moved two squares

        assertNotNull(whitePawn);
        assertNotNull(blackPawn);
        assertNull(endSquare.getPiece(), "En passant target square (d6) should be empty.");

        // Create the en passant move (captured piece is null, enPassant flag is true)
        Move enPassantMove = new Move(startSquare, endSquare, whitePawn, null, null, false, true);

        assertTrue(game.validateMove(enPassantMove), "En passant move should be valid.");
        assertTrue(game.makeMove(enPassantMove), "Making en passant move should succeed.");

        // Verify positions
        assertNull(board.getPieceAt(whitePawnStartPos), "White pawn start square (e5) should be empty.");
        assertNull(board.getPieceAt(blackPawnActualPos), "Captured black pawn square (d5) should be empty.");
        assertEquals(whitePawn, board.getPieceAt(whitePawnEndPos), "White pawn should be at the en passant target square (d6).");

        assertEquals(blackPlayer, game.getCurrentPlayer(), "Player should switch to Black.");
        assertEquals(5, game.getMoveHistory().size(), "Move history should contain five moves.");
        Move lastMove = game.getMoveHistory().get(4);
        assertTrue(lastMove.isEnPassant(), "Move in history should be marked as en passant.");
        assertNull(lastMove.getPieceCaptured(), "Captured piece in en passant move object should be null.");
    }

    @Test
    @DisplayName("Make Promotion Move")
    void testMakePromotionMove() {
        // Setup: White pawn reaches the 8th rank
        board.clearBoard();
        Pawn whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(new Position(6, 0))); // a7
        King whiteKing = new King(ChessColor.WHITE, board.getSquareAt(new Position(0, 4))); // e1
        King blackKing = new King(ChessColor.BLACK, board.getSquareAt(new Position(7, 4))); // e8
        board.setPieceAt(whitePawn, new Position(6, 0));
        board.setPieceAt(whiteKing, new Position(0, 4));
        board.setPieceAt(blackKing, new Position(7, 4));

        // Set current player to White
        // (Assuming default start is White, if not, set it explicitly)
        if (game.getCurrentPlayer().getColor() != ChessColor.WHITE) {
            game.switchPlayer();
        }

        Position startPos = new Position(6, 0); // a7
        Position endPos = new Position(7, 0);   // a8
        Square startSquare = board.getSquareAt(startPos);
        Square endSquare = board.getSquareAt(endPos);

        // Create the promotion move (promoting to Queen)
        Move promotionMove = new Move(startSquare, endSquare, whitePawn, null, PieceType.QUEEN, false, false);

        assertTrue(game.validateMove(promotionMove), "Promotion move should be valid.");
        assertTrue(game.makeMove(promotionMove), "Making promotion move should succeed.");

        // Verify positions and piece type
        assertNull(board.getPieceAt(startPos), "Pawn start square (a7) should be empty.");
        Piece promotedPiece = board.getPieceAt(endPos);
        assertNotNull(promotedPiece, "End square (a8) should have a piece.");
        assertEquals(PieceType.QUEEN, promotedPiece.getType(), "Piece on a8 should be a Queen.");
        assertEquals(ChessColor.WHITE, promotedPiece.getColor(), "Promoted piece should be White.");
        assertEquals(endSquare, promotedPiece.getCurrentSquare(), "Promoted piece's square reference should be updated.");

        assertEquals(blackPlayer, game.getCurrentPlayer(), "Player should switch to Black.");
        assertEquals(1, game.getMoveHistory().size(), "Move history should contain one move.");
        Move lastMove = game.getMoveHistory().getFirst();
        assertTrue(lastMove.isPromotion(), "Move in history should be marked as promotion.");
        assertEquals(PieceType.QUEEN, lastMove.getPromotionPieceType(), "Promotion type in move history should be Queen.");
    }

    // Helper method to simplify making moves in tests where validation isn't the focus
    private void makeSimpleMove(Position startPos, Position endPos) {
        Square startSquare = board.getSquareAt(startPos);
        Square endSquare = board.getSquareAt(endPos);
        Piece piece = startSquare.getPiece();
        Piece captured = endSquare.getPiece();
        if (piece == null) return; // Cannot move from empty square

        // Basic move object - assumes not special moves for this helper
        Move move = new Move(startSquare, endSquare, piece, captured);
        game.makeMove(move);
    }
}