package model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Logic/Service class for User entity operations.
 * Handles business logic for managing users.
 */
public class UserRegister {
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String JSON_FILE_PATH = "src/main/resources/data/users.json";
    private int nextId = 1;

    public UserRegister() {
        loadUsersFromFile();
//        loadUsers();
    }

     /**
      *  Adds a new user with username, password, and role.
      */
    public void addUser(String username, String password, String role) {
        userList.add(new User(nextId++, username, password, role));
        saveUsersToFile();
    }

    /**
     * Loads users from JSON file. If file doesn't exist, loads default users.
     */
    private void loadUsersFromFile() {
        try {
            File file = new File(JSON_FILE_PATH);
            if (file.exists() && file.length() > 0) {
                List<UserDTO> userDTOs = objectMapper.readValue(file, new TypeReference<List<UserDTO>>() {});
                List<User> users = userDTOs.stream().map(UserDTO::toUser).toList();
                userList.addAll(users);
                nextId = users.stream().mapToInt(User::getId).max().orElse(0) + 1;
            } else {
                loadDefaultUsers();
                saveUsersToFile();
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load users from file: " + e.getMessage());
            loadDefaultUsers();
        }
    }

    /**
     * Loads default users when JSON file doesn't exist.
     */
    private void loadDefaultUsers() {
        userList.addAll(List.of(
                new User(1, "john.doe", "password123", "Admin"),
                new User(2, "jane.smith", "password123", "Operator"),
                new User(3, "mike.jones", "password123", "Viewer")
        ));
        nextId = 4;
    }

    /**
     * Saves current user list to JSON file.
     */
    private void saveUsersToFile() {
        try {
            List<UserDTO> userDTOs = userList.stream().map(UserDTO::fromUser).toList();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(JSON_FILE_PATH), userDTOs);
        } catch (IOException e) {
            showAlert("Error", "Failed to save users to file: " + e.getMessage());
        }
    }

    /**
     * Adds a new user through dialog interaction and saves to file.
     */
    public void addUser() {
        TextInputDialog usernameDialog = new TextInputDialog("new.user");
        usernameDialog.setTitle("Add New User");
        usernameDialog.setHeaderText("Enter the username for the new user.");
        usernameDialog.setContentText("Username:");

        Optional<String> usernameResult = usernameDialog.showAndWait();
        if (usernameResult.isPresent()) {
            TextInputDialog passwordDialog = new TextInputDialog();
            passwordDialog.setTitle("Add New User");
            passwordDialog.setHeaderText("Enter the password for the new user.");
            passwordDialog.setContentText("Password:");

            Optional<String> passwordResult = passwordDialog.showAndWait();
            if (passwordResult.isPresent()) {
                userList.add(new User(nextId++, usernameResult.get(), passwordResult.get(), "Operator"));
                saveUsersToFile();
            }
        }
    }


    /**
     * Edits the selected user.
     */
    public void editUser(User selectedUser) {
        if (selectedUser != null) {
            showAlert("Edit User", "Editing functionality is not implemented yet.");
        } else {
            showAlert("No Selection", "Please select a user to edit.");
        }
    }

    /**
     * Deletes the selected user and saves to file.
     */
    public void deleteUser(User selectedUser) {
        if (selectedUser != null) {
            userList.remove(selectedUser);
            saveUsersToFile();
        } else {
            showAlert("No Selection", "Please select a user to delete.");
        }
    }

    /**
     * Gets the observable list of users for UI binding.
     */
    public ObservableList<User> getUserList() {
        return userList;
    }

    /**
     * Utility method to show an alert dialog.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
