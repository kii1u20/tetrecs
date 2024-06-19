package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.event.PieceChangeListener;
import uk.ac.soton.comp1206.game.GamePiece;

/**
 * A class representing a game board for showing the current and next piece
 */
public class PieceBoard extends GameBoard implements PieceChangeListener {

    //Whether to paint the centre guide circle
    private boolean showCircleGuide = false;

    /**
        * @param cols the number of columns for this piece board
        * @param rows the number of rows for this piece board
        * @param width the width of this piece board
        * @param height the height of this piece board
    */
    public PieceBoard(int cols, int rows, double width, double height) {
        super(cols, rows, width, height);
    }

    /**
     * Draw a game piece on the piece board
     * @param piece the piece to be drawn
     */
    public void displayPiece(GamePiece piece) {
        cleanBoard();
        int[][] pieceBlocks = piece.getBlocks();
        
        for (int i = 0; i < pieceBlocks.length; i++) {
            for (int j = 0; j < pieceBlocks[i].length; j++) {
                if (pieceBlocks[i][j] != 0) {
                    grid.set(i, j, piece.getValue());
                }
            }
        }
        if (showCircleGuide == true) {
            getBlock(1, 1).paintCentreCircle(); //Paint the centre guide circle
        }
    }

    /**
     * Set the centre circle to be shown
     */
    public void showCircleGuide() {
        showCircleGuide = true;
    }

    /**
     * Clear the piece board
     */
    public void cleanBoard() {
        for (int i = 0; i < grid.getCols(); i++) {
            for (int j = 0; j < grid.getRows(); j++) {
                grid.set(i, j, 0);
            }
        }
    }

    /**
     * Update the game piece being displayed
     * @param piece the piece to be displayed
     */
    @Override
    public void updatePiece(GamePiece piece) {
        displayPiece(piece);
    }
}
