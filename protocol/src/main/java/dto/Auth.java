
package dto;

public class Auth {
    public String type = "auth";
    public String id;
    public String username;
    public String password;

    public Auth() {}

    public Auth(String id, String username, String password) {  // ✅ Fix constructor
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}