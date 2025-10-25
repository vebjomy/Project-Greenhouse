package model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * A model class representing a user in the system.
 * It uses JavaFX Properties for easy integration with a TableView.
 */
public class User {
  private final SimpleIntegerProperty id;
  private final SimpleStringProperty username;
  private final SimpleStringProperty role;

  /** Constructor to initialize a User object with id, username, and role. */

  public User(int id, String username, String role) {
    this.id = new SimpleIntegerProperty(id);
    this.username = new SimpleStringProperty(username);
    this.role = new SimpleStringProperty(role);
  }

  // --- Getters and Property Getters ---

  public int getId() {
    return id.get();
  }

  public SimpleIntegerProperty idProperty() {
    return id;
  }

  public String getUsername() {
    return username.get();
  }

  public SimpleStringProperty usernameProperty() {
    return username;
  }

  public String getRole() {
    return role.get();
  }

  public SimpleStringProperty roleProperty() {
    return role;
  }
}