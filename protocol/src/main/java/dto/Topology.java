package dto;

import java.util.List;
public class Topology {
  public String type = "topology";
  public List<Node> nodes;
  public static class Node {
    public String id;
    public String name;
    public String location;
    public String ip;
    public List<String> sensors;
    public List<String> actuators;
  }
}
