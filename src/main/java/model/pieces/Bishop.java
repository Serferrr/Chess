package model.pieces;

import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor;
import model.enums.PieceType;

import java.util.List;

public class Bishop extends Piece {
    public Bishop(ChessColor chessColor, Square square) {
        super(chessColor, PieceType.BISHOP, square);
    }

    @Override
    public List<Position> getValidMoves(Board board, Square square) {
        return getValidMovesStatic(board, square, this.chessColor);
    }
    public static List<Position> getValidMovesStatic(Board board, Square square, ChessColor chessColor) {
        // Implementation needed (diagonal movement)
        List<Position> validMoves = new java.util.ArrayList<>();
        int[] dx = {1, 1, -1, -1};
        int[] dy = {1, -1, 1, -1};

        for (int i = 0; i < 4; i++) {
            int x = square.getPosition().getRow() + dx[i];
            int y = square.getPosition().getCol() + dy[i];

            while (x >= 0 && x < 8 && y >= 0 && y < 8) {
                Square targetSquare = board.getSquareAt(new Position(x, y));
                if (!targetSquare.isEmpty()) {
                    if (targetSquare.getPiece().getColor() != chessColor) {
                        validMoves.add(new Position(x, y));
                    }
                    break;
                } else {
                    validMoves.add(new Position(x, y));
                }
                x += dx[i];
                y += dy[i];
            }
        }
        return validMoves;
    }

}