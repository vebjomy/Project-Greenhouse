package model;

/**
 * Simple data class for JSON serialization/deserialization
 */
public class UserDTO {
    private int id;
    private String username;
    private String role;

    // Default constructor for Jackson
    public UserDTO() {}

    public UserDTO(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    // Convert from User to UserDTO
    public static UserDTO fromUser(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getRole());
    }

    // Convert from UserDTO to User
    public User toUser() {
        return new User(id, username, role);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
