package model.enums;

/**
 * Represents the possible states of the chess game.
 */
public enum GameState {
    ONGOING,
    CHECK,
    CHECKMATE_WHITE_WINS,
    CHECKMATE_BLACK_WINS,
    STALEMATE,
    DRAW_AGREED,
    DRAW_50_MOVE,
    DRAW_REPETITION
}