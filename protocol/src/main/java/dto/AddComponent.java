package dto;

import java.util.Map;
public class AddComponent {
  public String type = "add_component";
  public String id;
  public String nodeId;
  public Map<String,Object> component; // {kind:"sensor|actuator", name:"ph|fan|..."}
}
