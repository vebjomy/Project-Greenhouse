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
 * Service for managing users stored in a JSON file (`users.json`).
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Load users on startup (creating default admin/user if file absent).
 *   <li>Provide registration, update, deletion, and simple credential validation.
 *   <li>Persist mutations immediately (except initial seeding).
 * </ul>
 *
 * <p>Concurrency: public mutating and listing methods (`getAllUsers`, `updateUser`, `deleteUser`)
 * are synchronized; registration and validation are not synchronized and may observe in-flight
 * changes. All information including passwords is currently stored in plain text.
 */
public class UserService {
  private static final String USERS_FILE = "users.json";
  private final ObjectMapper mapper = new ObjectMapper();
  private ArrayNode users;

  /** Constructor loads users from file or creates default users if file absent. */
  public UserService() {
    loadUsers();
  }

  private void loadUsers() {
    try {
      File file = new File(USERS_FILE);

      if (!file.exists()) {
        users = mapper.createArrayNode();
        createDefaultUsers();
        saveUsers();
        System.out.println("✅ Created users.json in root directory");
      } else {
        users = (ArrayNode) mapper.readTree(file);
        System.out.println("✅ Loaded users.json from root directory");
        System.out.println("   Users loaded: " + users.size());
      }
    } catch (IOException e) {
      System.err.println("❌ Failed to load users.json: " + e.getMessage());
      users = mapper.createArrayNode();
    }
  }

  /** Create default users. */
  private void createDefaultUsers() {
    addUser("admin", "admin123", "Admin");
    addUser("user", "user123", "Viewer");
  }

  /**
   * Add a user to the in-memory list.
   *
   * @param username the username
   * @param password the password
   * @param role the role
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
   * Register a new user and save to file.
   *
   * @param username the username
   * @param password the password
   * @param role the role
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

  /** Save users to the JSON file. */
  private void saveUsers() {
    try {
      mapper.writerWithDefaultPrettyPrinter().writeValue(new File(USERS_FILE), users);
      System.out.println("✅ Saved users to root directory");
    } catch (IOException e) {
      System.err.println("❌ Failed to save users: " + e.getMessage());
    }
  }

  /**
   * Validate user credentials.
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
   * Get user ID by username.
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
   * Get user role by username.
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
   * Get a snapshot list of all users (id, username, role). Thread-safe via synchronization; returns
   * a new list.
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
   * Update a user's username and role by ID and persist.
   *
   * @param userId target user ID
   * @param newUsername new username
   * @param newRole new role
   * @return true if updated; false if not found
   */
  public synchronized boolean updateUser(int userId, String newUsername, String newRole) {
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
   * Delete a user by ID and persist changes.
   *
   * @param userId target user ID
   * @return true if deleted; false if not found
   */
  public synchronized boolean deleteUser(int userId) {
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
