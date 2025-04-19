package piecesTest;

import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.enums.PieceType;
import model.pieces.Pawn; // Assuming you have a Pawn class for testing blocking/capturing
import model.pieces.Rook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RookTest {

    private Board board;
    private Rook whiteRook;

    @BeforeEach
    void setUp() {
        // Initialize a standard 8x8 board before each test
        board = new Board();
        // Clear the board for controlled testing (assuming Board has a clear method or similar)
        // If not, you might need to manually set squares to empty or create a test-specific board setup.
        // For simplicity, we'll assume the board starts empty or we place pieces explicitly.
    }

    // Helper method to easily compare lists of positions
    private void assertMovesContain(List<Position> actualMoves, Position... expectedPositions) {
        assertEquals(expectedPositions.length, actualMoves.size(), "Number of moves mismatch");
        for (Position expected : expectedPositions) {
            assertTrue(actualMoves.contains(expected), "Expected move " + expected + " not found in actual moves: " + actualMoves);
        }
    }

    @Test
    void testGetValidMoves_EmptyBoard_Center() {
        // Place a white rook in the center (e.g., d4)
        Position startPos = new Position(3, 3); // d4
        whiteRook = new Rook(ChessColor.WHITE, board.getSquareAt(startPos));
        board.getSquareAt(startPos).setPiece(whiteRook);

        List<Position> validMoves = whiteRook.getValidMoves(board);

        // Expected moves: all squares along rank 4 and file d, excluding d4 itself
        assertEquals(14, validMoves.size()); // 7 horizontal + 7 vertical

        // Check a few key positions
        assertTrue(validMoves.contains(new Position(3, 0))); // a4
        assertTrue(validMoves.contains(new Position(3, 7))); // h4
        assertTrue(validMoves.contains(new Position(0, 3))); // d1
        assertTrue(validMoves.contains(new Position(7, 3))); // d8
        assertFalse(validMoves.contains(startPos)); // Cannot move to its own square
    }

    @Test
    void testGetValidMoves_EmptyBoard_Corner() {
        // Place a white rook in the corner (a1)
        Position startPos = new Position(0, 0); // a1
        whiteRook = new Rook(ChessColor.WHITE, board.getSquareAt(startPos));
        board.getSquareAt(startPos).setPiece(whiteRook);

        List<Position> validMoves = whiteRook.getValidMoves(board);

        // Expected moves: all squares along rank 1 and file a, excluding a1
        assertEquals(14, validMoves.size()); // 7 horizontal + 7 vertical

        assertTrue(validMoves.contains(new Position(0, 7))); // h1
        assertTrue(validMoves.contains(new Position(7, 0))); // a8
        assertFalse(validMoves.contains(startPos));
    }

    @Test
    void testGetValidMoves_BlockedByFriendlyPieces() {
        // Place white rook at d4
        Position startPos = new Position(3, 3); // d4
        whiteRook = new Rook(ChessColor.WHITE, board.getSquareAt(startPos));
        board.getSquareAt(startPos).setPiece(whiteRook);

        // Place friendly pawns blocking movement
        Position blockUpPos = new Position(5, 3); // d6
        Position blockRightPos = new Position(3, 5); // f4
        board.getSquareAt(blockUpPos).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(blockUpPos)));
        board.getSquareAt(blockRightPos).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(blockRightPos)));

        List<Position> validMoves = whiteRook.getValidMoves(board);

        // Expected moves: d1, d2, d3, d5 (up blocked at d6)
        //                 a4, b4, c4, e4 (right blocked at f4)
        assertMovesContain(validMoves,
                // Vertical down
                new Position(2, 3), new Position(1, 3), new Position(0, 3),
                // Vertical up (stops before d6)
                new Position(4, 3),
                // Horizontal left
                new Position(3, 2), new Position(3, 1), new Position(3, 0),
                // Horizontal right (stops before f4)
                new Position(3, 4)
        );
        assertEquals(8, validMoves.size());
        assertFalse(validMoves.contains(blockUpPos)); // Cannot move onto friendly piece
        assertFalse(validMoves.contains(blockRightPos)); // Cannot move onto friendly piece
        assertFalse(validMoves.contains(new Position(6, 3))); // Cannot move past friendly piece
        assertFalse(validMoves.contains(new Position(3, 6))); // Cannot move past friendly piece
    }

    @Test
    void testGetValidMoves_CaptureOpponentPieces() {
        // Place white rook at d4
        Position startPos = new Position(3, 3); // d4
        whiteRook = new Rook(ChessColor.WHITE, board.getSquareAt(startPos));
        board.getSquareAt(startPos).setPiece(whiteRook);

        // Place opponent pawns to be captured
        Position captureUpPos = new Position(5, 3); // d6
        Position captureRightPos = new Position(3, 5); // f4
        board.getSquareAt(captureUpPos).setPiece(new Pawn(ChessColor.BLACK, board.getSquareAt(captureUpPos)));
        board.getSquareAt(captureRightPos).setPiece(new Pawn(ChessColor.BLACK, board.getSquareAt(captureRightPos)));

        List<Position> validMoves = whiteRook.getValidMoves(board);

        // Expected moves: d1, d2, d3, d5, d6 (capture)
        //                 a4, b4, c4, e4, f4 (capture)
        assertMovesContain(validMoves,
                // Vertical down
                new Position(2, 3), new Position(1, 3), new Position(0, 3),
                // Vertical up (stops AT d6 - capture)
                new Position(4, 3), captureUpPos,
                // Horizontal left
                new Position(3, 2), new Position(3, 1), new Position(3, 0),
                // Horizontal right (stops AT f4 - capture)
                new Position(3, 4), captureRightPos
        );
        assertEquals(10, validMoves.size());
        assertTrue(validMoves.contains(captureUpPos)); // Can move onto opponent piece
        assertTrue(validMoves.contains(captureRightPos)); // Can move onto opponent piece
        assertFalse(validMoves.contains(new Position(6, 3))); // Cannot move past captured piece
        assertFalse(validMoves.contains(new Position(3, 6))); // Cannot move past captured piece
    }

    @Test
    void testGetValidMoves_CompletelyBlocked() {
        // Place white rook at a1
        Position startPos = new Position(0, 0); // a1
        whiteRook = new Rook(ChessColor.WHITE, board.getSquareAt(startPos));
        board.getSquareAt(startPos).setPiece(whiteRook);

        // Place friendly pieces blocking immediately
        Position blockUpPos = new Position(1, 0); // a2
        Position blockRightPos = new Position(0, 1); // b1
        board.getSquareAt(blockUpPos).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(blockUpPos)));
        board.getSquareAt(blockRightPos).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(blockRightPos)));

        List<Position> validMoves = whiteRook.getValidMoves(board);

        assertEquals(0, validMoves.size(), "Rook should have no valid moves when completely blocked by friendly pieces");
    }

    @Test
    void testGetValidMoves_BlockedByOpponentImmediately() {
        // Place white rook at a1
        Position startPos = new Position(0, 0); // a1
        whiteRook = new Rook(ChessColor.WHITE, board.getSquareAt(startPos));
        board.getSquareAt(startPos).setPiece(whiteRook);

        // Place opponent pieces blocking immediately (can be captured)
        Position captureUpPos = new Position(1, 0); // a2
        Position captureRightPos = new Position(0, 1); // b1
        board.getSquareAt(captureUpPos).setPiece(new Pawn(ChessColor.BLACK, board.getSquareAt(captureUpPos)));
        board.getSquareAt(captureRightPos).setPiece(new Pawn(ChessColor.BLACK, board.getSquareAt(captureRightPos)));

        List<Position> validMoves = whiteRook.getValidMoves(board);

        assertMovesContain(validMoves, captureUpPos, captureRightPos);
        assertEquals(2, validMoves.size());
    }

    @Test
    void testConstructorAndGetters() {
        Position startPos = new Position(0, 0);
        Square startSquare = board.getSquareAt(startPos);
        Rook rook = new Rook(ChessColor.BLACK, startSquare);

        assertEquals(ChessColor.BLACK, rook.getColor());
        assertEquals(PieceType.ROOK, rook.getType());
        assertEquals(startSquare, rook.getCurrentSquare());
        assertEquals(startPos, rook.getCurrentSquare().getPosition()); // Assuming Piece has getCurrentPosition
    }
}