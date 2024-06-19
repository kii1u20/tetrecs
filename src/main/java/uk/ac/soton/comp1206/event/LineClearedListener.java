package uk.ac.soton.comp1206.event;

import java.util.HashSet;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * The line cleared listener is used for listening to new lines being cleared
 */
public interface LineClearedListener {
    /**
     * Run the line cleared animation
     * @param blocks the set of blocks to be cleared
     */
    public void fadeOut(HashSet<GameBlockCoordinate> blocks);
}
