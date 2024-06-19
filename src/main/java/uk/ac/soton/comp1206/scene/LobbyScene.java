package uk.ac.soton.comp1206.scene;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class LobbyScene extends BaseScene implements CommunicationsListener {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    private Communicator communicator;

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> timer;

    private VBox channelList;
    private BorderPane mainPane;
    private Button startGameButton;
    private VBox messages;
    private VBox userList;
    private ScrollPane chatWindow;

    private boolean hostButtonClicked;

    public static String nickname;

    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        communicator = gameWindow.getCommunicator();
    }

    /**
     * Handle what happens when a key is pressed
     */
    @Override
    public void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            timer.cancel(false);
            sendCommunication("PART");
            gameWindow.startMenu();
        } else if (event.getCode() == KeyCode.HOME) {
            updateChannelsList();
        }
    }

    /**
     * Request the current channels from the server
     */
    public void updateChannelsList() {
        communicator.send("LIST");
    }

    /**
     * Initialize the scene
     */
    @Override
    public void initialise() {
        logger.info("Opening instructions");
        timer = scheduler.scheduleWithFixedDelay(this::updateChannelsList, 0, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Build the scene
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        communicator.addListener(this);
        
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

        var hostButton = new Button("Host Game");
        hostButton.getStyleClass().add("hostButton");
        var buttonBox = new VBox();
        buttonBox.getChildren().add(hostButton);
        hostButton.setOnAction(e -> {
            createNewChannel(buttonBox);
            hostButtonClicked = true;
        });
        leftBar.getChildren().add(buttonBox);

        var holder = new VBox();
        holder.setSpacing(10);
        var currentChannelsText = new Text("Current channels:");
        channelList = new VBox();
        currentChannelsText.getStyleClass().add("heading");
        holder.getChildren().add(currentChannelsText);
        holder.getChildren().add(channelList);
        leftBar.getChildren().add(holder);
    }

    /**
     * Create a new channel
     * @param buttonBox the container for the text field
     */
    public void createNewChannel(VBox buttonBox) {
        if (!hostButtonClicked) {
            var channelNameInput = new TextField();
            channelNameInput.setPromptText("Enter channel name");
            buttonBox.getChildren().add(channelNameInput);
            channelNameInput.setOnAction(e -> {sendCommunication("CREATE " + channelNameInput.getText());});
        }

    }

    /**
    * Handles building the chat UI for the channel
     * @param channel the name of the channel
     */
    public void buildChannelUI(String channel) {
        var pane = new BorderPane();
        pane.setMaxWidth(gameWindow.getWidth() / 1.5);
        pane.setMaxHeight(gameWindow.getHeight() / 1.5);
        pane.getStyleClass().add("chatwindow");
        mainPane.setCenter(pane);

        chatWindow = new ScrollPane();
        chatWindow.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        chatWindow.setHbarPolicy(ScrollBarPolicy.NEVER);
        pane.setCenter(chatWindow);

        messages = new VBox();
        chatWindow.setContent(messages);
        chatWindow.setFitToWidth(true);

        var currentChannelText = new Text(channel);
        var holder = new HBox();
        holder.getChildren().add(currentChannelText);
        currentChannelText.getStyleClass().add("title");
        pane.setTop(holder);
        holder.setAlignment(Pos.CENTER);
        holder.setPadding(new Insets(5, 5, 5, 5));

        userList = new VBox();
        userList.setPadding(new Insets(5, 5, 5, 5));
        pane.setLeft(userList);

        var buttonsBox = new BorderPane();
        pane.setBottom(buttonsBox);

        var textInput = new TextField();
        textInput.setPromptText("Enter a message");
        textInput.setOnAction(e -> {
            sendCommunication("MSG " + textInput.getText());
            textInput.clear();
        });
        buttonsBox.setTop(textInput);

        var leaveButton = new Button("Leave channel");
        leaveButton.setOnAction(e -> {leaveChannel();});
        buttonsBox.setRight(leaveButton);

        startGameButton = new Button("Start Game");
        startGameButton.setOnAction(e -> {sendCommunication("START");});
        startGameButton.setVisible(false);
        buttonsBox.setLeft(startGameButton);
        
    }
    
    /**
     * Send communication to the server
     * @param communication the communication to be send
     */
    public void sendCommunication(String communication) {
        if (communication.contains("/nick")) {
            var temp = communication.replace("/nick", "");
            temp = temp.replace("MSG", "");
            communicator.send("NICK " + temp);
        } else {
            communicator.send(communication);
        }
    }

    /**
     * Join a channel
     * @param channel the channel to join
     */
    public void joinChannel(String channel) {
        buildChannelUI(channel);
    }

    /**
     * Leave a channel
     */
    public void leaveChannel() {
        sendCommunication("PART");
    }

    /**
     * Handles recieving a communication from the server
     * @param communication the message recieved from the server
     */
    @Override
    public void receiveCommunication(String communication) {
        logger.info(communication);     
        if (communication.startsWith("CHANNELS")) {
            Platform.runLater(() -> {channelList.getChildren().clear();});
            var temp = communication.split(" ");
            var channels = temp[1].split("\n");
            for (String string : channels) {
                var channel = new Text(string);
                channel.getStyleClass().add("channelItem");
                channel.setOnMouseClicked(e -> {sendCommunication("JOIN " + channel.getText());});
                Platform.runLater(() -> {channelList.getChildren().add(channel);});
            }
        } else if (communication.startsWith("JOIN")) {
            var channel = communication.split(" ")[1];
            Platform.runLater(() -> {joinChannel(channel);});
        } else if (communication.startsWith("PARTED")) {
            Platform.runLater(() -> {mainPane.setCenter(null);});
        } else if (communication.startsWith("HOST")) {
            Platform.runLater(() -> {startGameButton.setVisible(true);});
        } else if (communication.startsWith("MSG")) {
            var temp = communication.replace("MSG ", "");
            var userMessagesComponent = temp.split("\n");
            for (String string : userMessagesComponent) {
                var nickname = string.split(":")[0];
                var message = string.split(":")[1];
                Platform.runLater(() -> {
                    var messageComponent = new Text(nickname + ": " + message);
                    var textflow = new TextFlow();
                    textflow.getChildren().add(messageComponent);
                    textflow.getStyleClass().add("messages");
                    messages.getChildren().add(textflow);
                    chatWindow.applyCss();
                    chatWindow.layout();
                    chatWindow.setVvalue(1.0f);
                });
            }
        } else if (communication.startsWith("ERROR")) {
            var temp = communication.replace("ERROR ", "");
            Platform.runLater(() -> {Alert error = new Alert(Alert.AlertType.ERROR, temp);
                error.showAndWait();
            });
        } else if (communication.startsWith("USERS")) {
            Platform.runLater(() -> {userList.getChildren().clear();;});
            var temp = communication.replace("USERS ", "");
            var users = temp.split("\n");
            for (String string : users) {
                var user = new Text(string);
                user.getStyleClass().add("playerBox");
                Platform.runLater(() -> {userList.getChildren().add(user);});
            }
        } else if (communication.startsWith("START")) {
            Platform.runLater(() -> {
                timer.cancel(false);
                sendCommunication("SCORES");
                gameWindow.openMultiplayerGame(userList.getChildren());
            });
        } else if (communication.startsWith("NICK")) {
            if (communication.contains(":")) {
                var temp = communication.replace("NICK ", "");
                var name = temp.split(":")[1];
                nickname = name;
            } else {
                var name = communication.replace("NICK ", "");
                nickname = name;
            }
        } 
    }
}
