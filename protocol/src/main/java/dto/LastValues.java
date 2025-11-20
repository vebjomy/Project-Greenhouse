package dto;

import java.util.Map;

/**
 * Represents a response containing the latest sensor/actuator values from a node.
 * Contains the request ID, node ID, data map, and timestamp.
 */
public class LastValues {
  /** Message type identifier. */
  public String type = "last_values";
  /** Request identifier. */
  public String id;
  /** Node ID the values belong to. */
  public String nodeId;
  /** Map of sensor/actuator names to their latest values. */
  public Map<String,Object> data;
  /** Timestamp of the values (epoch milliseconds). */
  public long timestamp;
}
