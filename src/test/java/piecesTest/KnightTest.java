package piecesTest;

import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.pieces.Knight;
import model.pieces.Pawn; // Assuming you have a Pawn class or similar for testing captures/blocks
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KnightTest {

    private Board board;
    private Knight whiteKnight;

    @BeforeEach
    void setUp() {
        board = new Board(); // Assumes Board() creates a standard 8x8 empty board
    }

    // Helper method to check if a list of positions contains a specific position
    private boolean containsPosition(List<Position> positions, int row, int col) {
        return positions.stream().anyMatch(p -> p.getRow() == row && p.getCol() == col);
    }

    @Test
    void testKnightMovesFromCenter() {
        // Place knight at d4 (row 3, col 3 in 0-based indexing)
        Square startSquare = board.getSquareAt(new Position(3, 3));
        whiteKnight = new Knight(ChessColor.WHITE, startSquare);
        startSquare.setPiece(whiteKnight);

        List<Position> validMoves = whiteKnight.getValidMoves(board);

        // A knight in the center should have 8 possible moves
        assertEquals(8, validMoves.size());

        // Check specific expected moves
        assertTrue(containsPosition(validMoves, 1, 2)); // c6
        assertTrue(containsPosition(validMoves, 1, 4)); // e6
        assertTrue(containsPosition(validMoves, 2, 1)); // b5
        assertTrue(containsPosition(validMoves, 2, 5)); // f5
        assertTrue(containsPosition(validMoves, 4, 1)); // b3
        assertTrue(containsPosition(validMoves, 4, 5)); // f3
        assertTrue(containsPosition(validMoves, 5, 2)); // c2
        assertTrue(containsPosition(validMoves, 5, 4)); // e2
    }

    @Test
    void testKnightMovesFromCornerA1() {
        // Place knight at a1 (row 7, col 0)
        Square startSquare = board.getSquareAt(new Position(7, 0));
        whiteKnight = new Knight(ChessColor.WHITE, startSquare);
        startSquare.setPiece(whiteKnight);

        List<Position> validMoves = whiteKnight.getValidMoves(board);

        // A knight at a1 should have 2 possible moves
        assertEquals(2, validMoves.size());
        assertTrue(containsPosition(validMoves, 5, 1)); // b3
        assertTrue(containsPosition(validMoves, 6, 2)); // c2
    }

    @Test
    void testKnightMovesFromCornerH8() {
        // Place knight at h8 (row 0, col 7)
        Square startSquare = board.getSquareAt(new Position(0, 7));
        Knight blackKnight = new Knight(ChessColor.BLACK, startSquare);
        startSquare.setPiece(blackKnight);

        List<Position> validMoves = blackKnight.getValidMoves(board);

        // A knight at h8 should have 2 possible moves
        assertEquals(2, validMoves.size());
        assertTrue(containsPosition(validMoves, 1, 5)); // f7
        assertTrue(containsPosition(validMoves, 2, 6)); // g6
    }

    @Test
    void testKnightMovesBlockedByOwnPiece() {
        // Place knight at b1 (row 7, col 1)
        Square startSquare = board.getSquareAt(new Position(7, 1));
        whiteKnight = new Knight(ChessColor.WHITE, startSquare);
        startSquare.setPiece(whiteKnight);

        // Place a friendly pawn at d2 (row 6, col 3), one of the knight's potential moves
        Square blockedSquare = board.getSquareAt(new Position(6, 3));
        Pawn friendlyPawn = new Pawn(ChessColor.WHITE, blockedSquare); // Assuming Pawn class exists
        blockedSquare.setPiece(friendlyPawn);

        List<Position> validMoves = whiteKnight.getValidMoves(board);

        // A knight at b1 normally has 3 moves (a3, c3, d2)
        // Since d2 is blocked by a friendly piece, it should only have 2 moves
        assertEquals(2, validMoves.size());
        assertTrue(containsPosition(validMoves, 5, 0)); // a3
        assertTrue(containsPosition(validMoves, 5, 2)); // c3
        assertFalse(containsPosition(validMoves, 6, 3)); // d2 should NOT be valid
    }

    @Test
    void testKnightMovesCapturingOpponentPiece() {
        // Place knight at g1 (row 7, col 6)
        Square startSquare = board.getSquareAt(new Position(7, 6));
        whiteKnight = new Knight(ChessColor.WHITE, startSquare);
        startSquare.setPiece(whiteKnight);

        // Place an enemy pawn at e2 (row 6, col 4), one of the knight's potential moves
        Square captureSquare = board.getSquareAt(new Position(6, 4));
        Pawn enemyPawn = new Pawn(ChessColor.BLACK, captureSquare); // Assuming Pawn class exists
        captureSquare.setPiece(enemyPawn);

        List<Position> validMoves = whiteKnight.getValidMoves(board);

        // A knight at g1 normally has 3 moves (e2, f3, h3)
        // Capturing e2 is allowed
        assertEquals(3, validMoves.size());
        assertTrue(containsPosition(validMoves, 6, 4)); // e2 (capture)
        assertTrue(containsPosition(validMoves, 5, 5)); // f3
        assertTrue(containsPosition(validMoves, 5, 7)); // h3
    }

    @Test
    void testKnightMovesMixedScenario() {
        // Place knight at d5 (row 3, col 3)
        Square startSquare = board.getSquareAt(new Position(3, 3));
        whiteKnight = new Knight(ChessColor.WHITE, startSquare);
        startSquare.setPiece(whiteKnight);

        // Place a friendly piece at c7 (row 1, col 2)
        Square friendlySquare = board.getSquareAt(new Position(1, 2));
        friendlySquare.setPiece(new Pawn(ChessColor.WHITE, friendlySquare));

        // Place an enemy piece at e7 (row 1, col 4)
        Square enemySquare = board.getSquareAt(new Position(1, 4));
        enemySquare.setPiece(new Pawn(ChessColor.BLACK, enemySquare));

        // Place an enemy piece at f6 (row 2, col 5)
        Square enemySquare2 = board.getSquareAt(new Position(2, 5));
        enemySquare2.setPiece(new Pawn(ChessColor.BLACK, enemySquare2));

        List<Position> validMoves = whiteKnight.getValidMoves(board);

        // Expected moves: e7(capture), b6, f6(capture), b4, c3, e3, f4
        // Blocked move: c7
        // Total: 7 moves
        assertEquals(7, validMoves.size());

        // Check specific moves
        assertFalse(containsPosition(validMoves, 1, 2)); // c7 (blocked by friendly)
        assertTrue(containsPosition(validMoves, 1, 4));  // e7 (capture enemy)
        assertTrue(containsPosition(validMoves, 2, 1));  // b6
        assertTrue(containsPosition(validMoves, 2, 5));  // f6 (capture enemy)
        assertTrue(containsPosition(validMoves, 4, 1));  // b4
        assertTrue(containsPosition(validMoves, 4, 5));  // f4
        assertTrue(containsPosition(validMoves, 5, 2));  // c3
        assertTrue(containsPosition(validMoves, 5, 4));  // e3
    }
}