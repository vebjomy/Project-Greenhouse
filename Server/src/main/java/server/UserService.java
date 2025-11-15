package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.nio.file.*;

public class UserService {
    private static final String USERS_FILE = "users.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private ArrayNode users;

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


    private void createDefaultUsers() {
        addUser("admin", "admin123", "admin");
        addUser("user", "user123", "user");
    }

    private void addUser(String username, String password, String role) {
        ObjectNode user = mapper.createObjectNode();
        user.put("id", users.size() + 1);
        user.put("username", username);
        user.put("password", password);
        user.put("role", role);
        users.add(user);
    }

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


    private void saveUsers() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(USERS_FILE), users);
            System.out.println("✅ Saved users to root directory");
        } catch (IOException e) {
            System.err.println("❌ Failed to save users: " + e.getMessage());
        }
    }


    public boolean validateUser(String username, String password) {
        for (int i = 0; i < users.size(); i++) {
            ObjectNode user = (ObjectNode) users.get(i);
            if (user.get("username").asText().equals(username) &&
                    user.get("password").asText().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public int getUserId(String username) {
        for (int i = 0; i < users.size(); i++) {
            ObjectNode user = (ObjectNode) users.get(i);
            if (user.get("username").asText().equals(username)) {
                return user.get("id").asInt();
            }
        }
        return -1;
    }

    public String getUserRole(String username) {
        for (int i = 0; i < users.size(); i++) {
            ObjectNode user = (ObjectNode) users.get(i);
            if (user.get("username").asText().equals(username)) {
                return user.get("role").asText();
            }
        }
        return null;
    }
}
