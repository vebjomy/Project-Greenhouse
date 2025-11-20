package dto;

/**
 * Represents a ping message sent by the client to the server. Contains the message type and request
 * ID.
 */
public class Ping {
  /** Message type identifier. */
  public String type = "ping";

  /** Request identifier. */
  public String id;
}
