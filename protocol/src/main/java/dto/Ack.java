package dto;

/**
 * Represents a generic acknowledgement message from the server.
 * Contains status and optional node ID for certain replies.
 */
public class Ack {
  /** Message type identifier. */
  public String type = "ack";
  /** Request identifier. */
  public String id;
  /** Status of the acknowledgement. */
  public String status;
  /** Optional node ID (e.g., for create_node reply). */
  public String nodeId;
}