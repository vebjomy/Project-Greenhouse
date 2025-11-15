package dto;

public class RegisterResponse {
    public String type = "register_response";
    public String id;
    public boolean success;
    public int userId;
    public String message;

    public RegisterResponse() {}

    public RegisterResponse(String id, boolean success, int userId, String message) {
        this.id = id;
        this.success = success;
        this.userId = userId;
        this.message = message;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
