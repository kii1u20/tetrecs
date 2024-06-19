package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * A class for playing music and sounds
 */
public class Multimedia {

    private static final Logger logger = LogManager.getLogger(Multimedia.class);

    private static MediaPlayer audioPlayer;
    private static MediaPlayer musicPlayer;

    private static SimpleBooleanProperty mute = new SimpleBooleanProperty(false);

    /**
     * Plays a sound one time only
     * @param file the audio file to be played
     */
    public static void playAudio(String file) {
        if (mute.get() == false) {
            String fileToPlay = Multimedia.class.getResource("/sounds/" + file).toExternalForm();
            logger.info("Playing: " + fileToPlay);
            Media media = new Media(fileToPlay);
            audioPlayer = new MediaPlayer(media);
    
            try {
                audioPlayer.play();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Unable to play: " + fileToPlay);
            }
        }
    }

    /**
     * Plays a audio file as background music
     * @param loopFile the audio file to be played
     */
    public static void playBackgroundMusic(String loopFile) {
        if (mute.get() == false) {
            String fileToPlay = Multimedia.class.getResource("/music/" + loopFile).toExternalForm();
            logger.info("Playing: " + fileToPlay);
            Media media = new Media(fileToPlay);
            musicPlayer = new MediaPlayer(media);
            musicPlayer.cycleCountProperty().set(MediaPlayer.INDEFINITE); //Set the media player to play continuously
            musicPlayer.setVolume(0.5);
    
            try {
                musicPlayer.play();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Unable to play: " + fileToPlay);
            }    
        }
    }

    public static SimpleBooleanProperty getMuteProperty() {
        return mute;
    }

    /**
     * Stop playing background music
     */
    public static void stopPlayingBackgroundMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }
}
