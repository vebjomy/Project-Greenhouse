package ui;

import App.MainApp;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import model.ServerConfig;


/**
 * LoginScreenView represents the login screen UI for the Green House Control application. It
 * provides fields for username, password, server selection, and navigation options.
 */
public class LoginScreenView {

  private final HBox root;
  private final Circle statusIndicator;
  private final Text statusText;
  private TextField usernameField; // Declare usernameField
  private PasswordField passwordField; // Declare passwordField


  /**
   * Constructor to initialize the login screen view.
   *
   * @param mainApp Reference to the main application for navigation.
   */
  public LoginScreenView(MainApp mainApp) {
    root = new HBox();
    root.setStyle("-fx-background-color: #ffffff;");
    statusIndicator = new Circle(7);
    statusText = new Text("Server Status: Connecting...");

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

  /**
   * Creates the left pane of the login screen containing login fields and server selection.
   *
   * @param mainApp Reference to the main application for navigation.
   * @return Configured VBox representing the left pane.
   */

  private VBox createLeftPane(MainApp mainApp) {
    VBox pane = new VBox(25);
    pane.setPadding(new Insets(50));
    pane.setAlignment(Pos.CENTER_LEFT);
    pane.setStyle(
        "-fx-background-color: #ecffe9; -fx-border-color: #eafde6; -fx-border-width: 0 1 0 0;");

    // Header Texts

    Text subtitle = new Text("Green House\nA Project that can\nchange your life\n");
    subtitle.getStyleClass().add("login-title-subtitle");
    Text prompt = new Text("Welcome back! Please log in\nto your account");
    prompt.getStyleClass().add("login-prompt");
    TextFlow headerFlow = new TextFlow(subtitle, prompt);
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

    // Server Status Indicator ---

    statusIndicator.getStyleClass().add("status-indicator");

    statusText.getStyleClass().add("status-text");

    HBox serverStatusContainer = new HBox(10, statusIndicator, statusText);
    serverStatusContainer.setAlignment(Pos.CENTER_LEFT);
    VBox.setMargin(serverStatusContainer, new Insets(0, 0, 15, 0));

    // Login Button
    Button loginButton = new Button("LOG IN");
    loginButton.getStyleClass().add("login-button");

    controller.LoginController loginController = new controller.LoginController(
        this,
        mainApp.getClientApi(),
        mainApp
    );

    loginButton.setOnAction(e -> loginController.handleLogin());

    HBox buttonContainer = new HBox(loginButton);
    buttonContainer.setAlignment(Pos.CENTER);
    // Login Button Action
    //    loginButton.setOnAction(e -> {
    //      // define login action here
    //      String ipAddress = mainApp.getServerAddress(); // adjust as needed
    //      if (mainApp.isConnected()) { // check server connection
    //        System.out.println("Attempting login to: " + ipAddress);
    //        mainApp.showDashboard();
    //      } else {
    //        new Alert(Alert.AlertType.ERROR, "Server is offline. Cannot log in.").showAndWait();
    //      }
    //    });

    // Assemble Left Pane
    pane.getChildren().addAll(
        headerFlow,
        new VBox(5, usernameField),
        new VBox(5, passwordField),
        serverStatusContainer,
        loginButton
    );
    return pane;
  }

  /**
   * Updates the server status indicator and text based on connection status.
   *
   * @param isConnected True if connected to the server, false otherwise.
   */
  public void updateServerStatus(boolean isConnected) {
    if (isConnected) {
      statusIndicator.setFill(javafx.scene.paint.Color.LIMEGREEN);
      statusText.setText("Server Status: Online (Connected)");
    } else {
      statusIndicator.setFill(javafx.scene.paint.Color.RED);
      statusText.setText("Server Status: Offline (Disconnected)");
    }
  }

  /**
   * Displays a dialog to input a custom server IP address.
   *
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

  /**
   * Creates the right pane of the login screen containing navigation buttons and content area.
   *
   * @param mainApp Reference to the main application for navigation.
   * @return Configured VBox representing the right pane.
   */
  private VBox createRightPane(MainApp mainApp) {
    VBox pane = new VBox(30);
    pane.setPadding(new Insets(30));
    pane.setAlignment(Pos.TOP_CENTER);
    ImageView imageView = new ImageView(
        new Image(getClass().getResourceAsStream("/images/house.gif")));
    imageView.setFitHeight(850);
    imageView.setPreserveRatio(true);
    // About Project Text
    Text aboutText = new Text("Welcome to the Smart Greenhouse Network System – a distributed "
        + "IoT application designed for real-time greenhouse monitoring and control.\n"
        + "\n"
        + "This project is part of the IDATA2304 course at NTNU Ålesund and "
        + "demonstrates professional network programming practices using a custom "
        + "TCP-based application-layer protocol.");
    aboutText.setWrappingWidth(480);
    aboutText.setTextAlignment(TextAlignment.JUSTIFY);
    aboutText.setStyle(
        "-fx-font-size: 24px; -fx-line-spacing: 6px; -fx-font-family: 'Kaisei Decol';");
    aboutText.setVisible(false);
    // Creators Text
    Text creatorsText = new Text(
        "Creators of the 'Green House Control' project:\n\n"
            + "1. Vebjørn - Server Lead & Simulation Architect\n"
            + "2. Dymitri - Network Engineer & Data Lead\n"
            + "3. Eyob - Documentation Lead & User System\n"
            + "4. Arkadii - GUI Lead & Client Developer\n\n"
            + "Together, we combined our expertise to create an "
            + "innovative solution for modern agriculture.");
    creatorsText.setWrappingWidth(480);
    creatorsText.setTextAlignment(TextAlignment.LEFT);
    creatorsText.setStyle(
        "-fx-font-size: 24px; -fx-line-spacing: 6px; -fx-font-family: 'Kaisei Decol';");
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
        button.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #000000; -fx-font-weight: bold; "
                + "-fx-font-size: 18px; -fx-padding: 5 10; -fx-border-width: 0 0 2 0; "
                + "-fx-border-color: transparent; -fx-cursor: hand;");
        button.setOnMouseEntered(
            e -> button.setStyle(button.getStyle() + "-fx-border-color: #3f3e3e;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle()
            .replace("-fx-border-color: #1a73e8;", "-fx-border-color: transparent;")));
      }
    }

    pane.getChildren().addAll(menuBar, contentArea);
    return pane;
  }

  /**
   * Returns the root HBox of the login screen view.
   *
   * @return The root HBox containing the entire login screen layout.
   */

  public HBox getRoot() {
    return root;
  }

  public TextField getUsernameField() {
    return usernameField;
  }

  public PasswordField getPasswordField() {
    return passwordField;
  }

}
