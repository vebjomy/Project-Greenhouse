package dto;

/**
 * Represents a user registration request. Contains user credentials and desired role for
 * registration.
 */
public class RegisterRequest {
  public String type = "register";
  public String id;
  public String username;
  public String password;
  public String role;

  /** Default constructor. */
  public RegisterRequest() {}

  /**
   * Constructs a RegisterRequest with all fields.
   *
   * @param id Request identifier
   * @param username Desired username
   * @param password Desired password
   * @param role Desired role
   */
  public RegisterRequest(String id, String username, String password, String role) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.role = role;
  }

  /**
   * Gets the request identifier.
   *
   * @return Request ID
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the request identifier.
   *
   * @param id Request ID
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the desired username.
   *
   * @return Username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the desired username.
   *
   * @param username Username
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Gets the desired password.
   *
   * @return Password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the desired password.
   *
   * @param password Password
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Gets the desired role.
   *
   * @return Role
   */
  public String getRole() {
    return role;
  }

  /**
   * Sets the desired role.
   *
   * @param role Role
   */
  public void setRole(String role) {
    this.role = role;
  }
}
