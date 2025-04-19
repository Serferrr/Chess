package boardTest;

import model.pieces.Piece; // Assuming Piece is an abstract class or interface
// Import necessary concrete piece details if needed, or use a dummy
import model.enums.ChessColor;    // Example: If Piece needs Color
import model.enums.PieceType; // Example: If Piece needs PieceType
import model.board.Board;
import model.board.Position;
import model.board.Square;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections; // For dummy getValidMoves
import java.util.List;        // For dummy getValidMoves

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Square class (without mocks).
 */
class SquareTest {

    // Simple concrete implementation of Piece for testing
    private static class DummyPiece extends Piece {
        // Provide a basic constructor if Piece requires it
        public DummyPiece() {
            super(ChessColor.WHITE, PieceType.PAWN, null);
        }

        @Override
        public List<Position> getValidMoves(Board board, Square square) {
            // Not relevant for Square tests, return empty list
            return Collections.emptyList();
        }

        // Implement other abstract methods if Piece has them
        // e.g., getColor(), getType(), etc. returning default/dummy values
        @Override
        public ChessColor getColor() { return ChessColor.WHITE; } // Example

        @Override
        public PieceType getType() { return PieceType.PAWN; } // Example
    }

    private Position testPosition;
    private Piece testPiece;
    private Square square;

    @BeforeEach
    void setUp() {
        // Create actual instances instead of mocks
        testPosition = new Position(1, 2); // Use concrete Position
        testPiece = new DummyPiece();       // Use our dummy Piece implementation

        // Create a new Square instance for each test
        square = new Square(testPosition, testPiece); // Pass the real Position
    }

    @Test
    void constructor_shouldSetPositionCorrectly() {
        // Assert that the position passed during construction is correctly stored
        // Use assertEquals if Position has a proper equals method, otherwise assertSame is fine here
        assertEquals(testPosition, square.getPosition(), "The position should be the one passed in the constructor.");
        // Or: assertSame(testPosition, square.getPosition(), "The position should be the exact instance passed in the constructor.");
    }

    @Test
    void getPosition_shouldReturnCorrectPosition() {
        // This test is similar to the constructor test but explicitly tests the getter
        Position specificPosition = new Position(3, 4); // Example concrete position
        Square specificSquare = new Square(specificPosition,null);
        assertEquals(specificPosition, specificSquare.getPosition(), "getPosition should return the correct Position object.");
    }

    @Test
    void setPiece_shouldPlacePieceOnSquare() {
        // Act: Set a piece on the square
        square.setPiece(testPiece);

        // Assert: Verify the piece is set correctly
        assertSame(testPiece, square.getPiece(), "getPiece should return the piece that was set.");
        assertFalse(square.isEmpty(), "The square should not be empty after setting a piece.");
    }

    @Test
    void setPiece_shouldReplaceExistingPiece() {
        // Arrange: Set an initial piece
        Piece initialPiece = new DummyPiece(); // Create another instance
        square.setPiece(initialPiece);
        assertSame(initialPiece, square.getPiece()); // Pre-condition check
        assertFalse(square.isEmpty());             // Pre-condition check

        // Act: Set a new piece (the testPiece from setUp)
        square.setPiece(testPiece);

        // Assert: Verify the new piece replaced the old one
        assertSame(testPiece, square.getPiece(), "getPiece should return the newly set piece.");
        assertFalse(square.isEmpty(), "The square should still not be empty after replacing a piece.");
    }

    @Test
    void setPiece_withNullShouldMakeSquareEmpty() {
        // Arrange: Set an initial piece
        square.setPiece(testPiece);
        assertFalse(square.isEmpty(), "Square should not be empty initially in this test."); // Pre-condition

        // Act: Set the piece to null
        square.setPiece(null);

        // Assert: Verify the square is now empty
        assertNull(square.getPiece(), "getPiece should return null after setting the piece to null.");
        assertTrue(square.isEmpty(), "isEmpty should return true after setting the piece to null.");
    }

    @Test
    void isEmpty_shouldReturnTrueWhenPieceIsNull() {
        // Arrange: Ensure piece is null (it is by default after setUp)
        square.setPiece(null); // Explicitly set to null for clarity

        // Assert
        assertTrue(square.isEmpty(), "isEmpty should return true when the piece is null.");
    }

    @Test
    void isEmpty_shouldReturnFalseWhenPieceIsNotNull() {
        // Arrange: Set a piece
        square.setPiece(testPiece);

        // Assert
        assertFalse(square.isEmpty(), "isEmpty should return false when the square has a piece.");
    }
}