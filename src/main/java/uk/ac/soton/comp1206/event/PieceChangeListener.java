package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The piece change listener is used to listen for changes to the current and next piece
 */
public interface PieceChangeListener {
    /**
     * Handle when a new piece gets generated and get it displayed on the piece board
     * @param piece the new piece to be displayed
     */
    public void updatePiece(GamePiece piece);
}
