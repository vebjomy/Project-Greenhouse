package controller;

import App.MainApp;
import javafx.scene.control.Alert;
import service.AuthenticationService;
import ui.LoginScreenView;
import core.ClientApi;
import javafx.application.Platform;

/**
 * Controller for handling user login logic and interactions with the LoginScreenView.
 * Manages user input validation, communicates with the authentication service, and updates the UI.
 */
public class LoginController {
    private final LoginScreenView view;
    private final AuthenticationService authService;
    private final ClientApi clientApi;
    private final MainApp mainApp;

    /**
     * Constructs a LoginController with the given view, API client, and main application reference.
     *
     * @param view      the login screen view UI
     * @param clientApi the client API for server communication
     * @param mainApp   the main application instance for navigation
     */
    public LoginController(LoginScreenView view, ClientApi clientApi, MainApp mainApp) {
        this.view = view;
        this.clientApi = clientApi;
        this.authService = new AuthenticationService();
        this.authService.setClientApi(clientApi);
        this.mainApp = mainApp;
    }

    /**
     * Handles the login process by validating user input,
     * sending login requests, and updating the UI based on the result.
     *
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
                    mainApp.showDashboard(username); // ✅ Navigate to dashboard
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
     * Returns the authentication service used for login operations.
     *
     * @return the authentication service
     */
    public AuthenticationService getAuthService() {
        return authService;
    }

    /**
     * Displays an error alert dialog with the specified message.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
