package dto;

import java.util.Map;
public class SensorUpdate {
  public String type = "sensor_update";
  public String nodeId;
  public long timestamp;
  public Map<String,Object> data;
}
