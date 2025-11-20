package dto;

/**
 * Represents an authentication request containing user credentials. Used to send authentication
 * data to the server.
 */
public class Auth {
  public String type = "auth";
  public String id;
  public String username;
  public String password;

  /** Default constructor. */
  public Auth() {}

  /**
   * Constructs an Auth request with all fields.
   *
   * @param id Request identifier
   * @param username Username
   * @param password Password
   */
  public Auth(String id, String username, String password) { // ✅ Fix constructor
    this.id = id;
    this.username = username;
    this.password = password;
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
   * Gets the username.
   *
   * @return Username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Gets the password.
   *
   * @return Password
   */
  public String getPassword() {
    return password;
  }
}
