package piecesTest;

import model.board.Board;
import model.board.Position;
import model.enums.ChessColor;
import model.pieces.Pawn;
import model.pieces.Piece; // Assuming a generic Piece or another type for testing captures
import model.pieces.Rook;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PawnTest {

    private Board board;
    private Pawn whitePawn;
    private Pawn blackPawn;

    // Helper to create a position - assumes 0-based indexing
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    private Position pos(int row, int col) {
        return new Position(row, col);
    }

    // Helper to place a piece on the board
    private void placePiece(Piece piece) {
        // Assuming Board has a method like this, adjust if necessary
        board.setPieceAt(piece, piece.getCurrentSquare().getPosition());
        // Also ensure the piece's internal square reference is updated if needed
        piece.setCurrentSquare(board.getSquareAt(piece.getCurrentSquare().getPosition()));
    }

    // Helper to check list contents (order doesn't matter)
    private void assertMovesMatch(List<Position> actualMoves, Position... expectedPositions) {
        assertEquals(expectedPositions.length, actualMoves.size(),
                "Number of moves mismatch. Expected: " + List.of(expectedPositions) + ", Actual: " + actualMoves);
        for (Position expected : expectedPositions) {
            assertTrue(actualMoves.contains(expected),
                    "Expected move " + expected + " not found in actual moves: " + actualMoves);
        }
         for (Position actual : actualMoves) {
             boolean found = false;
             for (Position expected : expectedPositions) {
                 if (actual.equals(expected)) {
                     found = true;
                     break;
                 }
             }
             assertTrue(found, "Unexpected move found: " + actual + " in actual moves: " + actualMoves);
         }
    }


    @BeforeEach
    void setUp() {
        board = new Board(); // Assumes default constructor creates an 8x8 empty board
    }

    // --- Tests for canPromote ---

    @Test
    void testWhitePawnCanPromoteOnRank8() {
        whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(6, 3))); // On rank before promotion
        assertTrue(whitePawn.canPromote(pos(7, 3)), "White pawn should be able to promote on rank 8 ");
    }

    @Test
    void testWhitePawnCannotPromoteBelowRank8() {
        whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(5, 3)));
        assertFalse(whitePawn.canPromote(pos(6, 3)), "White pawn should not be able to promote below rank 7");
    }

    @Test
    void testBlackPawnCanPromoteOnRank1() {
        blackPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(1, 4))); // On rank before promotion
        assertTrue(blackPawn.canPromote(pos(0, 4)), "Black pawn should be able to promote on rank 1");
    }

    @Test
    void testBlackPawnCannotPromoteAboveRank1() {
        blackPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(1, 4)));
        assertFalse(blackPawn.canPromote(pos(2, 4)), "Black pawn should not be able to promote above rank 1");
    }


    // --- Tests for getValidMoves (White Pawn) ---

    @Test
    void testWhitePawnInitialMoves() {
        whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(1, 4))); // Standard E2 start
        placePiece(whitePawn);
        List<Position> moves = whitePawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(2, 4), // Single move E3
                pos(3, 4)  // Double move E4
        );
    }

    @Test
    void testWhitePawnSingleMoveOnly() {
        whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(2, 4))); // Moved once to E3
        placePiece(whitePawn);
        List<Position> moves = whitePawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(3, 4) // Single move E4
        );
    }

    @Test
    void testWhitePawnBlockedSingleMove() {
        whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(1, 4))); // E2
        Pawn blockingPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(2, 4))); // E3
        placePiece(whitePawn);
        placePiece(blockingPawn);
        List<Position> moves = whitePawn.getValidMoves(board);

        assertTrue(moves.isEmpty(), "Pawn should have no moves when blocked directly.");
    }

    @Test
    void testWhitePawnBlockedDoubleMove() {
        whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(1, 4))); // E2
        Pawn blockingPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(3, 4))); // E4
        placePiece(whitePawn);
        placePiece(blockingPawn);
        List<Position> moves = whitePawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(2, 4) // Can only move one square forward
        );
    }

    @Test
    void testWhitePawnCaptureMoves() {
        whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(3, 4))); // E4
        Pawn captureTargetLeft = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(4, 3))); // D5
        Pawn captureTargetRight = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(4, 5))); // F5
        placePiece(whitePawn);
        placePiece(captureTargetLeft);
        placePiece(captureTargetRight);
        List<Position> moves = whitePawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(4, 4), // Forward move E5
                pos(4, 3), // Capture D5
                pos(4, 5)  // Capture F5
        );
    }

    @Test
    void testWhitePawnCaptureBlockedByFriendly() {
        whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(3, 4))); // E4
        Pawn friendlyPiece = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(4, 3))); // D5
        placePiece(whitePawn);
        placePiece(friendlyPiece);
        List<Position> moves = whitePawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(4, 4) // Forward move E5 only
        );
    }

    @Test
    void testWhitePawnPotentialEnPassant() {
        // Setup: White pawn on E5 (row 4), Black pawn adjacent on D5 (row 4)
        whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(4, 4))); // E5
        Pawn adjacentBlackPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(4, 3))); // D5
        placePiece(whitePawn);
        placePiece(adjacentBlackPawn);

        List<Position> moves = whitePawn.getValidMoves(board);

        // Expect forward move E6 and potential en passant capture on D6
        assertMovesMatch(moves,
                pos(5, 4), // Forward move E6
                pos(5, 3)  // Potential en passant capture target D6
        );
    }

    @Test
    void testWhitePawnNoEnPassantIfNotOnCorrectRank() {
        // Setup: White pawn on E4 (row 3), Black pawn adjacent on D4 (row 3)
        whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(3, 4))); // E4
        Pawn adjacentBlackPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(3, 3))); // D4
        placePiece(whitePawn);
        placePiece(adjacentBlackPawn);

        List<Position> moves = whitePawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(4, 4) // Forward move E5
        );
        assertFalse(moves.contains(pos(4, 3)), "Should not contain potential en passant target D5");
    }

    @Test
    void testWhitePawnNoEnPassantIfAdjacentPieceNotPawn() {
        whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(4, 4))); // E5
        // Use a different piece type, e.g., Rook - Need a concrete Piece or mock
        // For simplicity, let's assume Piece can be instantiated directly for testing if not abstract
        // If Piece is abstract, you'd need a concrete dummy like Rook or use a mocking framework
        Piece adjacentBlackPiece = new Rook(ChessColor.BLACK, board.getSquareAt(pos(4, 3))); // D5
        placePiece(whitePawn);
        placePiece(adjacentBlackPiece);

        List<Position> moves = whitePawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(5, 4) // Forward move E6 only
        );
        assertFalse(moves.contains(pos(5, 3)), "Should not contain potential en passant target D6");
    }

    @Test
    void testWhitePawnNoEnPassantIfTargetSquareOccupied() {
        whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(4, 4))); // E5
        Pawn adjacentBlackPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(4, 3))); // D5
        Pawn blockingPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(5, 3))); // D6 (target square)
        placePiece(whitePawn);
        placePiece(adjacentBlackPawn);
        placePiece(blockingPawn);

        List<Position> moves = whitePawn.getValidMoves(board);

        // Expect forward move E6 and standard capture on D6
        assertMovesMatch(moves,
                pos(5, 4), // Forward move E6
                pos(5, 3)  // Standard capture D6 (not en passant)
        );
    }


    // --- Tests for getValidMoves (Black Pawn) ---

    @Test
    void testBlackPawnInitialMoves() {
        blackPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(6, 4))); // Standard E7 start
        placePiece(blackPawn);
        List<Position> moves = blackPawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(5, 4), // Single move E6
                pos(4, 4)  // Double move E5
        );
    }

    @Test
    void testBlackPawnSingleMoveOnly() {
        blackPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(5, 4))); // Moved once to E6
        placePiece(blackPawn);
        List<Position> moves = blackPawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(4, 4) // Single move E5
        );
    }

    @Test
    void testBlackPawnBlockedSingleMove() {
        blackPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(6, 4))); // E7
        Pawn blockingPawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(5, 4))); // E6
        placePiece(blackPawn);
        placePiece(blockingPawn);
        List<Position> moves = blackPawn.getValidMoves(board);

        assertTrue(moves.isEmpty(), "Pawn should have no moves when blocked directly.");
    }

    @Test
    void testBlackPawnBlockedDoubleMove() {
        blackPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(6, 4))); // E7
        Pawn blockingPawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(4, 4))); // E5
        placePiece(blackPawn);
        placePiece(blockingPawn);
        List<Position> moves = blackPawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(5, 4) // Can only move one square forward
        );
    }

    @Test
    void testBlackPawnCaptureMoves() {
        blackPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(4, 4))); // E5
        Pawn captureTargetLeft = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(3, 3))); // D4
        Pawn captureTargetRight = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(3, 5))); // F4
        placePiece(blackPawn);
        placePiece(captureTargetLeft);
        placePiece(captureTargetRight);
        List<Position> moves = blackPawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(3, 4), // Forward move E4
                pos(3, 3), // Capture D4
                pos(3, 5)  // Capture F4
        );
    }

    @Test
    void testBlackPawnPotentialEnPassant() {
        // Setup: Black pawn on E4 (row 3), White pawn adjacent on D4 (row 3)
        blackPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(3, 4))); // E4
        Pawn adjacentWhitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(3, 3))); // D4
        placePiece(blackPawn);
        placePiece(adjacentWhitePawn);

        List<Position> moves = blackPawn.getValidMoves(board);

        // Expect forward move E3 and potential en passant capture on D3
        assertMovesMatch(moves,
                pos(2, 4), // Forward move E3
                pos(2, 3)  // Potential en passant capture target D3
        );
    }

    @Test
    void testBlackPawnNoEnPassantIfNotOnCorrectRank() {
        // Setup: Black pawn on E5 (row 4), White pawn adjacent on D5 (row 4)
        blackPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(4, 4))); // E5
        Pawn adjacentWhitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(4, 3))); // D5
        placePiece(blackPawn);
        placePiece(adjacentWhitePawn);

        List<Position> moves = blackPawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(3, 4) // Forward move E4
        );
        assertFalse(moves.contains(pos(3, 3)), "Should not contain potential en passant target D3");
    }

    // --- Edge Cases ---

    @Test
    void testWhitePawnOnEdgeNoWrapAroundCapture() {
        whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(3, 0))); // A4
        Pawn captureTarget = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(4, 1))); // B5
        placePiece(whitePawn);
        placePiece(captureTarget);
        List<Position> moves = whitePawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(4, 0), // Forward A5
                pos(4, 1)  // Capture B5
        );
    }

    @Test
    void testBlackPawnOnEdgeNoWrapAroundCapture() {
        blackPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(4, 7))); // H5
        Pawn captureTarget = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(3, 6))); // G4
        placePiece(blackPawn);
        placePiece(captureTarget);
        List<Position> moves = blackPawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(3, 7), // Forward H4
                pos(3, 6)  // Capture G4
        );
    }

    @Test
    void testWhitePawnNearPromotion() {
        whitePawn = new Pawn(ChessColor.WHITE, board.getSquareAt(pos(6, 2))); // C7
        placePiece(whitePawn);
        List<Position> moves = whitePawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(7, 2) // Forward C8 (promotion move)
        );
    }

    @Test
    void testBlackPawnNearPromotion() {
        blackPawn = new Pawn(ChessColor.BLACK, board.getSquareAt(pos(1, 5))); // F2
        placePiece(blackPawn);
        List<Position> moves = blackPawn.getValidMoves(board);

        assertMovesMatch(moves,
                pos(0, 5) // Forward F1 (promotion move)
        );
    }
}