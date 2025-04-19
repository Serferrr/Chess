package piecesTest;

import model.board.Board;
import model.board.Position;
import model.enums.ChessColor;
import model.pieces.King;
import model.pieces.Pawn; // Assuming you have a Pawn class or similar
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KingTest {

    private Board board;
    private King whiteKing;

    @BeforeEach
    void setUp() {
        // Initialize a standard 8x8 board before each test
        board = new Board();
    }

    // Helper method to place the king and get its valid moves
    private List<Position> getKingMoves(int row, int col, ChessColor chessColor) {
        Position kingPos = new Position(row, col);
        King king = new King(chessColor, board.getSquareAt(kingPos));
        board.getSquareAt(kingPos).setPiece(king);
        return king.getValidMoves(board, board.getSquareAt(kingPos));
    }

    // Helper to convert row/col pairs to a List of Positions
    private List<Position> createPositionList(int... coords) {
        List<Position> positions = new java.util.ArrayList<>();
        for (int i = 0; i < coords.length; i += 2) {
            positions.add(new Position(coords[i], coords[i + 1]));
        }
        return positions;
    }

    @Test
    void testKingMoves_CenterEmptyBoard() {
        List<Position> moves = getKingMoves(3, 3, ChessColor.WHITE); // King at d4
        List<Position> expectedMoves = createPositionList(
                2, 2, 2, 3, 2, 4, // c3, d3, e3
                3, 2,       3, 4, // c4,      e4
                4, 2, 4, 3, 4, 4  // c5, d5, e5
        );

        assertEquals(8, moves.size());
        assertTrue(moves.containsAll(expectedMoves));
        assertTrue(expectedMoves.containsAll(moves)); // Ensure no extra moves
    }

    @Test
    void testKingMoves_CornerA1EmptyBoard() {
        List<Position> moves = getKingMoves(0, 0, ChessColor.WHITE); // King at a1
        List<Position> expectedMoves = createPositionList(
                0, 1, // b1
                1, 0, 1, 1 // a2, b2
        );

        assertEquals(3, moves.size());
        assertTrue(moves.containsAll(expectedMoves));
        assertTrue(expectedMoves.containsAll(moves));
    }

    @Test
    void testKingMoves_CornerH8EmptyBoard() {
        List<Position> moves = getKingMoves(7, 7, ChessColor.BLACK); // King at h8
        List<Position> expectedMoves = createPositionList(
                6, 6, 6, 7, // g7, h7
                7, 6       // g8
        );

        assertEquals(3, moves.size());
        assertTrue(moves.containsAll(expectedMoves));
        assertTrue(expectedMoves.containsAll(moves));
    }

    @Test
    void testKingMoves_EdgeA4EmptyBoard() {
        List<Position> moves = getKingMoves(3, 0, ChessColor.WHITE); // King at a4
        List<Position> expectedMoves = createPositionList(
                2, 0, 2, 1, // a3, b3
                3, 1,       // b4
                4, 0, 4, 1  // a5, b5
        );

        assertEquals(5, moves.size());
        assertTrue(moves.containsAll(expectedMoves));
        assertTrue(expectedMoves.containsAll(moves));
    }

    @Test
    void testKingMoves_SurroundedByFriends() {
        // Place King at d4
        Position kingPos = new Position(3, 3);
        whiteKing = new King(ChessColor.WHITE, board.getSquareAt(kingPos));
        board.getSquareAt(kingPos).setPiece(whiteKing);

        // Place friendly pawns around the king
        int[] rowOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colOffsets = {-1, 0, 1, -1, 1, -1, 0, 1};
        for (int i = 0; i < 8; i++) {
            Position friendPos = new Position(kingPos.getRow() + rowOffsets[i], kingPos.getCol() + colOffsets[i]);
            board.getSquareAt(friendPos).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(friendPos))); // Assuming Pawn class
        }

        List<Position> moves = whiteKing.getValidMoves(board, board.getSquareAt(kingPos));

        assertEquals(0, moves.size(), "King should have no moves when surrounded by friendly pieces.");
    }

    @Test
    void testKingMoves_SurroundedByEnemies() {
        // Place King at d4
        Position kingPos = new Position(3, 3);
        whiteKing = new King(ChessColor.WHITE, board.getSquareAt(kingPos));
        board.getSquareAt(kingPos).setPiece(whiteKing);

        // Place enemy pawns around the king
        int[] rowOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colOffsets = {-1, 0, 1, -1, 1, -1, 0, 1};
        List<Position> expectedEnemyPositions = new java.util.ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Position enemyPos = new Position(kingPos.getRow() + rowOffsets[i], kingPos.getCol() + colOffsets[i]);
            board.getSquareAt(enemyPos).setPiece(new Pawn(ChessColor.BLACK, board.getSquareAt(enemyPos))); // Assuming Pawn class
            expectedEnemyPositions.add(enemyPos);
        }

        List<Position> moves = whiteKing.getValidMoves(board, board.getSquareAt(kingPos));

        assertEquals(8, moves.size(), "King should be able to move to all adjacent squares occupied by enemies.");
        assertTrue(moves.containsAll(expectedEnemyPositions));
        assertTrue(expectedEnemyPositions.containsAll(moves));
    }

    @Test
    void testKingMoves_MixedSurroundings() {
        // Place King at e4
        Position kingPos = new Position(3, 4);
        whiteKing = new King(ChessColor.WHITE, board.getSquareAt(kingPos));
        board.getSquareAt(kingPos).setPiece(whiteKing);

        // Friendly pieces
        board.getSquareAt(new Position(2, 4)).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(new Position(2, 4)))); // e3
        board.getSquareAt(new Position(3, 3)).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(new Position(3, 3)))); // d4
        board.getSquareAt(new Position(4, 5)).setPiece(new Pawn(ChessColor.WHITE, board.getSquareAt(new Position(4, 5)))); // f5

        // Enemy pieces
        board.getSquareAt(new Position(2, 3)).setPiece(new Pawn(ChessColor.BLACK, board.getSquareAt(new Position(2, 3)))); // d3
        board.getSquareAt(new Position(4, 4)).setPiece(new Pawn(ChessColor.BLACK, board.getSquareAt(new Position(4, 4)))); // e5

        List<Position> moves = whiteKing.getValidMoves(board, board.getSquareAt(kingPos));
        List<Position> expectedMoves = createPositionList(
                2, 3, // d3 (capture)
                2, 5, // f3 (empty)
                3, 5, // f4 (empty)
                4, 3, // d5 (empty)
                4, 4  // e5 (capture)
        );

        assertEquals(5, moves.size());
        assertTrue(moves.containsAll(expectedMoves));
        assertTrue(expectedMoves.containsAll(moves));
    }
}