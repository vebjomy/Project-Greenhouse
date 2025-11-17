package controller;

import core.ClientApi;
import dto.RegisterRequest;
import dto.UsersListResponse;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import model.User;
import ui.UsersView;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for managing user-related actions in the client application.
 * Handles loading, adding, editing, and deleting users via the UI and communicates with the server API.
 */
public class UsersController {
  private final UsersView view;
  private final ClientApi clientApi;

  public UsersController(UsersView view, ClientApi clientApi) {
    this.view = view;
    this.clientApi = clientApi;
  }

  /**
   * Loads the list of users from the server and updates the user table in the view.
   */
  public void loadUsers() {
    clientApi.getUsers().thenAccept(users -> {
      Platform.runLater(() -> {
        view.getUserTable().getItems().clear();
        for (UsersListResponse.UserData userData : users) {
          User user = new User(userData.id, userData.username, "", userData.role);
          view.getUserTable().getItems().add(user);
        }
        System.out.println("✅ Loaded " + users.size() + " users");
      });
    }).exceptionally(ex -> {
      Platform.runLater(() ->
              showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users: " + ex.getMessage())
      );
      return null;
    });
  }

  /**
   * Opens dialogs to collect user information and sends a request to add a new user.
   */
  public void addUser() {
    // Username
    TextInputDialog usernameDialog = new TextInputDialog();
    usernameDialog.setTitle("Add User");
    usernameDialog.setHeaderText("Enter new username");
    usernameDialog.setContentText("Username:");
    Optional<String> usernameOpt = usernameDialog.showAndWait();
    if (usernameOpt.isEmpty()) return;
    String username = usernameOpt.get().trim();
    if (username.isEmpty()) {
      showAlert(Alert.AlertType.ERROR, "Invalid Input", "Username cannot be empty.");
      return;
    }

    // Password
    TextInputDialog passwordDialog = new TextInputDialog();
    passwordDialog.setTitle("Add User");
    passwordDialog.setHeaderText("Enter password for " + username);
    passwordDialog.setContentText("Password:");
    Optional<String> passwordOpt = passwordDialog.showAndWait();
    if (passwordOpt.isEmpty()) return;
    String password = passwordOpt.get();
    if (password.length() < 6) {
      showAlert(Alert.AlertType.ERROR, "Invalid Input", "Password must be at least 6 characters.");
      return;
    }

    // Role
    List<String> roles = Arrays.asList("Admin", "Operator", "Viewer");
    ChoiceDialog<String> roleDialog = new ChoiceDialog<>("Viewer", roles);
    roleDialog.setTitle("Add User");
    roleDialog.setHeaderText("Select role for " + username);
    roleDialog.setContentText("Role:");
    Optional<String> roleOpt = roleDialog.showAndWait();
    if (roleOpt.isEmpty()) return;
    String role = roleOpt.get();

    // Build and send register request
    RegisterRequest req = new RegisterRequest();
    req.id = UUID.randomUUID().toString();
    req.username = username;
    req.password = password;
    req.role = role;

    clientApi.sendRegisterMessage(req).thenAccept(resp -> {
      Platform.runLater(() -> {
        if (resp != null && resp.success) {
          User newUser = new User(resp.userId, username, "", role);
          view.getUserTable().getItems().add(newUser);
          showAlert(Alert.AlertType.INFORMATION, "Success", "User registered successfully.");
        } else {
          String msg = (resp != null) ? resp.message : "Unknown error";
          showAlert(Alert.AlertType.ERROR, "Registration Failed", msg);
        }
      });
    }).exceptionally(ex -> {
      Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Registration failed: " + ex.getMessage()));
      return null;
    });
  }

  /**
   * Opens dialogs to edit the selected user's details and updates the user on the server.
   */
  public void editUser() {
    User selectedUser = view.getUserTable().getSelectionModel().getSelectedItem();
    if (selectedUser == null) {
      showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to edit.");
      return;
    }

    TextInputDialog dialog = new TextInputDialog(selectedUser.getUsername());
    dialog.setTitle("Edit User");
    dialog.setHeaderText("Edit username for user ID: " + selectedUser.getId());
    dialog.setContentText("Username:");

    Optional<String> usernameResult = dialog.showAndWait();
    if (usernameResult.isEmpty()) return;
    String newUsername = usernameResult.get().trim();
    if (newUsername.isEmpty()) {
      showAlert(Alert.AlertType.ERROR, "Invalid Input", "Username cannot be empty.");
      return;
    }

    List<String> roles = Arrays.asList("Admin", "Operator", "Viewer");
    ChoiceDialog<String> roleDialog = new ChoiceDialog<>(selectedUser.getRole(), roles);
    roleDialog.setTitle("Edit User");
    roleDialog.setHeaderText("Select role for user ID: " + selectedUser.getId());
    roleDialog.setContentText("Role:");
    Optional<String> roleResult = roleDialog.showAndWait();
    if (roleResult.isEmpty()) return;
    String newRole = roleResult.get();

    clientApi.updateUser(selectedUser.getId(), newUsername, newRole)
            .thenRun(() -> {
              Platform.runLater(() -> {
                selectedUser.setUsername(newUsername);
                selectedUser.setRole(newRole);
                view.getUserTable().refresh();
                showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully!");
              });
            })
            .exceptionally(ex -> {
              Platform.runLater(() ->
                      showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user: " + ex.getMessage())
              );
              return null;
            });
  }

  /**
   * Deletes the selected user after confirmation and updates the view.
   */
  public void deleteUser() {
    User selectedUser = view.getUserTable().getSelectionModel().getSelectedItem();
    if (selectedUser == null) {
      showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to delete.");
      return;
    }

    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
    confirmDialog.setTitle("Confirm Delete");
    confirmDialog.setHeaderText("Delete user: " + selectedUser.getUsername() + "?");
    confirmDialog.setContentText("This action cannot be undone.");

    Optional<ButtonType> result = confirmDialog.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      clientApi.deleteUser(selectedUser.getId())
              .thenRun(() -> {
                Platform.runLater(() -> {
                  view.getUserTable().getItems().remove(selectedUser);
                  showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully!");
                });
              })
              .exceptionally(ex -> {
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user: " + ex.getMessage())
                );
                return null;
              });
    }
  }

  /**
   * Shows an alert dialog with the specified type, title, and message.
   *
   * @param type    the type of alert
   * @param title   the title of the alert
   * @param message the message to display
   */
  private void showAlert(Alert.AlertType type, String title, String message) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
