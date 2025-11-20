package dto;

/**
 * Represents a request to set the sampling interval for a node.
 * Contains the request ID, target node ID, and the interval in milliseconds.
 */
public class SetSampling {
  /** Message type identifier. */
  public String type = "set_sampling";
  /** Request identifier. */
  public String id;
  /** Target node ID to set the sampling interval for. */
  public String nodeId;
  /** Sampling interval in milliseconds. */
  public int intervalMs;
}
