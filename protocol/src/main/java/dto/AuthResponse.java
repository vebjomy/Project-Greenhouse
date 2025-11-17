package dto;

public class AuthResponse {
    public String type = "auth_response";
    public String id;
    public boolean success;
    public int userId;
    public String role;
    public String message;

    public AuthResponse() {}

    public AuthResponse(String id, boolean success, int userId, String role, String message) {
        this.id = id;
        this.success = success;
        this.userId = userId;
        this.role = role;
        this.message = message;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
