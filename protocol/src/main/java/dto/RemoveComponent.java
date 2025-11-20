package dto;

import java.util.Map;

/**
 * Represents a request to remove a component (sensor or actuator) from a node.
 * Contains the request ID, target node ID, and component details.
 */
public class RemoveComponent {
  /** Message type identifier. */
  public String type = "remove_component";
  /** Request identifier. */
  public String id;
  /** Target node ID to remove the component from. */
  public String nodeId;
  /** Component details (e.g., kind and name). */
  public Map<String,Object> component; // {kind:"sensor|actuator", name:"ph|fan|..."}
}
