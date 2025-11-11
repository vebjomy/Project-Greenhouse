package controller;

import javafx.scene.control.Alert;
import service.AuthenticationService;
import ui.LoginScreenView;

/**
 * Controller for the LoginScreenView
 */
public class LoginController {
    private final LoginScreenView view;
    private final AuthenticationService authService;

    public LoginController(LoginScreenView view) {
        this.view = view;
        this.authService = new AuthenticationService();
    }

    /**
     * Handles user login
     * @return true if login successful, false otherwise
     */
    public boolean handleLogin() {
        String username = view.getUsernameField().getText().trim();
        String password = view.getPasswordField().getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return false;
        }

        if (authService.login(username, password)) {
            showAlert("Success", "Login successful! Welcome, " + username);
            return true;
        } else {
            showAlert("Error", "Invalid username or password.");
            return false;
        }
    }

    /**
     * Gets the authentication service
     */
    public AuthenticationService getAuthService() {
        return authService;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
