package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import uk.ac.soton.comp1206.component.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class SettingsScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(SettingsScene.class);
    
    private BorderPane mainPane;

    private static CheckBox muteBox = new CheckBox("Mute audio");;

    public SettingsScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    /**
     * Handle key being pressed
     */
    @Override
    public void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            createSaveFile();
            gameWindow.startMenu();
        }
    }

    /**
     * Initialize the scene
     */
    @Override
    public void initialise() {
        logger.info("Initializing the scene");
        readSaveFile();
    }

    /**
     * Build the UI
     */
    @Override
    public void build() {
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());    
        
        mainPane = new BorderPane();
        mainPane.setMaxWidth(gameWindow.getWidth());
        mainPane.setMaxHeight(gameWindow.getHeight());
        mainPane.getStyleClass().add("menu-background");
        root.getChildren().add(mainPane);

        var leftBar = new VBox();
        leftBar.setPadding(new Insets(30, 10, 10, 10));
        leftBar.setSpacing(20);
        mainPane.setLeft(leftBar);

        leftBar.getChildren().add(muteBox);
    }

    /**
     * Create a settings save file
     */
    public static void createSaveFile() {
        BufferedWriter scoresWriter;
        File scoresFile = new File("settings.txt");
        try {
            scoresWriter = new BufferedWriter(new FileWriter(scoresFile));
            scoresWriter.write(muteBox.selectedProperty().get() + "\n");
            scoresWriter.close();
        } catch (Exception e) {
            logger.error("Settings file is not found");
        }
    }

    /**
     * Read the save file and load the saved settings
     */
    public static void readSaveFile() {
        BufferedReader scoresReader;
        File scoresFile = new File("settings.txt");
        try {
            scoresReader = new BufferedReader(new FileReader(scoresFile));
            String scoreLine = scoresReader.readLine();
            while (scoreLine != null) {
                var setting = scoreLine.split("\n");
                muteBox.selectedProperty().bindBidirectional(Multimedia.getMuteProperty());
                muteBox.selectedProperty().set(Boolean.parseBoolean(setting[0]));
                scoreLine = scoresReader.readLine();
            }
            scoresReader.close();
        } catch (Exception e) {
            logger.error("Settings file is not found");
            e.printStackTrace();
            createSaveFile();
        }
    }
    
}
