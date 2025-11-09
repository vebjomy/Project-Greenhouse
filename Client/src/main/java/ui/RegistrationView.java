package ui;

import App.MainApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * RegistrationView represents the user registration screen UI for the application.
 * It provides fields for username, password, and password confirmation.
 */

public class RegistrationView {
  private final BorderPane root;

  public RegistrationView(MainApp mainApp) {
    root = new BorderPane();
    root.setStyle("-fx-background-color: #ffffff;");

    Label title = new Label("User Registration");
    title.setStyle("-fx-font-size: 60px; -fx-font-weight: bold; -fx-text-fill: #333333;");

    TextField usernameField = new TextField();
    usernameField.setPromptText("Enter username");

    PasswordField passwordField = new PasswordField();
    passwordField.setPromptText("Enter password");

    PasswordField confirmPasswordField = new PasswordField();
    confirmPasswordField.setPromptText("Confirm password");

    Button registerButton = new Button("Register");
    registerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 20; -fx-font-size: 16px; -fx-cursor: hand;");
    registerButton.setOnAction(e -> {
      // Handle registration logic here
      System.out.println("Registered: " + usernameField.getText());
      mainApp.showSplashScreen(); // Return to splash screen
    });

    VBox formBox = new VBox(10, title, usernameField, passwordField,confirmPasswordField ,registerButton);
    formBox.setAlignment(Pos.CENTER);
    formBox.setPadding(new Insets(20));
    root.setCenter(formBox);
  }

  // Getter for the root pane

  public BorderPane getRoot() {
    return root;
  }
}
