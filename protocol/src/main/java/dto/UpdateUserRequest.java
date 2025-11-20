package dto;

/**
 * Represents a request to update user information.
 * Contains user ID, new username, and new role.
 */
public class UpdateUserRequest {
    public String type = "update_user";
    public String id;
    public int userId;
    public String username;
    public String role;
}
