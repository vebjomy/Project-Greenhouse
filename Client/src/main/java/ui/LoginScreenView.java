package ui;

import App.MainApp;
import controller.LoginController;
import model.ServerConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextInputDialog;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * LoginScreenView represents the login screen UI for the Green House Control application.
 * It provides fields for username, password, server selection, and navigation options.
 */
public class LoginScreenView {
  private final HBox root;
  private TextField usernameField;
  private PasswordField passwordField;

  // Constructor to initialize the login screen view

  public LoginScreenView(MainApp mainApp) {
    root = new HBox();
    root.setStyle("-fx-background-color: #ffffff;");

    root.getStylesheets().add(getClass().getResource("/client.css").toExternalForm());

    // create left panel ---
    VBox leftPane = createLeftPane(mainApp);

    // create right panel ---
    VBox rightPane = createRightPane(mainApp);

    // layout settings ---
    leftPane.setMaxWidth(500);
    leftPane.setMinWidth(500);
    HBox.setHgrow(rightPane, Priority.ALWAYS);

    root.getChildren().addAll(leftPane, rightPane);
  }

  /** Creates the left pane of the login screen containing login fields and server selection.
   * @param mainApp Reference to the main application for navigation.
   *                @return Configured VBox representing the left pane.
   */

  private VBox createLeftPane(MainApp mainApp) {
    VBox pane = new VBox(25);
    pane.setPadding(new Insets(50));
    pane.setAlignment(Pos.CENTER_LEFT);
    pane.setStyle("-fx-background-color: #ecffe9; -fx-border-color: #eafde6; -fx-border-width: 0 1 0 0;");

    // Header Texts

    Text subtitle = new Text("Green House\nAn Project can\nchange your live\n");
    subtitle.getStyleClass().add("login-title-subtitle");
    Text prompt = new Text("Welcome back, please login\nto your account");
    prompt.getStyleClass().add("login-prompt");
    TextFlow headerFlow = new TextFlow( subtitle, prompt);
    headerFlow.setTextAlignment(TextAlignment.LEFT);

  // Input Fields


    // Input Fields
    usernameField = new TextField();
    usernameField.setPromptText("Username");
    usernameField.getStyleClass().add("text-field");

    passwordField = new PasswordField(); // Use the class field, not a new local variable
    passwordField.setPromptText("Password");
    passwordField.getStyleClass().add("password-field");
    VBox.setMargin(passwordField, new Insets(0, 0, 25, 0));

// Server Selection

    ObservableList<ServerConfig> serverList = FXCollections.observableArrayList(
        new ServerConfig("Localhost (Default)", "localhost"),
        new ServerConfig("Development Server", "192.168.1.100")
    );
    ComboBox<ServerConfig> serverComboBox = new ComboBox<>(serverList);
    serverComboBox.setPromptText("Select Server");
    serverComboBox.getSelectionModel().selectFirst();
    serverComboBox.setMaxWidth(Double.MAX_VALUE);
    serverComboBox.getStyleClass().add("text-field");
// Custom IP Button
    Button customIpButton = new Button("Set Custom\nIP Address");
    customIpButton.getStyleClass().add("ip-button");
    customIpButton.setMaxWidth(Double.MAX_VALUE);
    customIpButton.setStyle("-fx-font-size: 11px; -fx-padding: 10 5;");
    customIpButton.setWrapText(true);
    customIpButton.setPrefHeight(50);
// Server Selector Container
    HBox serverSelector = new HBox(10, serverComboBox, customIpButton);
    serverSelector.setAlignment(Pos.CENTER);
    HBox.setHgrow(serverComboBox, Priority.ALWAYS);
    HBox.setHgrow(customIpButton, Priority.ALWAYS);

    serverComboBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
    customIpButton.setPrefWidth(Region.USE_COMPUTED_SIZE);

    VBox.setMargin(serverSelector, new Insets(0, 0, 15, 0));

// Login Button
    Button loginButton = new Button("LOG IN");
    loginButton.getStyleClass().add("login-button");
    HBox buttonContainer = new HBox(loginButton);
    buttonContainer.setAlignment(Pos.CENTER);
// Login Button Action
    loginButton.setOnAction(e -> {
      LoginController loginController = new LoginController(this);
      if (loginController.handleLogin()) {
        ServerConfig selectedServer = serverComboBox.getSelectionModel().getSelectedItem();
        if (selectedServer != null) {
          String ipAddress = selectedServer.getIpAddress();
          System.out.println("Connecting to: " + ipAddress);
          mainApp.showDashboard();
        }
      }
    });

    customIpButton.setOnAction(e -> {
      showCustomIpDialog(serverComboBox);
    });


// Assemble Left Pane
    pane.getChildren().addAll(
        headerFlow,
        new VBox(5, usernameField),
        new VBox(5, passwordField),
        serverSelector,
        loginButton
    );
    return pane;
  }

  public TextField getUsernameField() {
    return usernameField;
  }

  public PasswordField getPasswordField() {
    return passwordField;
  }

  /** Displays a dialog to input a custom server IP address.
   * @param serverComboBox The ComboBox to update with the custom IP.
   */
  private void showCustomIpDialog(ComboBox<ServerConfig> serverComboBox) {
    TextInputDialog dialog = new TextInputDialog("127.0.0.1");
    dialog.setTitle("Set Custom Server IP");
    dialog.setHeaderText("Enter the IP address of your Green House Server.");
    dialog.setContentText("IP Address:");
    ServerConfig currentSelection = serverComboBox.getSelectionModel().getSelectedItem();
    if (currentSelection != null) {
      dialog.getEditor().setText(currentSelection.getIpAddress());
    }
    Optional<String> result = dialog.showAndWait();

    result.ifPresent(ip -> {
      String trimmedIp = ip.trim();
      if (!trimmedIp.isEmpty()) {
        Optional<ServerConfig> existing = serverComboBox.getItems().stream()
            .filter(sc -> sc.getIpAddress().equals(trimmedIp))
            .findFirst();
        if (existing.isPresent()) {
          serverComboBox.getSelectionModel().select(existing.get());
        } else {
          ServerConfig customServer = new ServerConfig("Custom IP", trimmedIp);
          serverComboBox.getItems().add(customServer);
          serverComboBox.getSelectionModel().select(customServer);
        }
      }
    });
  }
/** Creates the right pane of the login screen containing navigation buttons and content area.
   * @param mainApp Reference to the main application for navigation.
   * @return Configured VBox representing the right pane.
   */
  private VBox createRightPane(MainApp mainApp) {
    VBox pane = new VBox(30);
    pane.setPadding(new Insets(30));
    pane.setAlignment(Pos.TOP_CENTER);
    ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/house.gif")));
    imageView.setFitHeight(850);
    imageView.setPreserveRatio(true);
// About Project Text
    Text aboutText = new Text("Our 'Green House Control' project is a modern system for automated " +
        "greenhouse management. It allows you to monitor and regulate temperature, " +
        "humidity, lighting, and watering in real time, ensuring optimal conditions for plant growth.");
    aboutText.setWrappingWidth(480);
    aboutText.setTextAlignment(TextAlignment.JUSTIFY);
    aboutText.setStyle("-fx-font-size: 24px; -fx-line-spacing: 6px; -fx-font-family: 'Kaisei Decol';");
    aboutText.setVisible(false);
// Creators Text
    Text creatorsText = new Text(
        "Creators of the 'Green House Control' project:\n\n" +
            "1. ejob - Project Manager\n" +
            "2. VB- Lead Developer\n" +
            "3. Arkadii - UI/UX Designer\n" +
            "4. VJ - Hardware Specialist\n" +
            "5. Dmitry - Quality Assurance\n\n" +
            "Together, we combined our expertise to create an innovative solution for modern agriculture.");
    creatorsText.setWrappingWidth(480);
    creatorsText.setTextAlignment(TextAlignment.LEFT);
    creatorsText.setStyle("-fx-font-size: 24px; -fx-line-spacing: 6px; -fx-font-family: 'Kaisei Decol';");
    creatorsText.setVisible(false);

    StackPane contentArea = new StackPane(imageView, aboutText, creatorsText);
    contentArea.setAlignment(Pos.CENTER);
    contentArea.setMinHeight(350);

    Button homeButton = new Button("HOME");
    Button aboutButton = new Button("ABOUT PROJECT");
    Button creatorsButton = new Button("CREATORS");

    HBox menuBar = new HBox(30, homeButton, aboutButton, creatorsButton);
    menuBar.setAlignment(Pos.CENTER);

    homeButton.setOnAction(e -> mainApp.showSplashScreen());
    aboutButton.setOnAction(e -> {
      imageView.setVisible(false);
      creatorsText.setVisible(false);
      aboutText.setVisible(true);
    });
    creatorsButton.setOnAction(e -> {
      imageView.setVisible(false);
      aboutText.setVisible(false);
      creatorsText.setVisible(true);
    });
// Menu Button Styles

    for (Node n : menuBar.getChildren()) {
      if (n instanceof Button button) {
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: #000000; -fx-font-weight: bold; " +
            "-fx-font-size: 18px; -fx-padding: 5 10; -fx-border-width: 0 0 2 0; " +
            "-fx-border-color: transparent; -fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-border-color: #3f3e3e;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-border-color: #1a73e8;", "-fx-border-color: transparent;")));
      }
    }

    pane.getChildren().addAll(menuBar, contentArea);
    return pane;
  }

  /** Returns the root HBox of the login screen view.
   * @return The root HBox containing the entire login screen layout.
   */

  public HBox getRoot() {
    return root;
  }
}
