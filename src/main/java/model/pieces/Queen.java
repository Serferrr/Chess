package model.pieces;

import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.enums.PieceType;

import java.util.*;

public class Queen extends Piece {
    public Queen(ChessColor chessColor, Square square) {
        super(chessColor, PieceType.QUEEN, square);
    }
    @Override
    public List<Position> getValidMoves(Board board, Square square) {
        Collection<Position> validMoves = new HashSet<>();
        List<Position> bishopMoves = Bishop.getValidMovesStatic(board,square,this.chessColor);
        List<Position> RookMoves = Rook.getValidMovesStatic(board,square,this.chessColor);
        validMoves.addAll(bishopMoves);
        validMoves.addAll(RookMoves);
        return new ArrayList<>(validMoves);
    }

}