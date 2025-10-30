package dto;

import java.util.Map;
public class UpdateNode {
  public String type = "update_node";
  public String id;
  public String nodeId;
  public Map<String,Object> patch;
}
