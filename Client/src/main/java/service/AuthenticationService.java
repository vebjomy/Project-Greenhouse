package service;

import model.User;
import model.UserRegister;

/**
 * Service class for handling user authentication
 */
public class AuthenticationService {
    private final UserRegister userRegister;
    private User currentUser;

    public AuthenticationService() {
        this.userRegister = new UserRegister();
    }

    /**
     * Attempts to login a user with username and password
     * @param username the username
     * @param password the password
     * @return true if login successful, false otherwise
     */
    public boolean login(String username, String password) {
        for (User user : userRegister.getUserList()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                currentUser = user;
                return true;
            }
        }
        return false;
    }

    /**
     * Registers a new user
     * @param username the username
     * @param password the password
     * @param role the role (default: "Operator")
     * @return true if registration successful, false if username already exists
     */
    public boolean register(String username, String password, String role) {
        // Check if username already exists
        for (User user : userRegister.getUserList()) {
            if (user.getUsername().equals(username)) {
                return false; // Username already exists
            }
        }

        // Add new user
        userRegister.addUser(username, password, role != null ? role : "Operator");
        return true;
    }

    /**
     * Gets the currently logged-in user
     * @return the current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Logs out the current user
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Checks if a user is currently logged in
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Gets the UserRegister instance for user management
     * @return the UserRegister instance
     */
    public UserRegister getUserRegister() {
        return userRegister;
    }
}
