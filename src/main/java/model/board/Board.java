package model.board;

import model.pieces.*;
import model.enums.ChessColor;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the chessboard containing 64 squares.
 */
public class Board {
    private final Square[][] squares = new Square[8][8]; // Standard 8x8 board

    // Constructor could initialize squares and call setupBoard()
    public Board() {
      clearBoard();
    }

    public void setupBoard() {
        // Place white pieces
        squares[0][0].setPiece(new Rook(ChessColor.WHITE, squares[0][0]));
        squares[0][1].setPiece(new Knight(ChessColor.WHITE, squares[0][1]));
        squares[0][2].setPiece(new Bishop(ChessColor.WHITE, squares[0][2]));
        squares[0][3].setPiece(new Queen(ChessColor.WHITE, squares[0][3]));
        squares[0][4].setPiece(new King(ChessColor.WHITE, squares[0][4]));
        squares[0][5].setPiece(new Bishop(ChessColor.WHITE, squares[0][5]));
        squares[0][6].setPiece(new Knight(ChessColor.WHITE, squares[0][6]));
        squares[0][7].setPiece(new Rook(ChessColor.WHITE, squares[0][7]));
        for (int i = 0; i < 8; i++) {
            squares[1][i].setPiece(new Pawn(ChessColor.WHITE, squares[1][i]));
        }

        // Place black pieces
        squares[7][0].setPiece(new Rook(ChessColor.BLACK, squares[7][0]));
        squares[7][1].setPiece(new Knight(ChessColor.BLACK, squares[7][1]));
        squares[7][2].setPiece(new Bishop(ChessColor.BLACK, squares[7][2]));
        squares[7][3].setPiece(new Queen(ChessColor.BLACK, squares[7][3]));
        squares[7][4].setPiece(new King(ChessColor.BLACK, squares[7][4]));
        squares[7][5].setPiece(new Bishop(ChessColor.BLACK, squares[7][5]));
        squares[7][6].setPiece(new Knight(ChessColor.BLACK, squares[7][6]));
        squares[7][7].setPiece(new Rook(ChessColor.BLACK, squares[7][7]));
        for (int i = 0; i < 8; i++) {
            squares[6][i].setPiece(new Pawn(ChessColor.BLACK, squares[6][i]));
        }
        }


    public Piece getPieceAt(Position pos) {
        return getSquareAt(pos).getPiece();
    }

    public void setPieceAt(Piece piece, Position pos) {
        getSquareAt(pos).setPiece(piece);
    }

    public void movePiece(Position fromPos, Position toPos) {
        Square fromSquare = getSquareAt(fromPos);
        Square toSquare = getSquareAt(toPos);
        Piece movedPiece = fromSquare.getPiece();
        fromSquare.setPiece(null);
        toSquare.setPiece(movedPiece);
    }

    public boolean isEmpty(Position pos) {
        // Implementation needed (validate pos, check square's piece)
      return getSquareAt(pos).getPiece() == null;
    }

    // Optional helper method
     public boolean isPathClear(@NotNull Position fromPos, @NotNull Position toPos) {

        if (fromPos.equals(toPos)) {
            return false;
        }
        int rowDiff = toPos.getRow() - fromPos.getRow();
        int colDiff = toPos.getCol() - fromPos.getCol();
        int rowDir = Integer.signum(rowDiff);
        int colDir = Integer.signum(colDiff);
        int row = fromPos.getRow() + rowDir;
        int col = fromPos.getCol() + colDir;

        while (row != toPos.getRow() || col != toPos.getCol()) {
            if (!squares[row][col].isEmpty()) {
                return false;
            }
            row += rowDir;
            col += colDir;
        }
        return true;

     }

    // Helper to get a square object from a position
    public Square getSquareAt(@NotNull Position pos) {
        return squares[pos.getRow()][pos.getCol()];
    }

    public void clearBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col] = new Square(new Position(row, col), null);
            }
        }
    }
}
