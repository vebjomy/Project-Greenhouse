package dto;

import java.util.Map;

/**
 * Represents a request to add a component (sensor or actuator) to a node.
 * Contains the request ID, target node ID, and component details.
 */
public class AddComponent {
  /** Message type identifier. */
  public String type = "add_component";
  /** Request identifier. */
  public String id;
  /** Target node ID to add the component to. */
  public String nodeId;
  /** Component details (e.g., kind and name). */
  public Map<String,Object> component; // {kind:"sensor|actuator", name:"ph|fan|..."}
}
