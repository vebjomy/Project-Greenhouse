package dto;

import java.util.Map;

/**
 * Represents an update message containing sensor values from a node. Contains the node ID,
 * timestamp, and a map of sensor data.
 */
public class SensorUpdate {
  /** Message type identifier. */
  public String type = "sensor_update";

  /** Node ID the sensor data belongs to. */
  public String nodeId;

  /** Timestamp of the sensor data (epoch milliseconds). */
  public long timestamp;

  /** Map of sensor names to their values. */
  public Map<String, Object> data;
}
