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
 *
 * @author Green House Control Team
 * @version 3.0
 */
public class MainApp extends Application {
  private Stage primaryStage;
  private LoginScreenView loginScreenView;
  private DashboardView dashboardView;
  private Scene dashboardScene; // Cache the dashboard scene

  private static final double SCENE_WIDTH = 1480;
  private static final double SCENE_HEIGHT = 1000;

  private ClientApi api;
  private boolean isConnected = false;
  private final String SERVER_ADDRESS = "127.0.0.1";
  private final int SERVER_PORT = 5555;

  @Override
  public void start(Stage stage) {
    api = new ClientApi();

    // Create the dashboard view ONCE
    dashboardView = new DashboardView(this, api);

    // Create the dashboard scene ONCE and cache it
    dashboardScene = new Scene(dashboardView.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);

    // Connect to server with proper timing
    api.connect(SERVER_ADDRESS, SERVER_PORT).thenRun(() -> {
      System.out.println("✅ Connected to server");
      isConnected = true;

      // IMPORTANT: First subscribe, THEN request topology
      Platform.runLater(() -> {
        // Subscribe to updates FIRST
        api.subscribe(List.of("*"), List.of("sensor_update", "node_change"))
                .thenRun(() -> {
                  System.out.println("✅ Subscribed to updates");

                  // THEN get topology
                  api.getTopology().thenAccept(topology -> {
                    System.out.println("✅ Initial topology loaded: " +
                            (topology.nodes != null ? topology.nodes.size() : 0) + " nodes");
                  });
                });

        // Update login screen status
        if (loginScreenView != null) {
          loginScreenView.updateServerStatus(true);
        }
      });
    }).exceptionally(ex -> {
      System.err.println("❌ Failed to connect to server: " + ex.getMessage());
      isConnected = false;
      if (loginScreenView != null) {
        Platform.runLater(() -> loginScreenView.updateServerStatus(false));
      }
      return null;
    });

    // Initialize network AFTER UI is set up
    dashboardView.initNetwork(api);

    this.primaryStage = stage;
    primaryStage.setTitle("Green House Control");
    primaryStage.centerOnScreen();
    primaryStage.setMaximized(true);

    // Load custom font
    Font customFont = Font.loadFont(
            getClass().getResourceAsStream("/fonts/KaiseiDecol-Regular.ttf"), 10
    );
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
    // Create NEW instance of login screen each time
    loginScreenView = new LoginScreenView(this);
    loginScreenView.updateServerStatus(isConnected);

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
   * Displays the dashboard view by reusing the cached scene.
   * This prevents "already set as root" error.
   */
  public void showDashboard() {
    // Simply switch to the cached dashboard scene
    primaryStage.setScene(dashboardScene);
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