package controller;

import javafx.scene.control.Alert;
import service.AuthenticationService;
import ui.RegistrationView;

/**
 * Controller for the RegistrationView
 */
public class RegisterController {
    private final RegistrationView view;
    private final AuthenticationService authService;

    public RegisterController(RegistrationView view) {
        this.view = view;
        this.authService = new AuthenticationService();
    }

    /**
     * Handles user registration
     * @return true if registration successful, false otherwise
     */
    public boolean handleRegister() {
        String username = view.getUsernameField().getText().trim();
        String password = view.getPasswordField().getText();
        String confirmPassword = view.getConfirmPasswordField().getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match.");
            return false;
        }

        if (password.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters long.");
            return false;
        }

        if (authService.register(username, password, "Operator")) {
            showAlert("Success", "Registration successful! You can now login.");
            return true;
        } else {
            showAlert("Error", "Username already exists. Please choose a different username.");
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
