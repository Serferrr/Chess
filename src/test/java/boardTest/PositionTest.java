package boardTest;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import model.board.Position;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;


class PositionTest {

    @Test
    void constructorAndGetters_shouldInitializeCorrectly_withValidInput() {
        int expectedRow = 5;
        int expectedCol = 3; // Use valid column index (0-7)
        Position position = new Position(expectedRow, expectedCol);

        assertEquals(expectedRow, position.getRow(), "Row should match the value passed to the constructor.");
        assertEquals(expectedCol, position.getCol(), "Column should match the value passed to the constructor.");
    }

    // Test constructor validation for invalid inputs
    @ParameterizedTest
    @CsvSource({
            "-1, 5",  // Invalid row (too low)
            "8, 5",   // Invalid row (too high)
            "5, -1",  // Invalid col (too low)
            "5, 8"    // Invalid col (too high)
    })
    void constructor_shouldThrowException_forInvalidInput(int row, int col) {
        assertThrows(IllegalArgumentException.class, () -> {
            new Position(row, col);
        }, "Constructor should throw IllegalArgumentException for out-of-bounds row/col: row=" + row + ", col=" + col);
    }


    @Test
    void equals_shouldReturnTrue_forSameObject() {
        Position position1 = new Position(3, 4);
        assertEquals(position1, position1, "An object should be equal to itself.");
    }

    @Test
    void equals_shouldReturnTrue_forEqualObjects() {
        Position position1 = new Position(7, 2);
        Position position2 = new Position(7, 2);
        assertEquals(position1, position2, "Objects with the same row and column should be equal.");
        assertEquals(position2, position1, "Equality should be symmetric.");
    }

    @Test
    void equals_shouldReturnFalse_forNull() {
        Position position1 = new Position(1, 1);
        // Use assertNotEquals with explicit null check for clarity if needed,
        // but assertEquals(false, position1.equals(null)) is also valid.
        assertFalse(position1.equals(null), "An object should not be equal to null.");
    }

    @Test
    void equals_shouldReturnFalse_forDifferentClass() {
        Position position1 = new Position(2, 3);
        String otherObject = "Not a Position";
        // Use assertNotEquals with explicit check for clarity
        assertFalse(position1.equals(otherObject), "An object should not be equal to an object of a different class.");
    }

    @Test
    void equals_shouldReturnFalse_forDifferentRow() {
        Position position1 = new Position(5, 7); // Use valid col
        Position position2 = new Position(6, 7); // Different row
        assertNotEquals(position1, position2, "Objects with different rows should not be equal.");
    }

    @Test
    void equals_shouldReturnFalse_forDifferentCol() {
        Position position1 = new Position(5, 6); // Use valid col
        Position position2 = new Position(5, 7); // Different column
        assertNotEquals(position1, position2, "Objects with different columns should not be equal.");
    }

    @Test
    void hashCode_shouldBeEqual_forEqualObjects() {
        Position position1 = new Position(7, 1); // Use valid row/col
        Position position2 = new Position(7, 1);
        assertEquals(position1.hashCode(), position2.hashCode(), "Equal objects must have the same hash code.");
    }

    // This test was incorrectly named/implemented. Renamed to test consistency for a *valid* object.
    @Test
    void hashCode_shouldBeConsistent_forSameObject() {
        Position position = new Position(4, 5);
        int initialHashCode = position.hashCode();
        // Call hashCode multiple times on the same object
        assertEquals(initialHashCode, position.hashCode(), "hashCode should be consistent across multiple calls.");
        assertEquals(initialHashCode, position.hashCode(), "hashCode should be consistent across multiple calls.");
    }


    @Test
    void hashCode_shouldLikelyBeDifferent_forUnequalObjects() {
        Position position1 = new Position(1, 2);
        Position position2 = new Position(2, 1); // Different row and col
        Position position3 = new Position(1, 3); // Different col
        Position position4 = new Position(3, 2); // Different row

        assertNotEquals(position1.hashCode(), position2.hashCode(), "Unequal objects should ideally have different hash codes (position 2).");
        assertNotEquals(position1.hashCode(), position3.hashCode(), "Unequal objects should ideally have different hash codes (position 3).");
        assertNotEquals(position1.hashCode(), position4.hashCode(), "Unequal objects should ideally have different hash codes (position 4).");
    }

    // --- Tests for new methods ---

    @ParameterizedTest
    @CsvSource({
            "0, 'a'",
            "1, 'b'",
            "2, 'c'",
            "3, 'd'",
            "4, 'e'",
            "5, 'f'",
            "6, 'g'",
            "7, 'h'"
    })
    void getColChar_shouldReturnCorrectChar(int col, char expectedChar) {
        Position position = new Position(0, col); // Row doesn't matter for this test
        assertEquals(expectedChar, position.getColChar(), "Column " + col + " should correspond to char '" + expectedChar + "'.");
    }

    @ParameterizedTest
    @CsvSource({
            "0, '1'",
            "1, '2'",
            "2, '3'",
            "3, '4'",
            "4, '5'",
            "5, '6'",
            "6, '7'",
            "7, '8'"
    })
    void getRowChar_shouldReturnCorrectChar(int row, char expectedChar) {
        Position position = new Position(row, 0); // Column doesn't matter for this test
        assertEquals(expectedChar, position.getRowChar(), "Row " + row + " should correspond to char '" + expectedChar + "'.");
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0, 'a1'",
            "7, 7, 'h8'",
            "3, 4, 'e4'",
            "5, 2, 'c6'"
    })
    void toString_shouldReturnCorrectAlgebraicNotation(int row, int col, String expectedString) {
        Position position = new Position(row, col);
        assertEquals(expectedString, position.toString(), "Position(" + row + "," + col + ") should produce string '" + expectedString + "'.");
    }
}