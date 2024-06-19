package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.PieceChangeListener;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    private GamePiece currentPiece;
    protected GamePiece nextPiece;

    private SimpleIntegerProperty score = new SimpleIntegerProperty(0);
    private SimpleIntegerProperty level = new SimpleIntegerProperty(0);
    private SimpleIntegerProperty lives = new SimpleIntegerProperty(3);
    private SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);
    private SimpleIntegerProperty highScore = new SimpleIntegerProperty();

    protected PieceChangeListener currentPieceListener;
    protected PieceChangeListener nextPieceListener;
    private LineClearedListener lineClearedListener;

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> gameLoopJob;

    private GameLoopListener gameLoopListener;

    private SimpleBooleanProperty gameOver = new SimpleBooleanProperty(false);

    private boolean multiplayer = false;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);

        if (this instanceof MultiplayerGame) {
            multiplayer = true;
        }
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        gameOver.set(false);
        gameLoopListener.gameLoop(getTimerDelay());
        gameLoopJob = scheduler.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        currentPiece = spawnPiece();
        nextPiece();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        if (placeBlock(gameBlock.getX(), gameBlock.getY())) {
            gameBlock.paintHover();
        }
    }
    
    /**
     * Place the game block on a specified x and y of the grid
     * @param x the x coordinate to place the game block at
     * @param y the y coordinate to place the game block at
     * @return true if the block was placed successfully
     */
    public boolean placeBlock(int x, int y) {
        if (grid.playPiece(currentPiece, x, y)) {
            Multimedia.playAudio("place.wav");
            restartGameLoop();
            afterPiece();
            setCurrentPiece();
            nextPiece();
            if (multiplayer) {
                ((MultiplayerGame)this).sendBoardChanged();
            }
            return true;   
        } else {
            Multimedia.playAudio("fail.wav");
            return false;
        }
    }

    /**
     * Called after a piece was placed on the grid. Checks if there are any lines, and if any lines are found,
     * add them to a set and then clear the line by setting the block's value to 0;
     */
    public void afterPiece() {
        ArrayList<GameBlockCoordinate> vList = new ArrayList<GameBlockCoordinate>();
        ArrayList<GameBlockCoordinate> hList = new ArrayList<GameBlockCoordinate>();
        HashSet<GameBlockCoordinate> blocksSet = new HashSet<GameBlockCoordinate>();
        int lines = 0;
        
        //Loop through the grid looking for any lines to be cleared
        for (int i = 0; i < grid.getCols(); i++) {
            for (int j = 0; j < grid.getRows(); j++) {
                 if (grid.get(i, j) != 0) {
                    GameBlockCoordinate blockToClear = new GameBlockCoordinate(i, j);
                    vList.add(blockToClear);
                 }
                 if (grid.get(j, i) != 0) {
                    GameBlockCoordinate blockToClear = new GameBlockCoordinate(j, i);
                    hList.add(blockToClear);
                }
            }
            //Check whether there is a vertical line
            if (vList.size() == grid.getCols()) {
                for (GameBlockCoordinate gameBlockCoordinate : vList) {
                    blocksSet.add(gameBlockCoordinate);
                }
                lines++;
            }
            //Check whether there is a horizontal line
            if (hList.size() == grid.getRows()) {
                for (GameBlockCoordinate gameBlockCoordinate : hList) {
                    blocksSet.add(gameBlockCoordinate);
                }
                lines++;
            }
            vList.clear();
            hList.clear();
        }

        //If there are any lines needed to be cleared
        if (lines != 0) {
            for (GameBlockCoordinate gameBlockCoordinate : blocksSet) {
                logger.info("Clearing " + gameBlockCoordinate.getX() + ", " + gameBlockCoordinate.getY());
                grid.set(gameBlockCoordinate.getX(), gameBlockCoordinate.getY(), 0);
            }
            lineClearedListener.fadeOut(blocksSet);
            Multimedia.playAudio("clear.wav");
            score(lines, blocksSet);
        }
        setMultiplier(lines);
    }

    /**
     * Update the score of the player after clearing a line of blocks
     * @param numberOfLines the number of lines that were cleared
     * @param blocks the set of blocks that were cleared
     */
    public void score(int numberOfLines, HashSet<GameBlockCoordinate> blocks) {
        logger.info("Updating the score");
        score.set(score.get() + (numberOfLines * blocks.size() * 10 * multiplier.get()));
        setLevel();
    }

    /**
     * Update the level of the player. Up one level on every 1000 points
     */
    public void setLevel() {
        logger.info("Updating the level");
        var tempLevel = level.get();
        level.set(Math.floorDiv(score.get(), 1000));
        if (tempLevel < level.get()) {
            Multimedia.playAudio("level.wav");
        }
    }

    /**
     * Update the multiplier on cleared lines
     * @param lines the number of lines that were cleared
     */
    public void setMultiplier(int lines) {
        logger.info("Updating the multiplier");
        if (lines != 0) {
            multiplier.set(multiplier.get() + 1);
        } else {
            multiplier.set(1);
        }
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Whether the game is a multiplayer one
     * @return true if the game is a multiplayer one
     */
    public boolean isMultiplayer() {
        return multiplayer;
    }

    /**
     * @return the score property
     */
    public SimpleIntegerProperty getScoreProperty() {
        return score;
    }

    /**
     * @return the level property
     */
    public SimpleIntegerProperty getLevelProperty() {
        return level;
    }

    /**
     * @return the lives property
     */
    public SimpleIntegerProperty getLivesProperty() {
        return lives;
    }

    /**
     * @return the multiplier property
     */
    public SimpleIntegerProperty getMultiplierProperty() {
        return multiplier;
    }
    /**
     * @return the high score property
     */
    public SimpleIntegerProperty getHighScoreProperty() {
        return highScore;
    }

    /**
     * @return the game over property
     */
    public SimpleBooleanProperty getGameOverProperty() {
        return gameOver;
    }

    /**
     * @return get the score as an int
     */
    public int getScore() {
        return score.get();
    }

    /**
     * @return get the level as an int
     */
    public int getLevel() {
        return level.get();
    }

    /**
     * @return get the lives as an int
     */
    public int getLives() {
        return lives.get();
    }

    /**
     * @return get the multiplier as an int
     */
    public int getMultiplier() {
        return multiplier.get();
    }

    /**
     * @return get the high score as an int
     */
    public int getHighScore() {
        return highScore.get();
    }

    /**
     * Sets the current piece listener
     * @param listener the current piece listener
     */
    public void setCurrentPieceListener(PieceChangeListener listener) {
        currentPieceListener = listener;
    }

    /**
     * Sets the next piece listener
     * @param listener the next piece listener
     */
    public void setNextPieceListener(PieceChangeListener listener) {
        nextPieceListener = listener;
    }

    /**
     * Sets the line cleared listener
     * @param listener the line cleared listener
     */
    public void setLineClearedListener(LineClearedListener listener) {
        lineClearedListener = listener;
    }

    /**
     * Sets the game loop listener
     * @param listener the game loop listener
     */
    public void setGameLoopListener(GameLoopListener listener) {
        gameLoopListener = listener;
    }

    /**
     * Set the current piece to the next piece
     */
    public void setCurrentPiece() {
        currentPiece = nextPiece;
        currentPieceListener.updatePiece(currentPiece);
    }

    /**
     * Generate a new game piece
     * @return the new game piece
     */
    public GamePiece spawnPiece() {
        logger.info("Creating a new game piece");
        GamePiece piece = GamePiece.createPiece(new Random().nextInt(15));
        currentPieceListener.updatePiece(piece);
        return piece;
    }

    /**
     * Creates a next piece to be played after the current piece has been played
     */
    public void nextPiece() {
        logger.info("Generating next piece");
        nextPiece = GamePiece.createPiece(new Random().nextInt(15));

        nextPieceListener.updatePiece(nextPiece);
    }

    /**
     * @return the current game piece
     */
    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    /**
     * @return the next game piece
     */
    public GamePiece getNextPiece() {
        return nextPiece;
    }

    /**
     * Rotates the current game piece
     * @param rotations the number of rotations to be performed
     */
    public void rotateCurrentPiece (int rotations) {
        logger.info("Rotating " + currentPiece.toString());
        Multimedia.playAudio("rotate.wav");
        currentPiece.rotate(rotations);
        currentPieceListener.updatePiece(currentPiece);
    }

    /**
     * Swap the current and next pieces
     */
    public void swapCurrentPiece () {
        Multimedia.playAudio("rotate.wav");
        logger.info("Swapping " + currentPiece.toString() + " with " + nextPiece.toString());
        var temp = nextPiece;
        nextPiece = currentPiece;
        currentPiece = temp;
        currentPieceListener.updatePiece(currentPiece);
        nextPieceListener.updatePiece(nextPiece);
    }

    /**
     * @return the current game loop time
     */
    public int getTimerDelay() {
        return Math.max(2500, (12000 - 500 * getLevel())); 
    }

    /**
     * When executed, generate a new piece and lower the lives by 1. If the lives are 0, the game ends
     */
    public void gameLoop() {
        logger.info("Executing game loop");
        if (lives.get() > 0) {
            Multimedia.playAudio("lifelose.wav");
            lives.set(lives.get() - 1);
            multiplier.set(1);
        } else {
            Multimedia.playAudio("transition.wav");
            if (multiplayer) {
                ((MultiplayerGame)this).sendCommunication("DIE");
            }
            gameOver.set(true);
            return;
        }
        setCurrentPiece();
        nextPiece();
        restartGameLoop();

    }

    /**
     * Stop the game loop from running
     */
    public void shutdownGameLoop() {
        gameLoopJob.cancel(false);
    }

    /**
     * Restart the game loop
     */
    public void restartGameLoop() {
        gameLoopJob.cancel(false);
        gameLoopListener.gameLoop(getTimerDelay());
        gameLoopJob = scheduler.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
    }
}
