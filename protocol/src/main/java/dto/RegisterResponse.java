package dto;

/**
 * Represents the response to a user registration request.
 * Contains registration status, user ID, and an optional message.
 */
public class RegisterResponse {
    public String type = "register_response";
    public String id;
    public boolean success;
    public int userId;
    public String message;

    /**
     * Default constructor.
     */
    public RegisterResponse() {}

    /**
     * Constructs a RegisterResponse with all fields.
     * @param id Request identifier
     * @param success Registration success status
     * @param userId Registered user ID
     * @param message Optional message
     */
    public RegisterResponse(String id, boolean success, int userId, String message) {
        this.id = id;
        this.success = success;
        this.userId = userId;
        this.message = message;
    }

    /**
     * Gets the request identifier.
     * @return Request ID
     */
    public String getId() { return id; }

    /**
     * Sets the request identifier.
     * @param id Request ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns registration success status.
     * @return true if registration succeeded, false otherwise
     */
    public boolean isSuccess() { return success; }

    /**
     * Sets registration success status.
     * @param success true if registration succeeded
     */
    public void setSuccess(boolean success) { this.success = success; }

    /**
     * Gets the registered user ID.
     * @return User ID
     */
    public int getUserId() { return userId; }

    /**
     * Sets the registered user ID.
     * @param userId User ID
     */
    public void setUserId(int userId) { this.userId = userId; }

    /**
     * Gets the optional message.
     * @return Message
     */
    public String getMessage() { return message; }

    /**
     * Sets the optional message.
     * @param message Message
     */
    public void setMessage(String message) { this.message = message; }
}
