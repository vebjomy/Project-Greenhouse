package App;

import core.ClientApi;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import ui.DashboardView;
import ui.LoginScreenView;
import ui.RegistrationView;
import ui.SplashScreenView;

import java.util.List;
import java.util.Optional;

/**
 * The `MainApp` class serves as the entry point for the Green House Control application.
 *
 * @author Green House Control Team
 * @version 3.4
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

        this.primaryStage = stage;
        primaryStage.setTitle("Green House Control");

        // Load custom font
        Font customFont = Font.loadFont(
            getClass().getResourceAsStream("/fonts/KaiseiDecol-Regular.ttf"), 10
        );
        if (customFont == null) {
            System.err.println("Error loading font");
        } else {
            System.out.println("Font is loaded: " + customFont.getFamily());
        }


        // Show the stage immediately
        primaryStage.setMinHeight(SCENE_HEIGHT);
        primaryStage.setMinWidth(SCENE_WIDTH);

        //  Sentralize the stage on the screen
        centerStageOnScreen();

        // Minimum size
        primaryStage.setMinHeight(1100);
        primaryStage.setMinWidth(800);

        // Initiate the connection loop.
        attemptInitialConnection(stage);
        primaryStage.show();
    }

    /**
     * Centers the primary stage on the screen.
     */

    private void centerStageOnScreen() {
        primaryStage.centerOnScreen();
    }

    /**
     * Shows a styled dialog to get the IP address and attempts connection.
     */
    private void attemptInitialConnection(Stage stage) {
        // --- VISUAL DESIGN START (MATERIAL STYLE) ---
        Dialog<String> ipDialog = new Dialog<>();
        ipDialog.setTitle("Server Connection Setup");
        ipDialog.setHeaderText(null);

        DialogPane dialogPane = ipDialog.getDialogPane();
        // Inline CSS for Material Design
        dialogPane.setStyle(
            "-fx-background-color: #FFFFFF;" +
                "-fx-font-family: 'Segoe UI', sans-serif;" +
                "-fx-font-size: 14px;"
        );

        // Layout
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #FFFFFF;");

        // Labels
        Label headerLabel = new Label("Connect to Server");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        headerLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label descLabel = new Label("Enter the server IP address you want to connect to:");
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #7f8c8d;");

        // Input (Dropdown + Type)
        ComboBox<String> ipComboBox = new ComboBox<>();
        ipComboBox.setEditable(true);
        ipComboBox.getItems().addAll("127.0.0.1", "localhost");
        ipComboBox.getSelectionModel().selectFirst();
        ipComboBox.setPrefWidth(350);
        ipComboBox.setStyle(
            "-fx-background-color: #fdfdfd;" +
                "-fx-border-color: #bdc3c7;" +
                "-fx-border-radius: 4px;"
        );

        layout.getChildren().addAll(headerLabel, descLabel, ipComboBox);
        dialogPane.setContent(layout);

        // Buttons
        ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

        Button connectBtn = (Button) dialogPane.lookupButton(connectButtonType);
        connectBtn.setStyle(
            "-fx-background-color: #2980b9;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4px;" +
                "-fx-cursor: hand;"
        );

        ipDialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                return ipComboBox.getEditor().getText();
            }
            return null;
        });
        // --- VISUAL DESIGN END ---

        Optional<String> result = ipDialog.showAndWait();
        String serverAddress = result.orElse(null);

        if (serverAddress == null || serverAddress.trim().isEmpty()) {
            Platform.exit();
            return;
        }

        // Update dynamic server address
        this.dynamicServerAddress = serverAddress;
        System.out.println("🔧 Server address set to: " + this.dynamicServerAddress);

        // Connection Logic
        api.connect(serverAddress, SERVER_PORT).thenRun(() -> {
            System.out.println("✅ Connected to server at " + serverAddress);
            isConnected = true;

            Platform.runLater(() -> {
                if (dashboardView == null) {
                    dashboardView = new DashboardView(this, api);
                    dashboardScene = new Scene(dashboardView.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
                    dashboardView.initNetwork(api);
                }

                api.subscribe(List.of("*"), List.of("sensor_update", "node_change"))
                        .thenRun(() -> {
                            System.out.println("✅ Subscribed to updates");
                            api.getTopology().thenAccept(topology -> {
                                System.out.println("✅ Initial topology loaded: " +
                                        (topology.nodes != null ? topology.nodes.size() : 0) + " nodes");
                            });
                        });

                showSplashScreen();

                if (loginScreenView != null) {
                    loginScreenView.updateServerStatus(true);
                }
            });

        }).exceptionally(ex -> {
            String errorMsg = "Failed to connect to server (" + serverAddress + "). " +
                "Please check the IP address and ensure the server is running.";
            System.err.println("❌ Failed to connect to server: " + ex.getMessage());
            isConnected = false;

            //Reset dynamic server address on failure
            this.dynamicServerAddress = null;

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Connection Error");
                alert.setHeaderText("Server Unavailable!");
                alert.setContentText(errorMsg);
                alert.getDialogPane().setStyle("-fx-font-family: 'Segoe UI', sans-serif;");
                alert.showAndWait();

                attemptInitialConnection(stage);
            });
            return null;
        });
    }

    public int getServerPort() {
        return SERVER_PORT;
    }

    public void showSplashScreen() {
        SplashScreenView splash = new SplashScreenView(this);
        Scene scene = new Scene(splash.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Green House Control - Welcome");
        centerStageOnScreen();
    }

    public void showRegistrationScreen() {
        RegistrationView registration = new RegistrationView(this);
        Scene scene = new Scene(registration.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Green House Control - Register");
        centerStageOnScreen();
    }

    public void showLoginScreen() {
        loginScreenView = new LoginScreenView(this);
        loginScreenView.updateServerStatus(isConnected);

        Scene scene = new Scene(loginScreenView.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Green House Control - Login");
        centerStageOnScreen();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getServerAddress() {
        return dynamicServerAddress;
    }


    public ClientApi getClientApi() {
        return api;
    }

    /**
     * Displays the dashboard view.
     */
    public void showDashboard(String username) {
        // Create dashboard if it doesn't exist yet
        if (dashboardView == null) {
            dashboardView = new DashboardView(this, api);
            dashboardScene = new Scene(dashboardView.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
            dashboardView.initNetwork(api);
            centerStageOnScreen();
        }

        dashboardView.setUserGreeting(username);
        primaryStage.setScene(dashboardScene);
        primaryStage.setTitle("Smart Farm Control");
    }


    public static void main(String[] args) {
        launch(args);
    }
}