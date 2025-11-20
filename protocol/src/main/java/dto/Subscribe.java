package dto;

import java.util.List;

/**
 * Represents a request to subscribe to specific nodes and events.
 * Contains the request ID, list of node IDs, and event types to subscribe to.
 */
public class Subscribe {
  /** Message type identifier. */
  public String type = "subscribe";
  /** Request identifier. */
  public String id;
  /** List of node IDs to subscribe to. */
  public List<String> nodes;
  /** List of event types to subscribe to. */
  public List<String> events;
}
