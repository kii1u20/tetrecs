package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.web("#31CE7D"), //1
            Color.web("#FF0006"), //2
            Color.web("#FF6400"), //3
            Color.web("#FFF300"), //4
            Color.web("#CE3166"), //5
            Color.web("#9DFF00"), //6
            Color.web("#D500D4"), //7
            Color.web("#1165FF"), //8
            Color.web("#1EE1E1"), //9
            Color.web("#00BFFF"), //10
            Color.web("#1670E9"), //11
            Color.web("#00FF96"), //12
            Color.web("#0300FF"), //13
            Color.web("#EA5AFF"), //14
            Color.web("#CB10DE")  //15
    };

    /**
     * The set of compliment colours for different pieces
     */

    public static final Color[] COMPLIMENT_COLOURS = {
            Color.TRANSPARENT,
            Color.web("#CE3182"), //1
            Color.web("#00FFF9"), //2 
            Color.web("#009BFF"), //3 
            Color.web("#000CFF"), //4 
            Color.web("#31CE99"), //5 
            Color.web("#6200FF"), //6 
            Color.web("#00D501"), //7
            Color.web("#FFAB11"), //8
            Color.web("#E11E1E"), //9
            Color.web("#FF4000"), //10
            Color.web("#E98F16"), //11
            Color.web("#FF0069"), //12
            Color.web("#FCFF00"), //13
            Color.web("#6FFF5A"), //14
            Color.web("#23DE10")  //15
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;
    
    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Whether this block is the center block of the game board
     */
    private boolean centre = false;

    /**
     * The colour that the block is painted. This relates to the set of colours.
     */
    private Integer colour = null;

    private AnimationTimer timer;

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(value.get());
        }
        if (centre == true) {
            paintCentreCircle();
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        this.colour = null;

        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillRect(0, 0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint a circle in the centre of the game block
     */
    public void paintCentreCircle() {
        var gc = getGraphicsContext2D();
        gc.setFill(Color.rgb(1, 1, 1, 0.5));
        gc.fillOval(width/4, height/4, width/2, height/2);
    }

    /**
     * Paint the hovering effect over the game block
     */
    public void paintHover() {
        var gc = getGraphicsContext2D();
        gc.setFill(Color.rgb(1, 55, 1, 0.5));
        gc.fillRect(0.0, 0.0, width, height);
    }

    /**
     * Unpaint the hovering effect over the game block
     */
    public void unpaintHover() {
        if (colour != null) {
            paintColor(colour);
        } else paintEmpty();
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(int colour) {
        logger.info("Painting " + this.toString() + colour);
        this.colour = colour;

        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.REFLECT, new Stop(0.0, COLOURS[colour]), new Stop(1.0, COMPLIMENT_COLOURS[colour]));
        LinearGradient gradient2 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.REFLECT, new Stop(0.0, COMPLIMENT_COLOURS[colour]), new Stop(1.0, COLOURS[colour]));

        //Paint the big rectangle
        gc.setFill(gradient);
        gc.fillRect(0, 0, width, height);

        //Paint the line across the rectangles 
        gc.setStroke(gradient2);
        gc.setLineWidth(5);
        gc.strokeLine(0, 0, width, height);

        //Paint the middle rectangle
        gc.setFill(gradient2);
        gc.fillRect(width/4, height/4, width/2, height/2);

        //Border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * Play the animation on this game block when a line is cleared
     */
    public void fadeOut() {
        logger.info("Starting line clearing animation");

        var gc = getGraphicsContext2D();
        
        //Create a new animation timer and play the fade animation
        timer = new AnimationTimer(){
            double animationOpacity = 1;
            @Override 
            public void handle(long now) {
                fadeOut();
            }
            
            private void fadeOut() {
                paintEmpty();
                animationOpacity -= 0.02D;
                if (animationOpacity <= 0.0D) {
                    stop();
                    return;
                } 
                gc.setFill((Paint)Color.color(0.0D, 1.0D, 0.0D, animationOpacity));
                gc.fillRect(0.0D, 0.0D, GameBlock.this.width, GameBlock.this.height);
            }
        };
        timer.start();
    }
}
