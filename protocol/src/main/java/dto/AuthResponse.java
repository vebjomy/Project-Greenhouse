package dto;

/**
 * Represents the response to an authentication request. Contains information about authentication
 * success, user ID, role, and an optional message.
 */
public class AuthResponse {
  public String type = "auth_response";
  public String id;
  public boolean success;
  public int userId;
  public String role;
  public String message;

  /** Default constructor. */
  public AuthResponse() {}

  /**
   * Constructs an AuthResponse with all fields.
   *
   * @param id Request identifier
   * @param success Authentication success status
   * @param userId Authenticated user ID
   * @param role User role
   * @param message Optional message
   */
  public AuthResponse(String id, boolean success, int userId, String role, String message) {
    this.id = id;
    this.success = success;
    this.userId = userId;
    this.role = role;
    this.message = message;
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
   * Returns authentication success status.
   *
   * @return true if authentication succeeded, false otherwise
   */
  public boolean isSuccess() {
    return success;
  }

  /**
   * Sets authentication success status.
   *
   * @param success true if authentication succeeded
   */
  public void setSuccess(boolean success) {
    this.success = success;
  }

  /**
   * Gets the authenticated user ID.
   *
   * @return User ID
   */
  public int getUserId() {
    return userId;
  }

  /**
   * Sets the authenticated user ID.
   *
   * @param userId User ID
   */
  public void setUserId(int userId) {
    this.userId = userId;
  }

  /**
   * Gets the user role.
   *
   * @return User role
   */
  public String getRole() {
    return role;
  }

  /**
   * Sets the user role.
   *
   * @param role User role
   */
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * Gets the optional message.
   *
   * @return Message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the optional message.
   *
   * @param message Message
   */
  public void setMessage(String message) {
    this.message = message;
  }
}
