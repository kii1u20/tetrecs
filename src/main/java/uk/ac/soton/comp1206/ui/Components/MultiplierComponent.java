package uk.ac.soton.comp1206.ui.Components;

import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.game.Game;

/**
 * Component representing the current multiplier
 */
public class MultiplierComponent extends VBox {
    Text title = new Text("Multiplier");
    Text multiplier = new Text();
    Game game;

    public MultiplierComponent (Game game) {
        this.setAlignment(Pos.CENTER);
        this.game = game;
        multiplier.textProperty().bind(game.getMultiplierProperty().asString());
        title.getStyleClass().add("heading");
        multiplier.getStyleClass().add("multiplier");
        this.getChildren().add(title);
        this.getChildren().add(multiplier);
    }
}
