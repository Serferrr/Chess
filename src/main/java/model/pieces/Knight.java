package model.pieces;

import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.enums.PieceType;

import java.util.List;

public class Knight extends Piece {
    public Knight(ChessColor chessColor, Square square) {
        super(chessColor, PieceType.KNIGHT, square);
    }

    @Override
    public List<Position> getValidMoves(Board board, Square square) {


        List<Position> validMoves = new java.util.ArrayList<>();
        int[][] possibleMoves = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        for (int[] move : possibleMoves) {
            int newRow = square.getPosition().getRow() + move[0];
            int newCol = square.getPosition().getCol() + move[1];

            if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
                Position newPosition = new Position(newRow, newCol);
                Square targetSquare = board.getSquareAt(newPosition);
                if (targetSquare.isEmpty() || targetSquare.getPiece().getColor() != this.getColor()) {
                    validMoves.add(newPosition);
                }
            }
        }
        return validMoves;
    }

}
