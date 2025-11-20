package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dto.UsersListResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing users stored in a JSON file ("users.json").
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Load users on startup (creating default admin/user if file absent).
 *   <li>Provide registration, update, deletion, and simple credential validation.
 *   <li>Persist mutations immediately (except initial seeding).
 * </ul>
 *
 * <p>Concurrency:</p>
 * <ul>
 *   <li>Public mutating and listing methods ("getAllUsers", "updateUser",
 *   "deleteUser") are synchronized.</li>
 *   <li>Registration and validation are not synchronized and may observe in flight changes.</li>
 * </ul>
 *
 * <p>All information including passwords is currently stored in plain text.</p>
 */
public class UserService {

  private static final String USERS_FILE = "users.json";
  private final ObjectMapper mapper = new ObjectMapper();
  private ArrayNode users;
  private final String usersFile;

  /**
   * Constructs a UserService and loads users from the default file, creating default users if
   * absent.
   */
  public UserService() {
    this.usersFile = USERS_FILE;
    loadUsers();
  }

  /**
   * Constructs a UserService and loads users from the specified file, creating default users if
   * absent.
   *
   * @param usersFile the path to the users JSON file
   */
  public UserService(String usersFile) {
    this.usersFile = usersFile;
    loadUsers();
  }

  /**
   * Load users from json file.
   */
  private void loadUsers() {
    try {
      File file = new File(usersFile);

      if (!file.exists()) {
        users = mapper.createArrayNode();
        createDefaultUsers();
        saveUsers();
        System.out.println("✅ Created users file: " + usersFile);
      } else {
        var node = mapper.readTree(file);
        if (node == null || !node.isArray()) {
          users = mapper.createArrayNode();
          System.out.println("⚠️ Users file was empty or malformed, initialized empty users list.");
        } else {
          users = (ArrayNode) node;
          System.out.println("✅ Loaded users file: " + usersFile);
          System.out.println("   Users loaded: " + users.size());
        }
      }
    } catch (IOException e) {
      System.err.println("❌ Failed to load users.json: " + e.getMessage());
      users = mapper.createArrayNode();
    }
  }


  /**
   * Create default users.
   */
  private void createDefaultUsers() {
    addUser("admin", "admin123", "Admin");
    addUser("user", "user123", "Viewer");
  }

  /**
   * Add a user to the in-memory list.
   *
   * @param username the username
   * @param password the password
   * @param role     the role
   */
  private void addUser(String username, String password, String role) {
    ObjectNode user = mapper.createObjectNode();
    user.put("id", users.size() + 1);
    user.put("username", username);
    user.put("password", password);
    user.put("role", role);
    users.add(user);
  }

  /**
   * Registers a new user and saves to file.
   *
   * @param username the username
   * @param password the password
   * @param role     the role
   * @return the new user ID
   */
  public int registerUser(String username, String password, String role) {
    // Generate new ID
    int newId = users.size() + 1;
    for (int i = 0; i < users.size(); i++) {
      int id = users.get(i).get("id").asInt();
      if (id >= newId) {
        newId = id + 1;
      }
    }

    // Create and add user
    ObjectNode user = mapper.createObjectNode();
    user.put("id", newId);
    user.put("username", username);
    user.put("password", password);
    user.put("role", role);
    users.add(user);

    // Save to file
    saveUsers();

    System.out.println("✅ User registered: " + username + " (ID: " + newId + ")");
    return newId;
  }

  /**
   * Save users to the JSON file.
   */
  private void saveUsers() {
    try {
      mapper.writerWithDefaultPrettyPrinter().writeValue(new File(usersFile), users);
      System.out.println("✅ Saved users to: " + usersFile);
    } catch (IOException e) {
      System.err.println("❌ Failed to save users: " + e.getMessage());
    }
  }

  /**
   * Validates user credentials.
   *
   * @param username the username
   * @param password the password
   * @return true if valid, false otherwise
   */
  public boolean validateUser(String username, String password) {
    for (int i = 0; i < users.size(); i++) {
      ObjectNode user = (ObjectNode) users.get(i);
      if (user.get("username").asText().equals(username)
          && user.get("password").asText().equals(password)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets user ID by username.
   *
   * @param username the username
   * @return the user ID, or -1 if not found
   */
  public int getUserId(String username) {
    for (int i = 0; i < users.size(); i++) {
      ObjectNode user = (ObjectNode) users.get(i);
      if (user.get("username").asText().equals(username)) {
        return user.get("id").asInt();
      }
    }
    return -1;
  }

  /**
   * Gets user role by username.
   *
   * @param username the username
   * @return the user role, or null if not found
   */
  public String getUserRole(String username) {
    for (int i = 0; i < users.size(); i++) {
      ObjectNode user = (ObjectNode) users.get(i);
      if (user.get("username").asText().equals(username)) {
        return user.get("role").asText();
      }
    }
    return null;
  }

  /**
   * Gets a snapshot list of all users (id, username, role). Thread-safe via synchronization;
   * returns a new list.
   *
   * @return list of user data
   */
  public synchronized List<UsersListResponse.UserData> getAllUsers() {
    List<UsersListResponse.UserData> list = new ArrayList<>();
    for (int i = 0; i < users.size(); i++) {
      ObjectNode u = (ObjectNode) users.get(i);
      UsersListResponse.UserData ud = new UsersListResponse.UserData();
      ud.id = u.get("id").asInt();
      ud.username = u.get("username").asText();
      ud.role = u.get("role").asText();
      list.add(ud);
    }
    return list;
  }

  /**
   * Updates a user's username and role by ID and persists changes.
   *
   * @param userId          target user ID
   * @param newUsername     new username
   * @param newRole         new role
   * @param currentUserRole role of the user performing the update (must be "Admin")
   * @return true if updated; false if not found or permission denied
   */
  public synchronized boolean updateUser(int userId, String newUsername, String newRole,
      String currentUserRole) {
    if (!"Admin".equalsIgnoreCase(currentUserRole)) {
      System.err.println("❌ Permission denied: Only Admin can update users.");
      return false;
    }
    for (int i = 0; i < users.size(); i++) {
      ObjectNode u = (ObjectNode) users.get(i);
      if (u.get("id").asInt() == userId) {
        u.put("username", newUsername);
        u.put("role", newRole);
        saveUsers();
        System.out.println(
            "✅ Updated user ID " + userId + " -> username: " + newUsername + ", role: " + newRole);
        return true;
      }
    }
    System.err.println("❌ Update failed: user ID " + userId + " not found");
    return false;
  }

  /**
   * Deletes a user by ID and persists changes.
   *
   * @param userId          target user ID
   * @param currentUserRole role of the user performing the deletion (must be "Admin")
   * @return true if deleted; false if not found or permission denied
   */
  public synchronized boolean deleteUser(int userId, String currentUserRole) {
    if (!"Admin".equalsIgnoreCase(currentUserRole)) {
      System.err.println("❌ Permission denied: Only Admin can delete users.");
      return false;
    }
    for (int i = 0; i < users.size(); i++) {
      ObjectNode u = (ObjectNode) users.get(i);
      if (u.get("id").asInt() == userId) {
        users.remove(i);
        saveUsers();
        System.out.println("✅ Deleted user ID " + userId);
        return true;
      }
    }
    System.err.println("❌ Delete failed: user ID " + userId + " not found");
    return false;
  }
}
