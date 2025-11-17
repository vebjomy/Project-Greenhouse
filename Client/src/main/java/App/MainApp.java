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
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

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
    private final int SERVER_PORT = 5555;
    private String dynamicServerAddress;

    @Override
    public void start(Stage stage) {
        api = new ClientApi();

        // Create the dashboard view
        dashboardView = new DashboardView(this, api);

        // Create the dashboard scene ONCE and cache it
        dashboardScene = new Scene(dashboardView.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);

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

        // Initiate the connection loop. This method will handle showing the first screen upon success.
        attemptInitialConnection(stage);

        // Show the stage immediately (it will be mostly empty until showSplashScreen is called)
        primaryStage.setMinHeight(SCENE_HEIGHT);
        primaryStage.setMinWidth(SCENE_WIDTH);
        primaryStage.show();
    }

    /**
     * Shows a dialog to get the IP address and attempts connection.
     * Recursively calls itself on connection failure, creating a loop until success or cancellation.
     */
    private void attemptInitialConnection(Stage stage) {
        // 1. Show IP input dialog
        TextInputDialog ipDialog = new TextInputDialog("127.0.0.1"); // <-- Вот ваш дефолтный IP
        ipDialog.setTitle("Server Connection Setup");
        ipDialog.setHeaderText("Enter Server IP Address");
        ipDialog.setContentText("IP:");

        Optional<String> result = ipDialog.showAndWait();
        String serverAddress = result.orElse(null); // <-- Здесь используется введенный IP

        // Handle cancellation or empty input by exiting the application
        if (serverAddress == null || serverAddress.trim().isEmpty()) {
            Platform.exit();
            return;
        }

        // 2. Attempt connection
        api.connect(serverAddress, SERVER_PORT).thenRun(() -> { // <-- Он используется здесь
            // SUCCESS: Connection established
            System.out.println("✅ Connected to server at " + serverAddress);
            isConnected = true;
            this.dynamicServerAddress = serverAddress; // <-- И сохраняется здесь

            // UI updates (showSplashScreen, subscribe, getTopology) must run on FX thread
            Platform.runLater(() -> {
                // Subscribe and get topology only after successful connection
                api.subscribe(List.of("*"), List.of("sensor_update", "node_change"))
                        .thenRun(() -> {
                            System.out.println("✅ Subscribed to updates");

                            api.getTopology().thenAccept(topology -> {
                                System.out.println("✅ Initial topology loaded: " +
                                        (topology.nodes != null ? topology.nodes.size() : 0) + " nodes");
                            });
                        });

                // Show the first view (Splash Screen)
                showSplashScreen(); // <-- Показывает UI *после* успеха

                // Update login status (if LoginScreen is shown later)
                if (loginScreenView != null) {
                    loginScreenView.updateServerStatus(true);
                }
            });

        }).exceptionally(ex -> {
            // FAILURE: Connection failed
            String errorMsg = "Failed to connect to server (" + serverAddress + "). " +
                    "Please check the IP address and ensure the server is running.";

            System.err.println("❌ Failed to connect to server: " + ex.getMessage());
            isConnected = false;

            Platform.runLater(() -> {
                // Show Error Alert
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Connection Error");
                alert.setHeaderText("Server Unavailable! Re-enter IP.");
                alert.setContentText(errorMsg);

                // Blocks until the user closes the alert
                alert.showAndWait();

                // 3. Loop: Call the function again to prompt for IP
                attemptInitialConnection(stage);
            });
            return null;
        });
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

    /**
     * Returns the dynamically entered server address.
     */
    public String getServerAddress() {
        return dynamicServerAddress;
    }

    /**
     * Gets the ClientApi instance for network operations
     * @return the ClientApi instance
     */
    public ClientApi getClientApi() {
        return api;
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