package dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Topology {
  public String type = "topology";
  public String id;
  public String status;
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