package boardTest;

import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.enums.PieceType;
import model.pieces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        // Create a new board before each test
        board = new Board();
    }

    @Test
    @DisplayName("Constructor should initialize all 64 squares as empty")
    void testBoardInitialization() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position currentPos = new Position(row, col);
                Square square = board.getSquareAt(currentPos);
                assertNotNull(square, "Square at " + currentPos + " should not be null");
                assertEquals(currentPos, square.getPosition(), "Square position should match");
                assertNull(square.getPiece(), "Square at " + currentPos + " should initially be empty");
                assertTrue(board.isEmpty(currentPos), "Board should report square " + currentPos + " as empty");
            }
        }
    }

    @Nested
    @DisplayName("After setupBoard()")
    class AfterSetup {

        @BeforeEach
        void setupTheBoard() {
            board.setupBoard();
        }

        @Test
        @DisplayName("Should place pieces in standard starting positions")
        void testSetupBoardPositions() {
            // --- White Pieces ---
            // Back Rank
            assertPiece(new Position(0, 0), ChessColor.WHITE, PieceType.ROOK);
            assertPiece(new Position(0, 1), ChessColor.WHITE, PieceType.KNIGHT);
            assertPiece(new Position(0, 2), ChessColor.WHITE, PieceType.BISHOP);
            assertPiece(new Position(0, 3), ChessColor.WHITE, PieceType.QUEEN);
            assertPiece(new Position(0, 4), ChessColor.WHITE, PieceType.KING);
            assertPiece(new Position(0, 5), ChessColor.WHITE, PieceType.BISHOP);
            assertPiece(new Position(0, 6), ChessColor.WHITE, PieceType.KNIGHT);
            assertPiece(new Position(0, 7), ChessColor.WHITE, PieceType.ROOK);
            // Pawns
            for (int col = 0; col < 8; col++) {
                assertPiece(new Position(1, col), ChessColor.WHITE, PieceType.PAWN);
            }

            // --- Black Pieces ---
            // Back Rank
            assertPiece(new Position(7, 0), ChessColor.BLACK, PieceType.ROOK);
            assertPiece(new Position(7, 1), ChessColor.BLACK, PieceType.KNIGHT);
            assertPiece(new Position(7, 2), ChessColor.BLACK, PieceType.BISHOP);
            assertPiece(new Position(7, 3), ChessColor.BLACK, PieceType.QUEEN);
            assertPiece(new Position(7, 4), ChessColor.BLACK, PieceType.KING);
            assertPiece(new Position(7, 5), ChessColor.BLACK, PieceType.BISHOP);
            assertPiece(new Position(7, 6), ChessColor.BLACK, PieceType.KNIGHT);
            assertPiece(new Position(7, 7), ChessColor.BLACK, PieceType.ROOK);
            // Pawns
            for (int col = 0; col < 8; col++) {
                assertPiece(new Position(6, col), ChessColor.BLACK, PieceType.PAWN);
            }

            // --- Empty Squares ---
            for (int row = 2; row < 6; row++) {
                for (int col = 0; col < 8; col++) {
                    Position emptyPos = new Position(row, col);
                    assertNull(board.getPieceAt(emptyPos), "Square " + emptyPos + " should be empty after setup");
                    assertTrue(board.isEmpty(emptyPos), "Board should report square " + emptyPos + " as empty");
                }
            }
        }

        @Test
        @DisplayName("getPieceAt should return correct piece or null")
        void testGetPieceAt() {
            Piece whiteKing = board.getPieceAt(new Position(0, 4));
            assertNotNull(whiteKing);
            assertEquals(ChessColor.WHITE, whiteKing.getColor());
            assertEquals(PieceType.KING, whiteKing.getType());

            Piece blackPawn = board.getPieceAt(new Position(6, 3));
            assertNotNull(blackPawn);
            assertEquals(ChessColor.BLACK, blackPawn.getColor());
            assertEquals(PieceType.PAWN, blackPawn.getType());

            Piece emptySquare = board.getPieceAt(new Position(3, 3));
            assertNull(emptySquare);
        }

        @Test
        @DisplayName("isEmpty should return false for occupied squares and true for empty ones")
        void testIsEmpty() {
            assertFalse(board.isEmpty(new Position(0, 0)), "White Rook position should not be empty");
            assertFalse(board.isEmpty(new Position(6, 5)), "Black Pawn position should not be empty");
            assertTrue(board.isEmpty(new Position(4, 4)), "Center square should be empty");
        }

        @Test
        @DisplayName("movePiece should update source and destination squares")
        void testMovePiece() {
            Position fromPos = new Position(1, 4); // White e-pawn
            Position toPos = new Position(3, 4);   // Move two squares forward
            Piece pawn = board.getPieceAt(fromPos);

            assertNotNull(pawn);
            assertTrue(board.isEmpty(toPos));

            board.movePiece(fromPos, toPos);

            assertNull(board.getPieceAt(fromPos), "Original square should be empty after move");
            assertTrue(board.isEmpty(fromPos), "Original square should report as empty after move");

            Piece movedPawn = board.getPieceAt(toPos);
            assertNotNull(movedPawn, "Destination square should have the piece after move");
            assertFalse(board.isEmpty(toPos), "Destination square should report as not empty");
            assertEquals(pawn, movedPawn, "The piece on the destination square should be the same piece that moved");
            // Optional: Check if the piece's internal position was updated (if Piece stores its Square)
            // assertEquals(board.getSquareAt(toPos), movedPawn.getCurrentSquare());
        }
    }

    @Test
    @DisplayName("setPieceAt should place a piece correctly")
    void testSetPieceAt() {
        Position pos = new Position(3, 3);
        assertTrue(board.isEmpty(pos));

        Piece testKnight = new Knight(ChessColor.BLACK, board.getSquareAt(pos)); // Knight needs a square initially
        board.setPieceAt(testKnight, pos); // Place the knight

        Piece retrievedPiece = board.getPieceAt(pos);
        assertNotNull(retrievedPiece);
        assertEquals(testKnight, retrievedPiece);
        assertEquals(ChessColor.BLACK, retrievedPiece.getColor());
        assertEquals(PieceType.KNIGHT, retrievedPiece.getType());
        assertFalse(board.isEmpty(pos));

        // Test setting null (clearing the square)
        board.setPieceAt(null, pos);
        assertNull(board.getPieceAt(pos));
        assertTrue(board.isEmpty(pos));
    }


    @Test
    @DisplayName("getSquareAt should return the correct Square object")
    void testGetSquareAt() {
        Position pos = new Position(5, 6);
        Square square = board.getSquareAt(pos);
        assertNotNull(square);
        assertEquals(pos, square.getPosition());
        assertEquals(5, square.getPosition().getRow());
        assertEquals(6, square.getPosition().getCol());
    }

    @Nested
    @DisplayName("isPathClear tests")
    class PathClearance {

        @Test
        @DisplayName("Should return false for same start and end position")
        void testPathClearSamePosition() {
            Position pos = new Position(3, 3);
            assertFalse(board.isPathClear(pos, pos));
        }

        @Test
        @DisplayName("Should return true for clear horizontal path")
        void testPathClearHorizontalClear() {
            Position from = new Position(3, 1);
            Position to = new Position(3, 6);
            assertTrue(board.isPathClear(from, to));
            assertTrue(board.isPathClear(to, from)); // Test reverse direction
        }

        @Test
        @DisplayName("Should return false for blocked horizontal path")
        void testPathClearHorizontalBlocked() {
            Position from = new Position(3, 1);
            Position to = new Position(3, 6);
            Position blockerPos = new Position(3, 4);
            board.setPieceAt(new Pawn(ChessColor.WHITE, board.getSquareAt(blockerPos)), blockerPos);

            assertFalse(board.isPathClear(from, to));
            assertFalse(board.isPathClear(to, from));
        }

        @Test
        @DisplayName("Should return true for clear vertical path")
        void testPathClearVerticalClear() {
            Position from = new Position(1, 4);
            Position to = new Position(6, 4);
            assertTrue(board.isPathClear(from, to));
            assertTrue(board.isPathClear(to, from));
        }

        @Test
        @DisplayName("Should return false for blocked vertical path")
        void testPathClearVerticalBlocked() {
            Position from = new Position(1, 4);
            Position to = new Position(6, 4);
            Position blockerPos = new Position(3, 4);
            board.setPieceAt(new Pawn(ChessColor.BLACK, board.getSquareAt(blockerPos)), blockerPos);

            assertFalse(board.isPathClear(from, to));
            assertFalse(board.isPathClear(to, from));
        }

        @Test
        @DisplayName("Should return true for clear diagonal path")
        void testPathClearDiagonalClear() {
            Position from = new Position(1, 1);
            Position to = new Position(5, 5); // Main diagonal
            assertTrue(board.isPathClear(from, to));
            assertTrue(board.isPathClear(to, from));

            Position from2 = new Position(1, 6);
            Position to2 = new Position(6, 1); // Anti-diagonal
            assertTrue(board.isPathClear(from2, to2));
            assertTrue(board.isPathClear(to2, from2));
        }

        @Test
        @DisplayName("Should return false for blocked diagonal path")
        void testPathClearDiagonalBlocked() {
            Position from = new Position(1, 1);
            Position to = new Position(5, 5);
            Position blockerPos = new Position(3, 3);
            board.setPieceAt(new Bishop(ChessColor.WHITE, board.getSquareAt(blockerPos)), blockerPos);

            assertFalse(board.isPathClear(from, to));
            assertFalse(board.isPathClear(to, from));

            Position from2 = new Position(1, 6);
            Position to2 = new Position(6, 1);
            Position blockerPos2 = new Position(4, 3);
            board.setPieceAt(new Bishop(ChessColor.BLACK, board.getSquareAt(blockerPos2)), blockerPos2);

            assertFalse(board.isPathClear(from2, to2));
            assertFalse(board.isPathClear(to2, from2));
        }

        @Test
        @DisplayName("Should return true for adjacent squares (path is trivially clear)")
        void testPathClearAdjacent() {
            Position from = new Position(3, 3);
            assertTrue(board.isPathClear(from, new Position(3, 4))); // Horizontal
            assertTrue(board.isPathClear(from, new Position(4, 3))); // Vertical
            assertTrue(board.isPathClear(from, new Position(4, 4))); // Diagonal
            assertTrue(board.isPathClear(from, new Position(2, 2))); // Diagonal
        }

    }

    // Helper assertion method
    private void assertPiece(Position pos, ChessColor expectedChessColor, PieceType expectedType) {
        Piece piece = board.getPieceAt(pos);
        assertNotNull(piece, "Piece should exist at " + pos);
        assertEquals(expectedChessColor, piece.getColor(), "Piece color mismatch at " + pos);
        assertEquals(expectedType, piece.getType(), "Piece type mismatch at " + pos);
        assertFalse(board.isEmpty(pos), "Board should report square " + pos + " as not empty");
    }
}