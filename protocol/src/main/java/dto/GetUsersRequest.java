package dto;

/** Represents a request to retrieve the list of users. Contains the request ID for tracking. */
public class GetUsersRequest {
  /** Message type identifier. */
  public String type = "get_users";

  /** Request identifier. */
  public String id;
}
