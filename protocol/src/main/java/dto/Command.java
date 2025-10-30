package dto;

import java.util.Map;
public class Command {
  public String type = "command";
  public String id;
  public String nodeId;
  public String target;
  public String action;
  public Map<String,Object> params;
}
