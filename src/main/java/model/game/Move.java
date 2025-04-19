package model.game;

import model.board.Square;
import model.enums.PieceType;
import model.pieces.Piece;

import java.util.Objects; // Import for Objects.hash

public class Move {
    private final Square startSquare;
    private final Square endSquare;
    private final Piece pieceMoved;
    private final Piece pieceCaptured; // Can be null, final after construction
    private final PieceType promotionPieceType; // Type promoted to, null if not promotion

    // Flags for special moves, final after construction
    private final boolean isCastling;
    private final boolean isEnPassant;

    /**
     * Constructor for a standard move (no capture, no special flags).
     */
    public Move(Square startSquare, Square endSquare, Piece pieceMoved) {
        this(startSquare, endSquare, pieceMoved, null, null, false, false);
    }

    /**
     * Constructor for a move with a potential capture.
     */
    public Move(Square startSquare, Square endSquare, Piece pieceMoved, Piece pieceCaptured) {
        this(startSquare, endSquare, pieceMoved, pieceCaptured, null, false, false);
    }

    /**
     * Full constructor allowing all fields to be set.
     * Use this for special moves like castling, en passant, and promotion.
     */
    public Move(Square startSquare, Square endSquare, Piece pieceMoved, Piece pieceCaptured,
                PieceType promotionPieceType, boolean isCastling, boolean isEnPassant) {
        this.startSquare = Objects.requireNonNull(startSquare, "Start square cannot be null");
        this.endSquare = Objects.requireNonNull(endSquare, "End square cannot be null");
        this.pieceMoved = Objects.requireNonNull(pieceMoved, "Moved piece cannot be null");
        this.pieceCaptured = pieceCaptured; // Can be null
        this.promotionPieceType = promotionPieceType; // Can be null
        this.isCastling = isCastling;
        this.isEnPassant = isEnPassant;

        // Basic validation: Cannot be both castling and en passant, or promotion and castling/en passant
        if (isCastling && (isEnPassant || promotionPieceType != null)) {
            throw new IllegalArgumentException("Castling move cannot be en passant or promotion.");
        }
        if (isEnPassant && promotionPieceType != null) {
            throw new IllegalArgumentException("En passant move cannot be a promotion.");
        }
        if (promotionPieceType != null && pieceMoved.getType() != PieceType.PAWN) {
            throw new IllegalArgumentException("Only pawns can be promoted.");
        }
    }

    // --- Getters ---

    public Square getStartSquare() {
        return startSquare;
    }

    public Square getEndSquare() {
        return endSquare;
    }

    public Piece getPieceMoved() {
        return pieceMoved;
    }

    public Piece getPieceCaptured() {
        return pieceCaptured;
    }

    public boolean isCastling() {
        return isCastling;
    }

    public boolean isEnPassant() {
        return isEnPassant;
    }

    public PieceType getPromotionPieceType() {
        return promotionPieceType;
    }

    public boolean isPromotion() {
        return promotionPieceType != null;
    }

    public boolean isCapture() {
        return pieceCaptured != null;
    }

    // --- Removed Setters to promote immutability ---
    // public void setPieceCaptured(Piece piece) { pieceCaptured = piece; }
    // public void setCastling(boolean castling) { isCastling = castling; }
    // public void setEnPassant(boolean enPassant) { isEnPassant = enPassant; }

    // --- Standard Object Methods ---

    @Override
    public String toString() {
        // Basic algebraic notation style (can be enhanced)
        StringBuilder sb = new StringBuilder();
        if (isCastling) {
            // Kingside (O-O) or Queenside (O-O-O)
            sb.append(endSquare.getPosition().getCol() > startSquare.getPosition().getCol() ? "O-O" : "O-O-O");
        } else {
            // Piece notation (omit for pawn unless capturing)
            if (pieceMoved.getType() != PieceType.PAWN) {
                // Use a standard character representation if available, otherwise use type name
                sb.append(getPieceChar(pieceMoved.getType()));
            }
            // Optional: Add disambiguation if needed (e.g., Rdf1) - complex to implement fully
            // sb.append(startSquare.getPosition().toString()); // Simple disambiguation

            if (isCapture()) {
                if (pieceMoved.getType() == PieceType.PAWN) {
                    // Pawn captures include start file
                    sb.append(startSquare.getPosition().getColChar());
                }
                sb.append("x");
            }

            sb.append(endSquare.getPosition().toString()); // e.g., e4

            if (isPromotion()) {
                sb.append("=").append(getPieceChar(promotionPieceType));
            }

            // TODO: Add check (+) or checkmate (#) indication (requires game state context)
        }
        return sb.toString();
    }

    // Helper for toString
    private char getPieceChar(PieceType type) {
        return switch (type) {
            case KING -> 'K';
            case QUEEN -> 'Q';
            case ROOK -> 'R';
            case BISHOP -> 'B';
            case KNIGHT -> 'N';
            case PAWN -> 'P'; // Usually omitted, but useful here
        };
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        // Compare all relevant fields for equality
        return isCastling == move.isCastling &&
                isEnPassant == move.isEnPassant &&
                Objects.equals(startSquare, move.startSquare) &&
                Objects.equals(endSquare, move.endSquare) &&
                Objects.equals(pieceMoved, move.pieceMoved) &&
                Objects.equals(pieceCaptured, move.pieceCaptured) && // Important for comparing captures
                promotionPieceType == move.promotionPieceType;
    }

    @Override
    public int hashCode() {
        // Generate hash code based on the same fields used in equals
        return Objects.hash(startSquare, endSquare, pieceMoved, pieceCaptured,
                promotionPieceType, isCastling, isEnPassant);
    }
}