package dto;

/**
 * Represents a request to create a new node in the topology.
 * Contains the request ID and the node details.
 */
public class CreateNode {
  /** Message type identifier. */
  public String type = "create_node";
  /** Request identifier. */
  public String id;
  /** Node details to be created. */
  public Topology.Node node;
}
