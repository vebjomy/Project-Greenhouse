package dto;

/**
 * Represents a request to update user information.
 * Contains user ID, new username, and new role.
 */
public class UpdateUserRequest {
    /** Message type identifier. */
    public String type = "update_user";
    /** Request identifier. */
    public String id;
    /** User ID to update. */
    public int userId;
    /** New username for the user. */
    public String username;
    /** New role for the user. */
    public String role;
}
