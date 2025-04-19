package piecesTest;

import model.pieces.*;
import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueenTest {

    private Board board;
    private Queen whiteQueen;

    // Helper to create a position - assumes 0-based indexing
    private Position pos(int row, int col) {
        return new Position(row, col);
    }

    // Helper to place a piece on the board
    private void placePiece(Piece piece) {
        if (piece == null || piece.getCurrentSquare() == null) return;
        board.setPieceAt(piece, piece.getCurrentSquare().getPosition());
        // Ensure piece's internal square reference is correct if needed
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
        // Optional stricter check for unexpected moves
        for(Position actual : actualMoves) {
            boolean found = false;
            for(Position expected : expectedPositions) {
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
        board = new Board(); // Creates a new board, potentially with standard setup
        // Clear the board if the default constructor sets up pieces
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board.setPieceAt(null, pos(r, c));
            }
        }
    }

    @Test
    void testConstructor() {
        Square startSquare = board.getSquareAt(pos(0, 3)); // d1
        Queen queen = new Queen(ChessColor.WHITE, startSquare);

        assertEquals(ChessColor.WHITE, queen.getColor());
        assertEquals(PieceType.QUEEN, queen.getPieceType());
        assertEquals(startSquare, queen.getCurrentSquare());
        assertEquals(pos(0, 3), queen.getCurrentSquare().getPosition()); // Assuming Piece has getCurrentPosition
    }

    @Test
    void testGetValidMoves_EmptyBoard_Center() {
        // Place Queen in the center (d4)
        Square startSquare = board.getSquareAt(pos(3, 3));
        whiteQueen = new Queen(ChessColor.WHITE, startSquare);
        placePiece(whiteQueen);

        List<Position> moves = whiteQueen.getValidMoves(board, startSquare);

        // Expected: 7 diagonal + 7 diagonal + 7 horizontal + 7 vertical = 28 moves? No, 27.
        // Let's list a few key ones and check size
        assertEquals(27, moves.size(), "Queen in center on empty board should have 27 moves");

        // Check corners reachable
        assertTrue(moves.contains(pos(0, 0))); // Diagonal
        assertTrue(moves.contains(pos(7, 7))); // Diagonal
        assertTrue(moves.contains(pos(0, 6))); // Diagonal
        assertTrue(moves.contains(pos(6, 0))); // Diagonal
        assertTrue(moves.contains(pos(0, 3))); // Vertical
        assertTrue(moves.contains(pos(7, 3))); // Vertical
        assertTrue(moves.contains(pos(3, 0))); // Horizontal
        assertTrue(moves.contains(pos(3, 7))); // Horizontal

        // Check adjacent squares
        assertTrue(moves.contains(pos(2, 2)));
        assertTrue(moves.contains(pos(2, 3)));
        assertTrue(moves.contains(pos(2, 4)));
        assertTrue(moves.contains(pos(3, 2)));
        assertTrue(moves.contains(pos(3, 4)));
        assertTrue(moves.contains(pos(4, 2)));
        assertTrue(moves.contains(pos(4, 3)));
        assertTrue(moves.contains(pos(4, 4)));
    }

    @Test
    void testGetValidMoves_EmptyBoard_Corner() {
        // Place Queen in corner (a1)
        Square startSquare = board.getSquareAt(pos(0, 0));
        whiteQueen = new Queen(ChessColor.WHITE, startSquare);
        placePiece(whiteQueen);

        List<Position> moves = whiteQueen.getValidMoves(board, startSquare);

        // Expected: 7 diagonal + 7 horizontal + 7 vertical = 21 moves
        assertEquals(21, moves.size(), "Queen in corner on empty board should have 21 moves");

        assertTrue(moves.contains(pos(7, 7))); // Diagonal
        assertTrue(moves.contains(pos(0, 7))); // Horizontal
        assertTrue(moves.contains(pos(7, 0))); // Vertical
    }

    @Test
    void testGetValidMoves_BlockedByFriendlyPieces() {
        // Place Queen at d4
        Square startSquare = board.getSquareAt(pos(3, 3));
        whiteQueen = new Queen(ChessColor.WHITE, startSquare);
        placePiece(whiteQueen);

        // Place friendly pawns blocking
        placePiece(new Pawn(ChessColor.WHITE, board.getSquareAt(pos(5, 5)))); // Diagonal up-right
        placePiece(new Pawn(ChessColor.WHITE, board.getSquareAt(pos(3, 1)))); // Horizontal left
        placePiece(new Pawn(ChessColor.WHITE, board.getSquareAt(pos(1, 3)))); // Vertical down

        List<Position> moves = whiteQueen.getValidMoves(board, startSquare);

        // Calculate expected moves manually
        assertMovesMatch(moves,
                // Diagonal up-right (stops before 5,5)
                pos(4, 4),
                // Diagonal up-left
                pos(4, 2), pos(5, 1), pos(6, 0),
                // Diagonal down-right
                pos(2, 4), pos(1, 5), pos(0, 6),
                // Diagonal down-left
                pos(2, 2), pos(1, 1), pos(0, 0),
                // Horizontal left (stops before 3,1)
                pos(3, 2),
                // Horizontal right
                pos(3, 4), pos(3, 5), pos(3, 6), pos(3, 7),
                // Vertical down (stops before 1,3)
                pos(2, 3),
                // Vertical up
                pos(4, 3), pos(5, 3), pos(6, 3), pos(7, 3)
        );
        // Count: 1 + 3 + 3 + 3 + 1 + 4 + 1 + 4 = 20 moves
        assertEquals(20, moves.size());
    }

    @Test
    void testGetValidMoves_CapturingOpponentPieces() {
        // Place Queen at d4
        Square startSquare = board.getSquareAt(pos(3, 3));
        whiteQueen = new Queen(ChessColor.WHITE, startSquare);
        placePiece(whiteQueen);

        // Place opponent pawns to capture
        Position captureDiag = pos(5, 5);
        Position captureHoriz = pos(3, 1);
        Position captureVert = pos(1, 3);
        placePiece(new Pawn(ChessColor.BLACK, board.getSquareAt(captureDiag)));
        placePiece(new Pawn(ChessColor.BLACK, board.getSquareAt(captureHoriz)));
        placePiece(new Pawn(ChessColor.BLACK, board.getSquareAt(captureVert)));

        List<Position> moves = whiteQueen.getValidMoves(board, startSquare);

        // Calculate expected moves manually (includes capture squares)
        assertMovesMatch(moves,
                // Diagonal up-right (stops AT 5,5)
                pos(4, 4), captureDiag,
                // Diagonal up-left
                pos(4, 2), pos(5, 1), pos(6, 0),
                // Diagonal down-right
                pos(2, 4), pos(1, 5), pos(0, 6),
                // Diagonal down-left
                pos(2, 2), pos(1, 1), pos(0, 0),
                // Horizontal left (stops AT 3,1)
                pos(3, 2), captureHoriz,
                // Horizontal right
                pos(3, 4), pos(3, 5), pos(3, 6), pos(3, 7),
                // Vertical down (stops AT 1,3)
                pos(2, 3), captureVert,
                // Vertical up
                pos(4, 3), pos(5, 3), pos(6, 3), pos(7, 3)
        );
        // Count: 2 + 3 + 3 + 3 + 2 + 4 + 2 + 4 = 23 moves
        assertEquals(23, moves.size());
    }

    @Test
    void testGetValidMoves_CompletelyBlocked() {
        // Place Queen at d4
        Square startSquare = board.getSquareAt(pos(3, 3));
        whiteQueen = new Queen(ChessColor.WHITE, startSquare);
        placePiece(whiteQueen);

        // Surround with friendly pieces
        placePiece(new Pawn(ChessColor.WHITE, board.getSquareAt(pos(2, 2))));
        placePiece(new Pawn(ChessColor.WHITE, board.getSquareAt(pos(2, 3))));
        placePiece(new Pawn(ChessColor.WHITE, board.getSquareAt(pos(2, 4))));
        placePiece(new Pawn(ChessColor.WHITE, board.getSquareAt(pos(3, 2))));
        placePiece(new Pawn(ChessColor.WHITE, board.getSquareAt(pos(3, 4))));
        placePiece(new Pawn(ChessColor.WHITE, board.getSquareAt(pos(4, 2))));
        placePiece(new Pawn(ChessColor.WHITE, board.getSquareAt(pos(4, 3))));
        placePiece(new Pawn(ChessColor.WHITE, board.getSquareAt(pos(4, 4))));

        List<Position> moves = whiteQueen.getValidMoves(board, startSquare);

        assertTrue(moves.isEmpty(), "Queen should have no moves when completely blocked");
    }
}