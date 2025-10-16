package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import model.User;
import ui.UsersView;
import java.util.Optional;

/**
 * Controller for the UsersView. Handles logic like adding,
 * editing, and deleting users.
 */
public class UsersController {
  private final UsersView view;
  private final ObservableList<User> userList = FXCollections.observableArrayList();
  private int nextId = 4;

  public UsersController(UsersView view) {
    this.view = view;
    this.view.getUserTable().setItems(userList);
  }

  /**
   * Loads the initial list of users (mock data).
   */
  public void loadUsers() {
    userList.addAll(
        new User(1, "john.doe", "Admin"),
        new User(2, "jane.smith", "Operator"),
        new User(3, "mike.jones", "Viewer")
    );
  }

  /**
   * Opens a dialog to add a new user.
   */
  public void addUser() {
    TextInputDialog dialog = new TextInputDialog("new.user");
    dialog.setTitle("Add New User");
    dialog.setHeaderText("Enter the username for the new user.");
    dialog.setContentText("Username:");

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(username -> {
      // In a real app, you would ask for role and password too
      userList.add(new User(nextId++, username, "Operator"));
    });
  }

  /**
   * Edits the selected user.
   */
  public void editUser() {
    User selectedUser = view.getUserTable().getSelectionModel().getSelectedItem();
    if (selectedUser != null) {

      // Edit logic (could open a new dialog)
      showAlert("Edit User", "Editing functionality is not implemented yet.");
    } else {
      showAlert("No Selection", "Please select a user to edit.");
    }
  }

  /**

   * Deletes the selected user.
   */
  public void deleteUser() {
    User selectedUser = view.getUserTable().getSelectionModel().getSelectedItem();
    if (selectedUser != null) {
      userList.remove(selectedUser);
    } else {
      showAlert("No Selection", "Please select a user to delete.");
    }
  }

  private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}