package dto;

/**
 * Represents a request to retrieve the latest sensor/actuator values from a node.
 * Contains the request ID and the target node ID.
 */
public class GetLastValues {
  /** Message type identifier. */
  public String type = "get_last_values";
  /** Request identifier. */
  public String id;
  /** Target node ID to fetch values from. */
  public String nodeId;
}