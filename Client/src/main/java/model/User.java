package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Entity class representing a user in the system.
 * Contains JavaFX properties for UI binding.
 */
public class User {
  private final IntegerProperty id;
  private final StringProperty username;
  private final StringProperty role;

  public User(int id, String username, String role) {
    this.id = new SimpleIntegerProperty(id);
    this.username = new SimpleStringProperty(username);
    this.role = new SimpleStringProperty(role);
  }

  // Property methods for JavaFX binding
  public IntegerProperty idProperty() {
    return id;
  }

  public StringProperty usernameProperty() {
    return username;
  }

  public StringProperty roleProperty() {
    return role;
  }

  // Getters and Setters
  public int getId() {
    return id.get();
  }

  public void setId(int id) {
    this.id.set(id);
  }

  public String getUsername() {
    return username.get();
  }

  public void setUsername(String username) {
    this.username.set(username);
  }

  public String getRole() {
    return role.get();
  }

  public void setRole(String role) {
    this.role.set(role);
  }

  @Override
  public String toString() {
    return "User{id=" + getId() + ", username='" + getUsername() + "', role='" + getRole() + "'}";
  }
}
