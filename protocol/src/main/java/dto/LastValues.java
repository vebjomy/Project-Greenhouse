package dto;

import java.util.Map;
public class LastValues {
  public String type = "last_values";
  public String id;
  public String nodeId;
  public Map<String,Object> data;
  public long timestamp;
}
