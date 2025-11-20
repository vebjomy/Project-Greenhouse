package dto;

import java.util.List;

/**
 * Represents a response containing a list of users. Used to transfer user data from the server to
 * the client.
 */
public class UsersListResponse {
  public String type = "users_list";
  public String id;
  public boolean success;
  public List<UserData> users;

  /** Represents a single user's data in the users list. */
  public static class UserData {
    public int id;
    public String username;
    public String role;
  }
}
