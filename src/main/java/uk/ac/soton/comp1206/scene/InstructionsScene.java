package uk.ac.soton.comp1206.scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class InstructionsScene extends BaseScene {
    
    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
    }



    /**
     * Initialize the scene
     */
    @Override
    public void initialise() {
        logger.info("Opening instructions");        
    }

    /** 
     * Build the scene
    */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());
        
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());    
        
        var mainPane = new BorderPane();
        mainPane.setMaxWidth(gameWindow.getWidth());
        mainPane.setMaxHeight(gameWindow.getHeight());
        mainPane.getStyleClass().add("menu-background");
        root.getChildren().add(mainPane);

        var box = new VBox();
        mainPane.setCenter(box);
        BorderPane.setAlignment(box, Pos.CENTER);
        box.setAlignment(Pos.TOP_CENTER);

        var instructions = new Text("Instructions");
        instructions.getStyleClass().add("title");
        box.getChildren().add(instructions);

        String imagePath = getClass().getResource("/images/Instructions.png").toExternalForm();
        ImageView instructionsImage = new ImageView(imagePath);
        instructionsImage.setFitWidth(gameWindow.getWidth()/1.5);
        instructionsImage.setPreserveRatio(true);
        box.getChildren().add(instructionsImage);

        var pieces = new GridPane();
        box.getChildren().add(pieces);
        pieces.setHgap(10);
        pieces.setVgap(10);
        pieces.setAlignment(Pos.CENTER);
        pieces.setPadding(new Insets(0, 0, 10, 0));

        int xPos = 0;
        int yPos = 0;

        //Generates the game pieces in the instruction screen
        for (int i = 0; i < 15; i++) {
            GamePiece piece = GamePiece.createPiece(i);
            var pieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 13, gameWindow.getWidth() / 13);
            pieceBoard.displayPiece(piece);
            pieces.add(pieceBoard, xPos, yPos);
            xPos++;
            if (xPos == 5) {
                xPos = 0;
                yPos++;
            }
        }
    }

    /**
     * Handle what happens when a key is pressed
     */
    @Override
    public void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            gameWindow.startMenu();
        }
    }
}
