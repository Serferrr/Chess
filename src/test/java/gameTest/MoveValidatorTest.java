package gameTest;

import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.enums.PieceType;
import model.game.Move;
import model.game.MoveValidator;
import model.game.Player;
import model.pieces.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach; // Import AfterEach

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveValidatorTest {

    private MoveValidator validator;
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private List<Move> moveHistory;
    private PrintStream originalErr; // To restore System.err

    // --- Helper Methods for Setup ---

    private Square getSq(int r, int c) {
        return board.getSquareAt(new Position(r, c));
    }

    private Piece placePiece(PieceType type, ChessColor chessColor, int r, int c) {
        Square square = getSq(r, c);
        Piece piece = switch (type) {
            case PAWN -> new Pawn(chessColor, square);
            case ROOK -> new Rook(chessColor, square);
            case KNIGHT -> new Knight(chessColor, square);
            case BISHOP -> new Bishop(chessColor, square);
            case QUEEN -> new Queen(chessColor, square);
            case KING -> new King(chessColor, square);
        };
        square.setPiece(piece);
        return piece;
    }

    // Updated createMove helper for standard moves or captures
    private Move createMove(Piece piece, Square start, Square end) {
        // Determine captured piece *before* creating the immutable Move
        Piece captured = end.getPiece();
        // Use the appropriate constructor based on whether it's a capture
        return new Move(start, end, piece, captured);
    }

    // Updated createMove helper using positions
    private Move createMove(int startR, int startC, int endR, int endC) {
        Square start = getSq(startR, startC);
        Square end = getSq(endR, endC);
        Piece piece = start.getPiece();
        if (piece == null) {
            throw new IllegalArgumentException("No piece at start square (" + startR + "," + startC + ") for move creation");
        }
        // Determine captured piece *before* creating the immutable Move
        Piece captured = end.getPiece();
        return new Move(start, end, piece, captured);
    }

    // Helper specifically for creating castling moves for tests
    private Move createCastlingMove(Piece king, Square start, Square end) {
        return new Move(start, end, king, null, null, true, false);
    }

    // Helper specifically for creating en passant moves for tests
    private Move createEnPassantMove(Piece pawn, Square start, Square end) {
        // En passant captures land on an empty square, so capturedPiece is null
        return new Move(start, end, pawn, null, null, false, true);
    }

    // --- Test Setup ---

    @BeforeEach
    void setUp() {
        originalErr = System.err; // Store original System.err
        validator = new MoveValidator();
        board = new Board();
        whitePlayer = new Player(ChessColor.WHITE);
        blackPlayer = new Player(ChessColor.BLACK);
        moveHistory = new ArrayList<>();
        // Clear board explicitly
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                getSq(r, c).setPiece(null);
            }
        }
    }

    @AfterEach
    void tearDown() {
        System.setErr(originalErr); // Restore System.err after each test
    }


    // --- Basic Validation Tests ---

    @Test
    @DisplayName("Test moving opponent's piece")
    void testMoveOpponentPiece() {
        placePiece(PieceType.PAWN, ChessColor.BLACK, 6, 0); // Black pawn
        Move move = createMove(6, 0, 5, 0);
        assertFalse(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Should not be legal for White to move Black's pawn");
    }

    @Test
    @DisplayName("Test invalid basic pawn move (3 squares)")
    void testInvalidPawnMove() {
        placePiece(PieceType.PAWN, ChessColor.WHITE, 1, 0); // White pawn at a2
        placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // Need kings for check validation
        placePiece(PieceType.KING, ChessColor.BLACK, 7, 4);
        Move move = createMove(1, 0, 4, 0); // Invalid 3-square move to a5
        assertFalse(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Pawn cannot move 3 squares forward");
    }

    @Test
    @DisplayName("Test valid basic knight move")
    void testValidKnightMove() {
        placePiece(PieceType.KNIGHT, ChessColor.WHITE, 0, 1); // White knight at b1
        placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // Need kings
        placePiece(PieceType.KING, ChessColor.BLACK, 7, 4);
        Move move = createMove(0, 1, 2, 2); // Move to c3
        assertTrue(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Knight move b1-c3 should be legal on empty board");
    }

    @Test
    @DisplayName("Test invalid basic knight move")
    void testInvalidKnightMove() {
        placePiece(PieceType.KNIGHT, ChessColor.WHITE, 0, 1); // White knight at b1
        placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // Need kings
        placePiece(PieceType.KING, ChessColor.BLACK, 7, 4);
        Move move = createMove(0, 1, 1, 1); // Invalid move to b2
        assertFalse(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Knight move b1-b2 should be illegal");
    }

    @Test
    @DisplayName("Test null move object")
    void testNullMove() {
        // Suppress expected error message for this specific test
        System.setErr(new PrintStream(OutputStream.nullOutputStream()));
        assertFalse(validator.isMoveLegal(null, board, whitePlayer, moveHistory),
                "Null move should be illegal");
        // System.err is restored in tearDown()
    }

    // --- Check Validation Tests ---

    @Test
    @DisplayName("Test moving a pinned piece illegally")
    void testMovePinnedPieceIllegally() {
        placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        Piece whiteRook = placePiece(PieceType.ROOK, ChessColor.WHITE, 1, 4); // e2
        placePiece(PieceType.ROOK, ChessColor.BLACK, 7, 4); // e8

        Move move = createMove(whiteRook, getSq(1, 4), getSq(1, 5)); // Re2 to f2

        assertFalse(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Moving a pinned rook off the pin line should be illegal");
    }

    @Test
    @DisplayName("Test moving a pinned piece legally along the pin")
    void testMovePinnedPieceLegally() {
        placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        Piece whiteRook = placePiece(PieceType.ROOK, ChessColor.WHITE, 1, 4); // e2
        placePiece(PieceType.ROOK, ChessColor.BLACK, 7, 4); // e8

        Move move = createMove(whiteRook, getSq(1, 4), getSq(2, 4)); // Re2 to e3
        assertTrue(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Moving a pinned rook along the pin line should be legal if it doesn't expose king");

        Move captureMove = createMove(whiteRook, getSq(1, 4), getSq(7, 4)); // Re2xe8
        assertTrue(validator.isMoveLegal(captureMove, board, whitePlayer, moveHistory),
                "Capturing the pinning piece with the pinned piece should be legal");
    }


    @Test
    @DisplayName("Test blocking a check")
    void testBlockCheck() {
        placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        placePiece(PieceType.ROOK, ChessColor.BLACK, 7, 4); // e8 (checking the king)
        Piece whiteBishop = placePiece(PieceType.BISHOP, ChessColor.WHITE, 3, 2); // c4

        assertTrue(validator.isKingInCheck(board, ChessColor.WHITE), "King should initially be in check");

        Move move = createMove(whiteBishop, getSq(3, 2), getSq(1, 4)); // Bc4 to e2

        assertTrue(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Moving the bishop to block the check should be legal");
    }

    @Test
    @DisplayName("Test moving king out of check")
    void testMoveKingOutOfCheck() {
        Piece king = placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        placePiece(PieceType.ROOK, ChessColor.BLACK, 7, 4); // e8 (checking the king)

        assertTrue(validator.isKingInCheck(board, ChessColor.WHITE), "King should initially be in check");

        Move move = createMove(king, getSq(0, 4), getSq(0, 5)); // Ke1 to f1

        assertTrue(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Moving the king out of check should be legal");
    }

    @Test
    @DisplayName("Test moving king into check")
    void testMoveKingIntoCheck() {
        Piece king = placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        placePiece(PieceType.ROOK, ChessColor.BLACK, 7, 5); // f8 (controls f1)

        assertFalse(validator.isKingInCheck(board, ChessColor.WHITE), "King should not initially be in check");

        Move move = createMove(king, getSq(0, 4), getSq(0, 5)); // Ke1 to f1

        assertFalse(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Moving the king into check should be illegal");
    }

    @Test
    @DisplayName("Test capturing checking piece")
    void testCaptureCheckingPiece() {
        placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        Piece blackRook = placePiece(PieceType.ROOK, ChessColor.BLACK, 7, 4); // e8
        Piece whiteKnight = placePiece(PieceType.KNIGHT, ChessColor.WHITE, 5, 3); // d6

        assertTrue(validator.isKingInCheck(board, ChessColor.WHITE), "King should initially be in check");

        Move move = createMove(whiteKnight, getSq(5, 3), getSq(7, 4)); // Nd6xe8

        assertTrue(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Capturing the checking piece should be legal");
    }


    // --- Castling Tests ---

    @Test
    @DisplayName("Test legal white kingside castling")
    void testLegalWhiteKingsideCastling() {
        Piece king = placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        Piece rook = placePiece(PieceType.ROOK, ChessColor.WHITE, 0, 7); // h1
        placePiece(PieceType.KING, ChessColor.BLACK, 7, 4); // Need opponent king

        Move move = createCastlingMove(king, getSq(0, 4), getSq(0, 6)); // O-O

        assertTrue(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "White kingside castling should be legal");
    }

    @Test
    @DisplayName("Test legal black queenside castling")
    void testLegalBlackQueensideCastling() {
        Piece king = placePiece(PieceType.KING, ChessColor.BLACK, 7, 4); // e8
        Piece rook = placePiece(PieceType.ROOK, ChessColor.BLACK, 7, 0); // a8
        placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // Need opponent king

        Move move = createCastlingMove(king, getSq(7, 4), getSq(7, 2)); // O-O-O

        assertTrue(validator.isMoveLegal(move, board, blackPlayer, moveHistory),
                "Black queenside castling should be legal");
    }

    @Test
    @DisplayName("Test illegal castling - path blocked")
    void testIllegalCastlingPathBlocked() {
        Piece king = placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        Piece rook = placePiece(PieceType.ROOK, ChessColor.WHITE, 0, 7); // h1
        placePiece(PieceType.BISHOP, ChessColor.WHITE, 0, 5); // f1 (blocking)
        placePiece(PieceType.KING, ChessColor.BLACK, 7, 4); // Need opponent king

        Move move = createCastlingMove(king, getSq(0, 4), getSq(0, 6)); // O-O

        assertFalse(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Castling with a blocked path should be illegal");
    }

    @Test
    @DisplayName("Test illegal castling - king passes through attack")
    void testIllegalCastlingPassesThroughAttack() {
        Piece king = placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        Piece rook = placePiece(PieceType.ROOK, ChessColor.WHITE, 0, 7); // h1
        placePiece(PieceType.ROOK, ChessColor.BLACK, 7, 5); // f8 (attacking f1)
        placePiece(PieceType.KING, ChessColor.BLACK, 7, 4); // Need opponent king

        Move move = createCastlingMove(king, getSq(0, 4), getSq(0, 6)); // O-O

        assertFalse(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Castling through an attacked square (f1) should be illegal");
    }

    @Test
    @DisplayName("Test illegal castling - king lands on attacked square")
    void testIllegalCastlingLandsOnAttack() {
        Piece king = placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        Piece rook = placePiece(PieceType.ROOK, ChessColor.WHITE, 0, 7); // h1
        placePiece(PieceType.ROOK, ChessColor.BLACK, 7, 6); // g8 (attacking g1)
        placePiece(PieceType.KING, ChessColor.BLACK, 7, 4); // Need opponent king

        Move move = createCastlingMove(king, getSq(0, 4), getSq(0, 6)); // O-O

        assertFalse(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Castling onto an attacked square (g1) should be illegal");
    }

    @Test
    @DisplayName("Test illegal castling - king in check")
    void testIllegalCastlingKingInCheck() {
        Piece king = placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        Piece rook = placePiece(PieceType.ROOK, ChessColor.WHITE, 0, 7); // h1
        placePiece(PieceType.ROOK, ChessColor.BLACK, 7, 4); // e8 (checking king)
        placePiece(PieceType.KING, ChessColor.BLACK, 7, 7); // Need opponent king

        Move move = createCastlingMove(king, getSq(0, 4), getSq(0, 6)); // O-O

        assertFalse(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Castling while in check should be illegal");
    }

    @Test
    @DisplayName("Test illegal castling - king has moved")
    void testIllegalCastlingKingHasMoved() {
        Piece king = placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        Piece rook = placePiece(PieceType.ROOK, ChessColor.WHITE, 0, 7); // h1
        placePiece(PieceType.KING, ChessColor.BLACK, 7, 4); // Need opponent king

        ((King) king).setHasMoved(true); // Mark king as moved

        Move move = createCastlingMove(king, getSq(0, 4), getSq(0, 6)); // O-O

        assertFalse(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Castling should be illegal if the king has moved");
    }

    @Test
    @DisplayName("Test illegal castling - rook has moved")
    void testIllegalCastlingRookHasMoved() {
        Piece king = placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        Piece rook = placePiece(PieceType.ROOK, ChessColor.WHITE, 0, 7); // h1
        placePiece(PieceType.KING, ChessColor.BLACK, 7, 4); // Need opponent king

        ((Rook) rook).setHasMoved(true); // Mark rook as moved

        Move move = createCastlingMove(king, getSq(0, 4), getSq(0, 6)); // O-O

        assertFalse(validator.isMoveLegal(move, board, whitePlayer, moveHistory),
                "Castling should be illegal if the rook has moved");
    }


    // --- En Passant Tests ---

    @Test
    @DisplayName("Test legal white en passant")
    void testLegalWhiteEnPassant() {
        placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // White King at e1
        placePiece(PieceType.KING, ChessColor.BLACK, 7, 4); // Black King at e8
        Piece whitePawn = placePiece(PieceType.PAWN, ChessColor.WHITE, 4, 4); // e5
        Piece blackPawn = placePiece(PieceType.PAWN, ChessColor.BLACK, 6, 3); // d7

        // Simulate black's last move: d7-d5
        Move lastMove = createMove(blackPawn, getSq(6, 3), getSq(4, 3)); // d7-d5
        getSq(4, 3).setPiece(blackPawn);
        getSq(6, 3).setPiece(null);
        blackPawn.setCurrentSquare(getSq(4, 3));
        moveHistory.add(lastMove);

        // White attempts en passant: exd6
        Move enPassantMove = createEnPassantMove(whitePawn, getSq(4, 4), getSq(5, 3)); // e5xd6

        assertTrue(validator.isMoveLegal(enPassantMove, board, whitePlayer, moveHistory),
                "Legal white en passant capture should be allowed");
    }

    @Test
    @DisplayName("Test legal black en passant")
    void testLegalBlackEnPassant() {
        placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // White King at e1
        placePiece(PieceType.KING, ChessColor.BLACK, 7, 4); // Black King at e8
        Piece blackPawn = placePiece(PieceType.PAWN, ChessColor.BLACK, 3, 4); // e4
        Piece whitePawn = placePiece(PieceType.PAWN, ChessColor.WHITE, 1, 5); // f2

        // Simulate white's last move: f2-f4
        Move lastMove = createMove(whitePawn, getSq(1, 5), getSq(3, 5)); // f2-f4
        getSq(3, 5).setPiece(whitePawn);
        getSq(1, 5).setPiece(null);
        whitePawn.setCurrentSquare(getSq(3, 5));
        moveHistory.add(lastMove);

        // Black attempts en passant: exf3
        Move enPassantMove = createEnPassantMove(blackPawn, getSq(3, 4), getSq(2, 5)); // e4xf3

        assertTrue(validator.isMoveLegal(enPassantMove, board, blackPlayer, moveHistory),
                "Legal black en passant capture should be allowed");
    }

    @Test
    @DisplayName("Test illegal en passant - not immediately after two-square move")
    void testIllegalEnPassantNotImmediate() {
        placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // Need kings
        placePiece(PieceType.KING, ChessColor.BLACK, 7, 4);
        Piece whitePawn = placePiece(PieceType.PAWN, ChessColor.WHITE, 4, 4); // e5
        Piece blackPawn = placePiece(PieceType.PAWN, ChessColor.BLACK, 6, 3); // d7
        Piece whiteKnight = placePiece(PieceType.KNIGHT, ChessColor.WHITE, 0, 1); // b1

        // Simulate black's move: d7-d5
        Move blackPawnMove = createMove(blackPawn, getSq(6, 3), getSq(4, 3));
        getSq(4, 3).setPiece(blackPawn);
        getSq(6, 3).setPiece(null);
        blackPawn.setCurrentSquare(getSq(4, 3));
        moveHistory.add(blackPawnMove);

        // Simulate white's intermediate move: Nb1-c3
        Move whiteKnightMove = createMove(whiteKnight, getSq(0, 1), getSq(2, 2));
        getSq(2, 2).setPiece(whiteKnight);
        getSq(0, 1).setPiece(null);
        whiteKnight.setCurrentSquare(getSq(2, 2));
        moveHistory.add(whiteKnightMove);

        // White attempts delayed en passant: exd6
        Move enPassantMove = createEnPassantMove(whitePawn, getSq(4, 4), getSq(5, 3)); // e5xd6

        assertFalse(validator.isMoveLegal(enPassantMove, board, whitePlayer, moveHistory),
                "En passant must be immediate, should be illegal after intermediate move");
    }

    @Test
    @DisplayName("Test illegal en passant - last move was not two squares")
    void testIllegalEnPassantLastMoveNotTwoSquares() {
        placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // Need kings
        placePiece(PieceType.KING, ChessColor.BLACK, 7, 4);
        Piece whitePawn = placePiece(PieceType.PAWN, ChessColor.WHITE, 4, 4); // e5
        Piece blackPawn = placePiece(PieceType.PAWN, ChessColor.BLACK, 5, 3); // d6

        // Simulate black's last move: d6-d5
        Move lastMove = createMove(blackPawn, getSq(5, 3), getSq(4, 3)); // d6-d5 (one square)
        getSq(4, 3).setPiece(blackPawn);
        getSq(5, 3).setPiece(null);
        blackPawn.setCurrentSquare(getSq(4, 3));
        moveHistory.add(lastMove);

        // White attempts en passant: exd6
        Move enPassantMove = createEnPassantMove(whitePawn, getSq(4, 4), getSq(5, 3)); // e5xd6

        assertFalse(validator.isMoveLegal(enPassantMove, board, whitePlayer, moveHistory),
                "En passant requires the last move to be a two-square pawn advance");
    }

    @Test
    @DisplayName("Test illegal en passant - leaves king in check")
    void testIllegalEnPassantLeavesKingInCheck() {
        placePiece(PieceType.KING, ChessColor.WHITE, 4, 7);   // Kh5
        Piece whitePawn = placePiece(PieceType.PAWN, ChessColor.WHITE, 4, 6);   // Pg5
        Piece blackPawn = placePiece(PieceType.PAWN, ChessColor.BLACK, 6, 5);   // Bf7
        placePiece(PieceType.ROOK, ChessColor.BLACK, 7, 7);   // Rh8 (pins white pawn to king)
        placePiece(PieceType.KING, ChessColor.BLACK, 0, 0); // Need opponent king

        // Simulate black's last move: f7-f5
        Move lastMove = createMove(blackPawn, getSq(6, 5), getSq(4, 5)); // f7-f5
        getSq(4, 5).setPiece(blackPawn);
        getSq(6, 5).setPiece(null);
        blackPawn.setCurrentSquare(getSq(4, 5));
        moveHistory.add(lastMove);

        // White attempts en passant: gxf6 e.p.
        Move enPassantMove = createEnPassantMove(whitePawn, getSq(4, 6), getSq(5, 5)); // g5xf6

        assertFalse(validator.isMoveLegal(enPassantMove, board, whitePlayer, moveHistory),
                "En passant capture that exposes the king to check should be illegal");
    }


    // --- Helper Method Tests (Example) ---

    @Test
    @DisplayName("Test isSquareAttacked by Rook")
    void testIsSquareAttackedByRook() {
        placePiece(PieceType.ROOK, ChessColor.BLACK, 7, 4); // Black Rook at e8
        Position target = new Position(0, 4); // e1
        assertTrue(validator.isSquareAttacked(target, board, ChessColor.BLACK),
                "e1 should be attacked by Black Rook on e8");
        Position safeTarget = new Position(0, 5); // f1
        assertFalse(validator.isSquareAttacked(safeTarget, board, ChessColor.BLACK),
                "f1 should not be attacked by Black Rook on e8");
    }

    @Test
    @DisplayName("Test isSquareAttacked by Pawn")
    void testIsSquareAttackedByPawn() {
        placePiece(PieceType.PAWN, ChessColor.WHITE, 1, 3); // White Pawn at d2
        Position target = new Position(2, 2); // c3
        assertTrue(validator.isSquareAttacked(target, board, ChessColor.WHITE),
                "c3 should be attacked by White Pawn on d2");
        Position target2 = new Position(2, 4); // e3
        assertTrue(validator.isSquareAttacked(target2, board, ChessColor.WHITE),
                "e3 should be attacked by White Pawn on d2");
        Position forwardTarget = new Position(2, 3); // d3
        assertFalse(validator.isSquareAttacked(forwardTarget, board, ChessColor.WHITE),
                "d3 (directly forward) should not be 'attacked' by White Pawn on d2 in this context");
    }

    @Test
    @DisplayName("Test isKingInCheck positive")
    void testIsKingInCheckPositive() {
        placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        placePiece(PieceType.ROOK, ChessColor.BLACK, 7, 4); // e8
        assertTrue(validator.isKingInCheck(board, ChessColor.WHITE),
                "King at e1 should be in check from Rook at e8");
    }

    @Test
    @DisplayName("Test isKingInCheck negative")
    void testIsKingInCheckNegative() {
        placePiece(PieceType.KING, ChessColor.WHITE, 0, 4); // e1
        placePiece(PieceType.ROOK, ChessColor.BLACK, 7, 5); // f8
        assertFalse(validator.isKingInCheck(board, ChessColor.WHITE),
                "King at e1 should not be in check from Rook at f8");
    }
}