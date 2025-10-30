package dto;

public class NodeChange {
  public String type = "node_change";
  public String op;              // "added" | "updated" | "removed"
  public Topology.Node node;     // for added/updated
  public String nodeId;          // for removed
}
