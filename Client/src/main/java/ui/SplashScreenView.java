package ui;

import App.MainApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
/**
 * Represents the splash screen view of the application.
 * This screen serves as the welcome interface with a title, image, and a button to proceed to the login screen.
 */
public class SplashScreenView {
  private final BorderPane root;

  public SplashScreenView(MainApp mainApp) {
    root = new BorderPane();
    root.setStyle("-fx-background-color: #ffffff;");

    root.getStylesheets().add(getClass().getResource("/client.css").toExternalForm());
    Button goButton = new Button("Log In");
    goButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 20; -fx-font-size: 16px; -fx-cursor: hand;");
    goButton.setOnAction(e -> mainApp.showLoginScreen());

    HBox topBar = new HBox(goButton);
    topBar.setAlignment(Pos.CENTER_RIGHT);
    topBar.setPadding(new Insets(20));
    root.setTop(topBar);


    Label titleMain = new Label("GREEN HOUSE");
    titleMain.getStyleClass().add("splash-title");


    ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/Farm house.png")));
    imageView.setFitWidth(1000);
    imageView.setFitHeight(650);
    imageView.setPreserveRatio(true);


    VBox centerBox = new VBox(50, titleMain, imageView);
    centerBox.setAlignment(Pos.CENTER);
    centerBox.setPadding(new Insets(20));
    root.setCenter(centerBox);
  }

  public BorderPane getRoot() {
    return root;
  }
}
