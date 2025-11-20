package dto;

/**
 * Represents a change event for a node in the topology.
 * Indicates whether a node was added, updated, or removed.
 */
public class NodeChange {
  /** Message type identifier. */
  public String type = "node_change";
  /** Operation type: "added", "updated", or "removed". */
  public String op;
  /** The node object for added or updated operations. */
  public Topology.Node node;
  /** The node ID for removed operations. */
  public String nodeId;
}