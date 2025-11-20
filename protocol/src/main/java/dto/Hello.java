package dto;

import java.util.List;

/**
 * Represents a hello message sent by the client to the server.
 * Contains the request ID, client identifier, user, and supported capabilities.
 */
public class Hello {
  /** Message type identifier. */
  public String type = "hello";
  /** Request identifier. */
  public String id;
  /** Client identifier. */
  public String clientId;
  /** Username of the client. */
  public String user;
  /** List of supported capabilities. */
  public List<String> capabilities;
}
