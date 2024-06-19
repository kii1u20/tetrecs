package uk.ac.soton.comp1206.event;

import javafx.scene.input.KeyEvent;

/**
 * The key pressed listener is used for listening to key presses
 */
public interface KeyPressedListener {
    /**
     * Handle a key being pressed
     * @param event the event of the keystroke
     */
    public void handleKeyPress(KeyEvent event);
}
