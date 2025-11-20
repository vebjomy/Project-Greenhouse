package dto;

/**
 * Represents a request to delete a node from the topology.
 * Contains the request ID and the node ID to be deleted.
 */
public class DeleteNode {
  /** Message type identifier. */
  public String type = "delete_node";
  /** Request identifier. */
  public String id;
  /** Node ID to delete. */
  public String nodeId;
}
