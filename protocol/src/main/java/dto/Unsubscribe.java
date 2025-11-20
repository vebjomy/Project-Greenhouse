package dto;

import java.util.List;

/**
 * Represents a request to unsubscribe from specific nodes and events.
 * Contains the request ID, list of node IDs, and event types to unsubscribe from.
 */
public class Unsubscribe {
  /** Message type identifier. */
  public String type = "unsubscribe";
  /** Request identifier. */
  public String id;
  /** List of node IDs to unsubscribe from. */
  public List<String> nodes;
  /** List of event types to unsubscribe from. */
  public List<String> events;
}
