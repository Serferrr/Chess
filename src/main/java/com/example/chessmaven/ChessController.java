package com.example.chessmaven;

import javafx.collections.FXCollections; // Import necessary classes
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn; // Import TableColumn
import javafx.scene.control.TableView;   // Import TableView
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color; // Alias to avoid confusion with model.enums.Color
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import model.board.Board;
import model.board.Position;
import model.board.Square;
import model.enums.ChessColor; // Your model's Color enum
import model.enums.GameState; // Import GameState
import model.enums.PieceType;
import model.game.Move;
import model.game.ChessGame;
import model.pieces.Piece;

import java.util.List;

public class ChessController {

    @FXML
    private GridPane boardPane; // Injected from FXML

    @FXML
    private Label statusLabel; // Injected from FXML

    // --- TableView Fields ---
    @FXML
    private TableView<MoveHistoryEntry> moveHistoryTable;
    @FXML
    private TableColumn<MoveHistoryEntry, Number> moveNumberCol;
    @FXML
    private TableColumn<MoveHistoryEntry, String> whiteMoveCol;
    @FXML
    private TableColumn<MoveHistoryEntry, String> blackMoveCol;
    // ------------------------

    private ChessGame game;
    private Board board; // Convenience reference

    private static final double SQUARE_SIZE = 70.0; // Match FXML size
    private static final String LIGHT_COLOR = "#f0d9b5"; // Example light square color
    private static final String DARK_COLOR = "#b58863";  // Example dark square color

    // Observable list to back the TableView
    private final ObservableList<MoveHistoryEntry> moveHistoryData = FXCollections.observableArrayList();

    // Method to receive the ChessGame instance from the Application
    public void setGame(ChessGame game) {
        this.game = game;
        this.board = game.getBoard(); // Get the board from the game
    }

    // Called by HelloApplication after setting the game
    public void initializeBoard() {
        if (board == null) {
            System.err.println("Board is null in initializeBoard. Ensure setGame is called first.");
            return;
        }
        setupMoveHistoryTable(); // Setup table columns
        drawBoard();
        updateStatusLabel();
        updateMoveHistoryDisplay(); // Initial population (empty)
    }

    // Setup TableView columns
    private void setupMoveHistoryTable() {
        moveNumberCol.setCellValueFactory(cellData -> cellData.getValue().moveNumberProperty());
        whiteMoveCol.setCellValueFactory(cellData -> cellData.getValue().whiteMoveProperty());
        blackMoveCol.setCellValueFactory(cellData -> cellData.getValue().blackMoveProperty());

        // Set the items for the table
        moveHistoryTable.setItems(moveHistoryData);
    }

    private void drawBoard() {
        boardPane.getChildren().clear(); // Clear previous state if any

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                // Create the visual square (StackPane allows layering)
                StackPane squarePane = createSquarePane(row, col);

                // Get the piece from the logical board
                Position boardPos = new Position(7 - row, col); // Convert GUI row to board row (0=bottom)
                Piece piece = board.getPieceAt(boardPos);

                if (piece != null) {
                    // Add the piece representation to the square
                    squarePane.getChildren().add(createPieceText(piece));
                }

                // Add the square to the GridPane
                boardPane.add(squarePane, col, row); // Add to grid at (col, row)
            }
        }
    }

    // Creates the visual representation of a square (background color)
    private StackPane createSquarePane(int guiRow, int guiCol) {
        StackPane pane = new StackPane();
        pane.setPrefSize(SQUARE_SIZE, SQUARE_SIZE);

        // Set background color based on position
        boolean isLightSquare = (guiRow + guiCol) % 2 == 0;
        String color = isLightSquare ? LIGHT_COLOR : DARK_COLOR;
        pane.setStyle("-fx-background-color: " + color + ";");

        // --- Add Click Handler (Lambda for brevity) ---
        pane.setOnMouseClicked(event -> handleSquareClick(guiRow, guiCol));
        // -----------------------------------------

        return pane;
    }

    // Creates the visual representation of a piece (using Unicode)
    private Text createPieceText(Piece piece) {
        Text pieceText = new Text(getUnicodePiece(piece));
        pieceText.setFill(piece.getColor() == ChessColor.WHITE ? Color.WHITE : Color.BLACK);
        pieceText.setStroke(piece.getColor() == ChessColor.WHITE ? Color.BLACK : Color.WHITE);
        pieceText.setStrokeWidth(0.5);
        pieceText.setFont(Font.font("DejaVu Sans", SQUARE_SIZE * 0.7));
        return pieceText;
    }

    // Maps PieceType and Color to Unicode characters
    private String getUnicodePiece(Piece piece) {
        if (piece == null) return "";
        return switch (piece.getType()) {
            case KING -> piece.getColor() == ChessColor.WHITE ? "\u2654" : "\u265A"; // ♔♚
            case QUEEN -> piece.getColor() == ChessColor.WHITE ? "\u2655" : "\u265B"; // ♕♛
            case ROOK -> piece.getColor() == ChessColor.WHITE ? "\u2656" : "\u265C"; // ♖♜
            case BISHOP -> piece.getColor() == ChessColor.WHITE ? "\u2657" : "\u265D"; // ♗♝
            case KNIGHT -> piece.getColor() == ChessColor.WHITE ? "\u2658" : "\u265E"; // ♘♞
            case PAWN -> piece.getColor() == ChessColor.WHITE ? "\u2659" : "\u265F"; // ♙♟
        };
    }

    // --- Basic Click Handling Logic ---
    private Square selectedSquare = null;

    private void handleSquareClick(int guiRow, int guiCol) {
        Position clickedBoardPos = new Position(7 - guiRow, guiCol); // Convert to board coordinates
        Square clickedSquare = board.getSquareAt(clickedBoardPos);

        System.out.println("Clicked: " + clickedBoardPos + " (GUI: " + guiCol + "," + guiRow + ")"); // Debugging

        if (selectedSquare == null) {
            // First click: Select a piece
            Piece clickedPiece = clickedSquare.getPiece();
            if (clickedPiece != null && clickedPiece.getColor() == game.getCurrentPlayer().getColor()) {
                selectedSquare = clickedSquare;
                highlightSquare(selectedSquare, true); // Highlight selected square
                System.out.println("Selected piece at: " + selectedSquare.getPosition());
            } else {
                System.out.println("Invalid selection (empty or opponent's piece).");
            }
        } else {
            // Second click: Attempt to move
            Piece pieceToMove = selectedSquare.getPiece();
            Move potentialMove = createPotentialMove(selectedSquare, clickedSquare); // Create the move object

            if (potentialMove != null) {
                System.out.println("Attempting move: " + potentialMove);
                if (game.makeMove(potentialMove)) {
                    System.out.println("Move successful!");
                    // Move was successful, redraw the board and update status
                    drawBoard(); // Redraw entire board (simple approach)
                    updateStatusLabel();
                    updateMoveHistoryDisplay(); // Update the table
                } else {
                    System.out.println("Invalid move.");
                    // Optionally provide feedback to the user
                }
            } else {
                System.out.println("Could not create move object.");
            }

            // Reset selection regardless of move success/failure
            highlightSquare(selectedSquare, false); // Unhighlight
            selectedSquare = null;
        }
    }

    // Helper to create a Move object (needs promotion handling later)
    private Move createPotentialMove(Square start, Square end) {
        Piece piece = start.getPiece();
        if (piece == null) return null;

        Piece captured = end.getPiece();
        boolean isCastling = piece.getType() == PieceType.KING && Math.abs(start.getPosition().getCol() - end.getPosition().getCol()) == 2;
        boolean isEnPassant = piece.getType() == PieceType.PAWN && start.getPosition().getCol() != end.getPosition().getCol() && captured == null;
        // Basic promotion check (needs UI interaction later)
        PieceType promotionType = null;
        if (piece.getType() == PieceType.PAWN) {
            int endRow = end.getPosition().getRow();
            if ((piece.getColor() == ChessColor.WHITE && endRow == 7) || (piece.getColor() == ChessColor.BLACK && endRow == 0)) {
                promotionType = PieceType.QUEEN; // Default to Queen for now
                System.out.println("Promotion detected (defaulting to Queen)");
            }
        }

        return new Move(start, end, piece, captured, promotionType, isCastling, isEnPassant);
    }


    // Basic highlighting (can be improved)
    private void highlightSquare(Square square, boolean highlight) {
        if (square == null) return;
        int guiRow = 7 - square.getPosition().getRow();
        int guiCol = square.getPosition().getCol();

        // Find the StackPane in the GridPane
        boardPane.getChildren().stream()
                .filter(node -> GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) // Ensure indices are not null
                .filter(node -> GridPane.getRowIndex(node) == guiRow && GridPane.getColumnIndex(node) == guiCol)
                .findFirst()
                .ifPresent(node -> {
                    // Combine styles carefully
                    String baseStyle = "-fx-background-color: " + ((guiRow + guiCol) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR) + ";";
                    String borderStyle = highlight ? "-fx-border-color: yellow; -fx-border-width: 3;" : "";
                    node.setStyle(baseStyle + borderStyle);
                });
    }

    private void updateStatusLabel() {
        String statusText = switch (game.getGameState()) {
            case ONGOING -> game.getCurrentPlayer().getColor() + "'s Turn";
            case CHECK -> game.getCurrentPlayer().getColor() + " is in Check!";
            case CHECKMATE_WHITE_WINS -> "Checkmate! White Wins!";
            case CHECKMATE_BLACK_WINS -> "Checkmate! Black Wins!";
            case STALEMATE -> "Stalemate! Draw.";
            // Handle other potential draw states if added to GameState enum
            default -> game.getGameState().toString(); // Fallback
        };
        statusLabel.setText(statusText);
    }

    // Update the TableView based on the game's move history
    private void updateMoveHistoryDisplay() {
        moveHistoryData.clear(); // Clear existing data
        List<Move> history = game.getMoveHistory();
        MoveHistoryEntry currentEntry = null;

        for (int i = 0; i < history.size(); i++) {
            Move move = history.get(i);
            String moveString = move.toString(); // Use the Move's toString() for notation

            if (i % 2 == 0) { // White's move (index 0, 2, 4...)
                int moveNumber = (i / 2) + 1;
                currentEntry = new MoveHistoryEntry(moveNumber, moveString, ""); // Create new row for White's move
                moveHistoryData.add(currentEntry);
            } else { // Black's move (index 1, 3, 5...)
                if (currentEntry != null) {
                    currentEntry.setBlackMove(moveString); // Update the existing row with Black's move
                } else {
                    // Should not happen in a normal game sequence, but handle defensively
                    System.err.println("Error updating move history: Black move without preceding White move.");
                }
            }
        }
        // Scroll to the last move
        if (!moveHistoryData.isEmpty()) {
            moveHistoryTable.scrollTo(moveHistoryData.size() - 1);
        }
    }
}