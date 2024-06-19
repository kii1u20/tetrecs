package uk.ac.soton.comp1206.ui.Components;

import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.game.Game;

/**
 * Component representing the current lives
 */
public class LivesComponent extends VBox {
    Text title = new Text("Lives");
    Text lives = new Text();
    Game game;

    public LivesComponent (Game game) {
        this.setAlignment(Pos.CENTER);
        this.game = game;
        lives.textProperty().bind(game.getLivesProperty().asString());
        title.getStyleClass().add("title");
        lives.getStyleClass().add("lives");
        this.getChildren().add(title);
        this.getChildren().add(lives);
    }
}
