package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Entity class representing a user in the system. Contains JavaFX properties for UI binding.
 */
public class User {

  private final IntegerProperty id;
  private final StringProperty username;
  private final StringProperty password;
  private final StringProperty role;

  /**
   * Constructor to initialize User object with given parameters.
   *
   * @param id       Unique identifier for the user
   * @param username User's username
   * @param password User's password
   * @param role     User's role in the system
   */
  public User(int id, String username, String password, String role) {
    this.id = new SimpleIntegerProperty(id);
    this.username = new SimpleStringProperty(username);
    this.password = new SimpleStringProperty(password);
    this.role = new SimpleStringProperty(role);
  }

  // Property methods for JavaFX binding

  /**
   * Gets the ID property of the user.
   *
   * @return The ID property
   */
  public IntegerProperty idProperty() {
    return id;
  }

  /**
   * Gets the username property of the user.
   *
   * @return The username property
   */
  public StringProperty usernameProperty() {
    return username;
  }

  /**
   * Gets the password property of the user.
   *
   * @return The password property
   */
  public StringProperty passwordProperty() {
    return password;
  }

  /**
   * Gets the role property of the user.
   *
   * @return The role property
   */
  public StringProperty roleProperty() {
    return role;
  }

  // Getters and Setters

  /**
   * Gets the ID of the user.
   *
   * @return The ID of the user
   */
  public int getId() {
    return id.get();
  }

  /**
   * Sets the ID of the user.
   *
   * @param id The new ID to assign to the user
   */
  public void setId(int id) {
    this.id.set(id);
  }

  /**
   * Gets the username of the user.
   *
   * @return The username of the user
   */
  public String getUsername() {
    return username.get();
  }

  /**
   * Sets the username of the user.
   *
   * @param username The new username to assign to the user
   */
  public void setUsername(String username) {
    this.username.set(username);
  }

  /**
   * Gets the password of the user.
   *
   * @return The password of the user
   */
  public String getPassword() {
    return password.get();
  }

  /**
   * Sets the password of the user.
   *
   * @param password The new password to assign to the user
   */
  public void setPassword(String password) {
    this.password.set(password);
  }

  /**
   * Gets the role of the user.
   *
   * @return The role of the user
   */
  public String getRole() {
    return role.get();
  }

  /**
   * Sets the role of the user.
   *
   * @param role The new role to assign to the user
   */
  public void setRole(String role) {
    this.role.set(role);
  }


  @Override
  public String toString() {
    return "User{id=" + getId() + ", username='" + getUsername() + "', role='" + getRole() + "'}";
  }
}
