package model.pieces;

import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.enums.PieceType;

import java.util.List;

/**
 * Abstract base class for all chess pieces.
 */
public abstract class Piece {
    protected ChessColor chessColor;
    protected PieceType type;
    protected Square currentSquare; // Reference back to its square

    protected Piece(ChessColor chessColor, PieceType type, Square currentSquare)
    {
        this.chessColor = chessColor;
        this.type = type;
        this.currentSquare = currentSquare;
    }

    public abstract List<Position> getValidMoves(Board board, Square square);
    public List<Position> getValidMoves(Board board) {
        return getValidMoves(board,currentSquare);
    }

    public ChessColor getColor()
    {
        return chessColor;
    }

    public PieceType getType() {
        return type;
    }

    public Square getCurrentSquare() {
        return currentSquare;
    }

    public void setCurrentSquare(Square square) {
        currentSquare = square;
    }

    public PieceType getPieceType() {
        return type;
    }

}