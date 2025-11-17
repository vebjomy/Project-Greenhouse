package dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * DTO for topology messages containing all network nodes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Topology {
  public String type = "topology";
  public String id;
  public String status;
  public List<Node> nodes;

  /**
   * Represents a single node in the greenhouse network.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Node {
    public String id;
    public String name;
    public String location;
    public String ip;
    public List<String> sensors;
    public List<String> actuators;

    @Override
    public String toString() {
      return String.format("Node{id='%s', name='%s', location='%s', ip='%s', sensors=%d, actuators=%d}",
              id, name, location, ip,
              sensors != null ? sensors.size() : 0,
              actuators != null ? actuators.size() : 0);
    }
  }

  @Override
  public String toString() {
    return String.format("Topology{type='%s', nodes=%d}",
            type, nodes != null ? nodes.size() : 0);
  }
}