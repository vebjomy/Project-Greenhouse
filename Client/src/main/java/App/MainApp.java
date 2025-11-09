package App;

import core.ClientApi;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ui.DashboardView;
import ui.LoginScreenView;
import ui.RegistrationView;
import ui.SplashScreenView;

import java.util.List;

/**
 * The `MainApp` class serves as the entry point for the Green House Control application.
 * It initializes the JavaFX application, manages the primary stage, and provides methods
 * to navigate between different views such as the splash screen, login screen, registration screen, and dashboard.
 */
public class MainApp extends Application {
  private Stage primaryStage;
  private LoginScreenView loginScreenView;
  private DashboardView dashboardView; // <-- Field for the dashboard
  private static final double SCENE_WIDTH = 1480;
  private static final double SCENE_HEIGHT = 1000;
  private ClientApi api;
  private boolean isConnected = false;
  private final String SERVER_ADDRESS = "127.0.0.1";
  private final int SERVER_PORT = 5555;

  @Override
  public void start(Stage stage) {
    api = new ClientApi(); // First, create the api

    // Create the dashboard view ONCE and store it
    dashboardView = new DashboardView(this, api); // Then, pass it

    // Use the port constant
    api.connect(SERVER_ADDRESS, SERVER_PORT).thenRun(() -> {
      Platform.runLater(() -> {
        api.getTopology();
        api.subscribe(List.of("*"), List.of("sensor_update", "node_change"));
        System.out.println("Subscribed for live updates");
      });
      System.out.println("Connected to server");
      isConnected = true;
      if (loginScreenView != null) {
        Platform.runLater(() -> loginScreenView.updateServerStatus(true));
      }
    }).exceptionally(ex -> {
      System.err.println("Failed to connect to server: " + ex.getMessage());
      isConnected = false;
      if (loginScreenView != null) {
        Platform.runLater(() -> loginScreenView.updateServerStatus(false));
      }
      return null;
    });

    dashboardView.initNetwork(api); // Initialize the network for the dashboard
    this.primaryStage = stage;
    primaryStage.setTitle("Green House Control");
    primaryStage.centerOnScreen();
    primaryStage.setMaximized(true);

    Font customFont = Font.loadFont(getClass().getResourceAsStream("/fonts/KaiseiDecol-Regular.ttf"), 10);
    if (customFont == null) {
      System.err.println("Error loading font");
    } else {
      System.out.println("Font is loaded: " + customFont.getFamily());
    }

    showSplashScreen();
    primaryStage.setMinHeight(SCENE_HEIGHT);
    primaryStage.setMinWidth(SCENE_WIDTH);
    primaryStage.show();
  }

  public int getServerPort() {
    return SERVER_PORT;
  }

  /**
   * Displays the splash screen view.
   */
  public void showSplashScreen() {
    SplashScreenView splash = new SplashScreenView(this);
    Scene scene = new Scene(splash.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Green House Control - Welcome");
    primaryStage.setMaximized(true);
  }

  /**
   * Displays the registration screen view.
   */
  public void showRegistrationScreen() {
    RegistrationView registration = new RegistrationView(this);
    Scene scene = new Scene(registration.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Green House Control - Register");
    primaryStage.setMaximized(true);
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
    primaryStage.setMaximized(true);
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
    // Use the *existing* dashboardView instance
    Scene scene = new Scene(dashboardView.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Smart Farm Control");
    primaryStage.setMaximized(true);
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