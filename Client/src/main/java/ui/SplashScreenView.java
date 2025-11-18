package ui;

import App.MainApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * SplashScreenView represents the splash screen UI for the Green House Control application. It
 * provides options to log in or register.
 */
public class SplashScreenView {

  private final BorderPane root;

  /**
   * Constructs the SplashScreenView with Log In and Register buttons.
   *
   * @param mainApp the main application instance for navigation
   */
  public SplashScreenView(MainApp mainApp) {
    root = new BorderPane();
    root.setStyle("-fx-background-color: #ffffff;");

    root.getStylesheets().add(getClass().getResource("/client.css").toExternalForm());

    // Log In Button
    Button goButton = new Button("Log In");
    goButton.setStyle(
        "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding:"
            + "10 20; -fx-background-radius: 20; -fx-font-size: 16px; -fx-cursor: hand;");
    goButton.setOnAction(e -> mainApp.showLoginScreen());

    // Registration Button
    Button registerButton = new Button("Register");
    registerButton.setStyle(
        "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding:"
            + "10 20; -fx-background-radius: 20; -fx-font-size: 16px; -fx-cursor: hand;");
    registerButton.setOnAction(e -> mainApp.showRegistrationScreen());
    // Layout Buttons in a VBox
    VBox buttonBox = new VBox(10, goButton, registerButton);
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    buttonBox.setPadding(new Insets(20));
    root.setTop(buttonBox);
    // Center Image and Title
    Label titleMain = new Label("GREEN HOUSE");
    titleMain.getStyleClass().add("splash-title");
    // Load and configure image
    ImageView imageView = new ImageView(
        new Image(getClass().getResourceAsStream("/images/Farm house.png")));
    imageView.setFitWidth(1000);
    imageView.setFitHeight(650);
    imageView.setPreserveRatio(true);
    // Layout Title and Image in a VBox
    VBox centerBox = new VBox(50, titleMain, imageView);
    centerBox.setAlignment(Pos.CENTER);
    centerBox.setPadding(new Insets(20));
    root.setCenter(centerBox);
  }

  public BorderPane getRoot() {
    return root;
  }
}