package controller;


import App.MainApp;
import core.ClientApi;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import service.AuthenticationService;
import ui.RegistrationView;


/**
 * Controller for handling user registration logic and interactions with the RegistrationView.
 * Manages user input validation, communicates with the authentication service, and updates the UI.
 */
public class RegisterController {
  private final RegistrationView view;
  private final AuthenticationService authService;
  private final ClientApi clientApi;
  private final MainApp mainApp;

  /**
   * Constructs a RegisterController with the given view, API client,
   * and main application reference.
   *
   * @param view      the registration view UI
   * @param clientApi the client API for server communication
   * @param mainApp   the main application instance for navigation
   */
  public RegisterController(RegistrationView view, ClientApi clientApi, MainApp mainApp) {
    this.view = view;
    this.clientApi = clientApi;
    this.authService = new AuthenticationService();
    this.authService.setClientApi(clientApi);
    this.mainApp = mainApp;
  }


  /**
   * Handles the registration process by validating user input,
   * sending registration requests, and updating the UI based on the result.
   */
  public void handleRegister() {
    String username = view.getUsernameField().getText().trim();
    String password = view.getPasswordField().getText();
    String confirmPassword = view.getConfirmPasswordField().getText();
    if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
      showAlert("Error", "Please fill in all fields.");
      return;
    }

    if (!password.equals(confirmPassword)) {
      showAlert("Error", "Passwords do not match.");
      return;
    }

    if (password.length() < 6) {
      showAlert("Error", "Password must be at least 6 characters long.");
      return;
    }
    authService.register(username, password, "Admin").thenAccept(response -> {
      Platform.runLater(() -> {
        if (response.success) {
          showAlert("Success", "Registration successful! You can now login.");
          mainApp.showLoginScreen();  // ✅ Navigate to login
        } else {
          showAlert("Error", response.message);
        }
      });
    }).exceptionally(ex -> {
      Platform.runLater(() -> showAlert("Error", "Connection failed: " + ex.getMessage()));
      return null;
    });
  }

  /**
   * Displays an alert dialog with the specified title and message.
   *
   * @param title   the title of the alert
   * @param message the message to display
   */
  private void showAlert(String title, String message) {
    Alert alert = new Alert(title.equals("Success") ? Alert.AlertType.INFORMATION :
            Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
