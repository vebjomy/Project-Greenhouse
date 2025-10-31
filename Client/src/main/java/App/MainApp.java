package App;

import core.ClientApi;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ui.DashboardView;
import ui.LoginScreenView;
import ui.RegistrationView;
import ui.SplashScreenView;

/**
 * The `MainApp` class serves as the entry point for the Green House Control application.
 * It initializes the JavaFX application, manages the primary stage, and provides methods
 * to navigate between different views such as the splash screen, login screen, registration screen, and dashboard.
 */
public class MainApp extends Application {

  private Stage primaryStage;
  private LoginScreenView loginScreenView;
  private static final double SCENE_WIDTH = 1480;
  private static final double SCENE_HEIGHT = 1000;
  private boolean isConnected = false; // new status field
  private final String SERVER_ADDRESS = "127.0.0.1";
  /**
   * Starts the JavaFX application by setting up the primary stage and displaying the splash screen.
   *
   * @param stage The primary stage for this application.
   */
  @Override
  public void start(Stage stage) {
    DashboardView dashboard = new DashboardView();

    ClientApi api = new ClientApi();
    api.connect(SERVER_ADDRESS, 5555).thenRun(() -> {
      System.out.println("Connected to server");
      isConnected = true; // status update
      // Оupdate UI if LoginScreenView is already initialized
      if (loginScreenView != null) {
        javafx.application.Platform.runLater(() -> loginScreenView.updateServerStatus(true));
      }
    }).exceptionally(ex -> {
      System.err.println("Failed to connect to server: " + ex.getMessage());
      isConnected = false; // setting status
      // update UI if LoginScreenView is already initialized
      if (loginScreenView != null) {
        javafx.application.Platform.runLater(() -> loginScreenView.updateServerStatus(false));
      }
      return null;
    });
    dashboard.initNetwork(api);

    this.primaryStage = stage;
    primaryStage.setTitle("Green House Control");
    primaryStage.centerOnScreen();

    Font customFont = Font.loadFont(getClass().getResourceAsStream("/fonts/KaiseiDecol-Regular.ttf"), 10);
    if (customFont == null) {
      System.err.println("error loading font");
    } else {
      System.out.println("Font is loaded: " + customFont.getFamily());
    }

    showSplashScreen(); // Start with the splash screen

    primaryStage.setMinHeight(SCENE_HEIGHT);
    primaryStage.setMinWidth(SCENE_WIDTH);
    primaryStage.show();
  }

  /**
   * Displays the splash screen view.
   */
  public void showSplashScreen() {
    SplashScreenView splash = new SplashScreenView(this);
    Scene scene = new Scene(splash.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Green House Control - Welcome");
  }

  /**
   * Displays the registration screen view.
   */
  public void showRegistrationScreen() {
    RegistrationView registration = new RegistrationView(this);
    Scene scene = new Scene(registration.getRoot(), 400, 400);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Green House Control - Register");
  }

  /**
   * Displays the login screen view.
   */
  public void showLoginScreen() {
    // Initialize the loginScreenView field
    loginScreenView = new LoginScreenView(this);

    // Update the server status
    loginScreenView.updateServerStatus(isConnected);

    // Set up the scene and stage
    Scene scene = new Scene(loginScreenView.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Green House Control - Login");
  }




  public boolean isConnected() {
    return isConnected;
  }

  public String getServerAddress() {
    return SERVER_ADDRESS;
  }

  /**
   * Displays the dashboard view.
   */
  public void showDashboard() {
    DashboardView dashboard = new DashboardView();
    Scene scene = new Scene(dashboard.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Smart Farm Control");
  }

  /**
   * The main method that launches the JavaFX application.
   *
   * @param args Command-line arguments passed to the application.
   */
  public static void main(String[] args) {
    launch(args);
  }
}
