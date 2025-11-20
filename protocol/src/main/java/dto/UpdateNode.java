package dto;

import java.util.Map;

/**
 * Represents a request to update a node's properties. Contains the node ID and a patch map with
 * updated fields.
 */
public class UpdateNode {
  /** Message type identifier. */
  public String type = "update_node";

  /** Request identifier. */
  public String id;

  /** Target node identifier. */
  public String nodeId;

  /** Patch map containing fields to update. */
  public Map<String, Object> patch;
}
