package service;

import core.ClientApi;
import dto.Auth;
import dto.AuthResponse;
import dto.RegisterRequest;
import dto.RegisterResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import model.User;

/**
 * Service class for handling user authentication.
 */
public class AuthenticationService {

  private User currentUser;
  private ClientApi clientApi;

  /**
   * Default contructor.
   */
  public AuthenticationService() {
  }

  public void setClientApi(ClientApi api) {
    this.clientApi = api;
  }

  /**
   * Authenticates user with server via TCP.
   */
  public CompletableFuture<AuthResponse> login(String username, String password) {
    if (clientApi == null) {
      CompletableFuture<AuthResponse> fut = new CompletableFuture<>();
      fut.completeExceptionally(new IllegalStateException("ClientApi not set"));
      return fut;
    }

    Auth authRequest = new Auth(UUID.randomUUID().toString(), username, password);

    return clientApi.sendAuthMessage(authRequest).thenApply(response -> {
      if (response.success) {
        // Create local user object
        currentUser = new User(response.userId, username, password, response.role);
        System.out.println("✅ Login successful - User: " + username);
      } else {
        System.out.println("❌ Login failed: " + response.message);
      }
      return response;
    });
  }

  /**
   * Registers new user with server via TCP.
   */
  public CompletableFuture<RegisterResponse> register(String username, String password,
      String role) {
    if (clientApi == null) {
      CompletableFuture<RegisterResponse> fut = new CompletableFuture<>();
      fut.completeExceptionally(new IllegalStateException("ClientApi not set"));
      return fut;
    }

    RegisterRequest request = new RegisterRequest(
        UUID.randomUUID().toString(),
        username,
        password,
        role != null ? role : "Admin"
    );

    return clientApi.sendRegisterMessage(request).thenApply(response -> {
      if (response.success) {
        System.out.println("✅ Registration successful - UserID: " + response.userId);
      } else {
        System.out.println("❌ Registration failed: " + response.message);
      }
      return response;
    });
  }

  /**
   * Gets the currently logged-in user.
   *
   * @return the current user or null if not logged in
   */
  public User getCurrentUser() {
    return currentUser;
  }

  /**
   * Logs out the current user.
   */
  public void logout() {
    currentUser = null;
  }

  /**
   * Checks if a user is currently logged in.
   *
   * @return true if logged in, false otherwise
   */
  public boolean isLoggedIn() {
    return currentUser != null;
  }

}
