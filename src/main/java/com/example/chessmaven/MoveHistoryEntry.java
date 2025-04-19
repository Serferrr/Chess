package com.example.chessmaven;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

// Simple class to represent a row in the move history table
public class MoveHistoryEntry {
    private final SimpleIntegerProperty moveNumber;
    private final SimpleStringProperty whiteMove;
    private final SimpleStringProperty blackMove;

    public MoveHistoryEntry(int moveNumber, String whiteMove, String blackMove) {
        this.moveNumber = new SimpleIntegerProperty(moveNumber);
        this.whiteMove = new SimpleStringProperty(whiteMove);
        this.blackMove = new SimpleStringProperty(blackMove);
    }

    public int getMoveNumber() {
        return moveNumber.get();
    }

    public SimpleIntegerProperty moveNumberProperty() {
        return moveNumber;
    }

    public String getWhiteMove() {
        return whiteMove.get();
    }

    public SimpleStringProperty whiteMoveProperty() {
        return whiteMove;
    }

    public String getBlackMove() {
        return blackMove.get();
    }

    public SimpleStringProperty blackMoveProperty() {
        return blackMove;
    }

    // Setter for black move, needed when updating an existing row
    public void setBlackMove(String blackMove) {
        this.blackMove.set(blackMove);
    }
}