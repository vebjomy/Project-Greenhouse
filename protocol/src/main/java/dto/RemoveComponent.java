package dto;

import java.util.Map;
public class RemoveComponent {
  public String type = "remove_component";
  public String id;
  public String nodeId;
  public Map<String,Object> component;
}
