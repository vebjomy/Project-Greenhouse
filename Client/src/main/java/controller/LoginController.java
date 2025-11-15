package controller;

import App.MainApp;
import javafx.scene.control.Alert;
import service.AuthenticationService;
import ui.LoginScreenView;
import core.ClientApi;
import javafx.application.Platform;

/**
 * Controller for the LoginScreenView
 */
public class LoginController {
    private final LoginScreenView view;
    private final AuthenticationService authService;
    private final ClientApi clientApi;
    private final MainApp mainApp;

    public LoginController(LoginScreenView view, ClientApi clientApi, MainApp mainApp) {
        this.view = view;
        this.clientApi = clientApi;
        this.authService = new AuthenticationService();
        this.authService.setClientApi(clientApi);
        this.mainApp = mainApp;
    }

    /**
     * Handles user login
     * @return true if login successful, false otherwise
     */
    public void handleLogin() {
        String username = view.getUsernameField().getText().trim();
        String password = view.getPasswordField().getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required.");
            return;
        }

        // Check server connection
        if (!mainApp.isConnected()) {
            showError("Server is offline. Cannot log in.");
            return;
        }

        authService.login(username, password).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.success) {
                    System.out.println("✅ Login successful - Role: " + response.getRole());
                    mainApp.showDashboard(); // ✅ Navigate to dashboard
                } else {
                    showError(response.getMessage());
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                showError("Login failed: " + ex.getMessage());
            });
            return null;
        });
    }

    /**
     * Gets the authentication service
     */
    public AuthenticationService getAuthService() {
        return authService;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
