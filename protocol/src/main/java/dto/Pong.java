package dto;

/**
 * Represents a pong message sent by the server in response to a ping. Contains the message type and
 * request ID.
 */
public class Pong {
  /** Message type identifier. */
  public String type = "pong";

  /** Request identifier. */
  public String id;
}
