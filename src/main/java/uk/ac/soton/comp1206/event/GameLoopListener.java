package uk.ac.soton.comp1206.event;

/**
 * The game loop listener is used for listening to the current time used by the game loop
 */
public interface GameLoopListener {
    /**
     * Handle a change in the game loop time
     * @param time the current time of the game loop
     */
    public void gameLoop(int time);
}
