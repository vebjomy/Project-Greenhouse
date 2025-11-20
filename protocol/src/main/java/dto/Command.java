package dto;

import java.util.Map;

/**
 * Represents a command message sent to a node or component. Contains the request ID, target node
 * ID, target component, action, and parameters.
 */
public class Command {
  /** Message type identifier. */
  public String type = "command";

  /** Request identifier. */
  public String id;

  /** Target node ID to send the command to. */
  public String nodeId;

  /** Target component or subsystem. */
  public String target;

  /** Action to perform (e.g., "set", "toggle"). */
  public String action;

  /** Command parameters as a key-value map. */
  public Map<String, Object> params;
}
