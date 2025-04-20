package piecesTest;

import model.pieces.*;
import model.board.Board;
import model.board.Position;
import model.enums.ChessColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BishopTest {

    private Board board;
    private Bishop whiteBishop;

    @BeforeEach
    void setUp() {
        // Initialize a standard 8x8 board for each test
        board = new Board();
    }

    // Helper method to create a position list for easier comparison
    private List<Position> createPositionList(int... coords) {
        List<Position> positions = new java.util.ArrayList<>();
        for (int i = 0; i < coords.length; i += 2) {
            positions.add(new Position(coords[i], coords[i + 1]));
        }
        return positions;
    }

    // Helper method to check if a list contains specific positions
    private void assertMovesContain(List<Position> actualMoves, List<Position> expectedMoves) {
        assertEquals(expectedMoves.size(), actualMoves.size(), "Number of moves should match.");
        assertTrue(actualMoves.containsAll(expectedMoves), "Actual moves should contain all expected moves.");
        assertTrue(expectedMoves.containsAll(actualMoves), "Expected moves should contain all actual moves.");
    }

    @Test
    void testGetValidMoves_CenterEmptyBoard() {
        // Place a white bishop in the center (e.g., d4 -> row 3, col 3)
        whiteBishop = new Bishop(ChessColor.WHITE, board.getSquareAt(new Position(3, 3)));
        board.getSquareAt(new Position(3, 3)).setPiece(whiteBishop);

        List<Position> validMoves = whiteBishop.getValidMoves(board);

        // Expected moves from (3,3) on an empty board
        List<Position> expectedMoves = createPositionList(
                // Up-Right diagonal
                4, 4, 5, 5, 6, 6, 7, 7,
                // Up-Left diagonal
                4, 2, 5, 1, 6, 0,
                // Down-Right diagonal
                2, 4, 1, 5, 0, 6,
                // Down-Left diagonal
                2, 2, 1, 1, 0, 0
        );

        assertMovesContain(validMoves, expectedMoves);
    }

    @Test
    void testGetValidMoves_CornerEmptyBoard() {
        // Place a white bishop in a corner (e.g., a1 -> row 0, col 0)
        whiteBishop = new Bishop(ChessColor.WHITE, board.getSquareAt(new Position(0, 0)));
        board.getSquareAt(new Position(0, 0)).setPiece(whiteBishop);

        List<Position> validMoves = whiteBishop.getValidMoves(board);

        // Expected moves from (0,0) on an empty board
        List<Position> expectedMoves = createPositionList(
                1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7
        );

        assertMovesContain(validMoves, expectedMoves);
    }

    @Test
    void testGetValidMoves_BlockedByFriendlyPieces() {
        // Place white bishop at d4 (3,3)
        whiteBishop = new Bishop(ChessColor.WHITE, board.getSquareAt(new Position(3, 3)));
        board.getSquareAt(new Position(3, 3)).setPiece(whiteBishop);

        // Place friendly pieces (White Pawns) blocking diagonals
        board.getSquareAt(new Position(5, 5)).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(new Position(5, 5)))); // Up-Right
        board.getSquareAt(new Position(1, 1)).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(new Position(1, 1)))); // Down-Left

        List<Position> validMoves = whiteBishop.getValidMoves(board);

        // Expected moves from (3,3) blocked by friendly pieces at (5,5) and (1,1)
        List<Position> expectedMoves = createPositionList(
                // Up-Right diagonal (blocked at 5,5)
                4, 4,
                // Up-Left diagonal
                4, 2, 5, 1, 6, 0,
                // Down-Right diagonal
                2, 4, 1, 5, 0, 6,
                // Down-Left diagonal (blocked at 1,1)
                2, 2
        );

        assertMovesContain(validMoves, expectedMoves);
    }

    @Test
    void testGetValidMoves_CapturingOpponentPieces() {
        // Place white bishop at d4 (3,3)
        whiteBishop = new Bishop(ChessColor.WHITE, board.getSquareAt(new Position(3, 3)));
        board.getSquareAt(new Position(3, 3)).setPiece(whiteBishop);

        // Place opponent pieces (Black Pawns) on diagonals
        board.getSquareAt(new Position(5, 5)).setPiece(new Pawn(ChessColor.BLACK, board.getSquareAt(new Position(5, 5)))); // Up-Right (capturable)
        board.getSquareAt(new Position(1, 1)).setPiece(new Pawn(ChessColor.BLACK, board.getSquareAt(new Position(1, 1)))); // Down-Left (capturable)
        board.getSquareAt(new Position(6,0)).setPiece(new Pawn(ChessColor.BLACK, board.getSquareAt(new Position(6,0)))); // Up-Left (capturable)


        List<Position> validMoves = whiteBishop.getValidMoves(board);

        // Expected moves from (3,3) with captures at (5,5), (1,1), (6,0)
        List<Position> expectedMoves = createPositionList(
                // Up-Right diagonal (captures at 5,5, stops there)
                4, 4, 5, 5,
                // Up-Left diagonal (captures at 6,0, stops there)
                4, 2, 5, 1, 6, 0,
                // Down-Right diagonal
                2, 4, 1, 5, 0, 6,
                // Down-Left diagonal (captures at 1,1, stops there)
                2, 2, 1, 1
        );

        assertMovesContain(validMoves, expectedMoves);
    }

    @Test
    void testGetValidMoves_MixedBlockingAndCapturing() {
        // Place black bishop at e5 (4,4)
        Bishop blackBishop = new Bishop(ChessColor.BLACK, board.getSquareAt(new Position(4, 4)));
        board.getSquareAt(new Position(4, 4)).setPiece(blackBishop);

        // Friendly piece (Black Pawn)
        board.getSquareAt(new Position(2, 6)).setPiece(new Pawn(ChessColor.BLACK, board.getSquareAt(new Position(2, 6)))); // Down-Right (blocking)

        // Opponent pieces (White Pawns)
        board.getSquareAt(new Position(6, 6)).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(new Position(6, 6)))); // Up-Right (capturable)
        board.getSquareAt(new Position(1, 1)).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(new Position(1, 1)))); // Down-Left (capturable)


        List<Position> validMoves = blackBishop.getValidMoves(board);

        // Expected moves from (4,4)
        List<Position> expectedMoves = createPositionList(
                // Up-Right diagonal (captures at 6,6)
                5, 5, 6, 6,
                // Up-Left diagonal
                5, 3, 6, 2, 7, 1,
                // Down-Right diagonal (blocked at 2,6)
                3, 5,
                // Down-Left diagonal (captures at 1,1)
                3, 3, 2, 2, 1, 1
        );

        assertMovesContain(validMoves, expectedMoves);
    }

    @Test
    void testGetValidMoves_NoMovesPossible() {
        // Place white bishop at d4 (3,3)
        whiteBishop = new Bishop(ChessColor.WHITE, board.getSquareAt(new Position(3, 3)));
        board.getSquareAt(new Position(3, 3)).setPiece(whiteBishop);

        // Surround with friendly pieces
        board.getSquareAt(new Position(4, 4)).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(new Position(4, 4))));
        board.getSquareAt(new Position(4, 2)).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(new Position(4, 2))));
        board.getSquareAt(new Position(2, 4)).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(new Position(2, 4))));
        board.getSquareAt(new Position(2, 2)).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(new Position(2, 2))));

        List<Position> validMoves = whiteBishop.getValidMoves(board);

        assertTrue(validMoves.isEmpty(), "Bishop should have no valid moves when completely blocked by friendly pieces.");
    }
}