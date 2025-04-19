package model.board;

import java.util.Objects;

/**
 * Represents a position on the chessboard using row and column indices.
 * Row 0 is the bottom rank (rank 1 in algebraic notation).
 * Column 0 is the leftmost file ('a' file in algebraic notation).
 */
public class Position {
    private final int row; // 0-7
    private final int col; // 0-7

    public Position(int row, int col) {
        // Optional: Add validation for row/col bounds (0-7)
        if (row < 0 || row > 7 || col < 0 || col > 7) {
            throw new IllegalArgumentException("Invalid row or column index: row=" + row + ", col=" + col);
        }
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    /**
     * Gets the algebraic notation character for the column ('a' through 'h').
     * @return The character representing the column.
     */
    public char getColChar() {
        // 'a' corresponds to column 0
        return (char) ('a' + col);
    }

    /**
     * Gets the algebraic notation character for the row ('1' through '8').
     * @return The character representing the row.
     */
    public char getRowChar() {
        // Row 0 corresponds to rank '1'
        return (char) ('1' + row);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    /**
     * Returns the position in standard algebraic notation (e.g., "e4", "a1", "h8").
     * @return String representation of the position.
     */
    @Override
    public String toString() {
        return "" + getColChar() + getRowChar(); // e.g., "e" + "4" -> "e4"
    }
}