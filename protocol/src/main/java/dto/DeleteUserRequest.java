package dto;

/** Represents a request to delete a user. Contains the request ID and the user ID to be deleted. */
public class DeleteUserRequest {
  /** Message type identifier. */
  public String type = "delete_user";

  /** Request identifier. */
  public String id;

  /** User ID to delete. */
  public int userId;
}
