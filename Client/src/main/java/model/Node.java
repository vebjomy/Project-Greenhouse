package model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import java.util.*;

/**
 * Client-side Node representation: stores only data received from the server.
 * Does NOT contain simulation logic - that's handled by the server.
 *
 * This class acts as a pure data model that receives updates via sensor_update
 * messages and provides observable properties for UI binding.
 *
 * @author Green House Control Team
 * @version 2.0
 * @since 1.0
 */
public class Node {
  private final String id;
  private final String name;
  private final String location;
  private final String ipAddress;

  // Lists of available component types (from topology)
  private final List<String> sensorTypes;
  private final List<String> actuatorTypes;

  // Live DATA from sensor_update (current values)
  private final ObservableMap<String, Double> sensorData =
          FXCollections.observableHashMap();
  private final ObservableMap<String, String> actuatorStates =
          FXCollections.observableHashMap();

  // Timestamp of last update
  private final LongProperty lastUpdate = new SimpleLongProperty(0);

  /**
   * Constructs a new Node with the specified parameters.
   *
   * @param id Unique node identifier (e.g., "node-1")
   * @param name Human-readable node name (e.g., "Greenhouse A-1")
   * @param location Physical location description
   * @param ip IP address of the node
   * @param sensors List of sensor types this node supports
   * @param actuators List of actuator types this node supports
   */
  public Node(String id, String name, String location, String ip,
              List<String> sensors, List<String> actuators) {
    this.id = id;
    this.name = name;
    this.location = location;
    this.ipAddress = ip;
    this.sensorTypes = new ArrayList<>(sensors);
    this.actuatorTypes = new ArrayList<>(actuators);

    // Initialize with default values
    sensors.forEach(s -> sensorData.put(s, 0.0));
    actuators.forEach(a -> actuatorStates.put(a, "UNKNOWN"));
  }

  /**
   * Updates node data from a sensor_update message received from the server.
   * This method distinguishes between sensor readings (numeric values) and
   * actuator states (string values like "ON"/"OFF").
   *
   * @param data Map containing both sensor readings and actuator states
   * @param timestamp Unix timestamp (milliseconds) of the update
   */
  public void updateFromServer(Map<String, Object> data, long timestamp) {
    data.forEach((key, value) -> {
      if (value instanceof Number) {
        // This is a sensor reading
        sensorData.put(key, ((Number) value).doubleValue());
      } else {
        // This is an actuator state
        actuatorStates.put(key, String.valueOf(value));
      }
    });
    lastUpdate.set(timestamp);
  }

  // === Getters for basic properties ===

  public String getId() { return id; }
  public String getName() { return name; }
  public String getLocation() { return location; }
  public String getIpAddress() { return ipAddress; }

  /**
   * Returns an immutable list of sensor types supported by this node.
   * @return List of sensor type names (e.g., ["temperature", "humidity"])
   */
  public List<String> getSensorTypes() { return Collections.unmodifiableList(sensorTypes); }

  /**
   * Returns an immutable list of actuator types supported by this node.
   * @return List of actuator type names (e.g., ["fan", "water_pump"])
   */
  public List<String> getActuatorTypes() { return Collections.unmodifiableList(actuatorTypes); }

  /**
   * Returns the observable map of sensor data for UI binding.
   * Keys are sensor types (e.g., "temperature"), values are current readings.
   * @return Observable map of sensor data
   */
  public ObservableMap<String, Double> getSensorData() { return sensorData; }

  /**
   * Returns the observable map of actuator states for UI binding.
   * Keys are actuator types (e.g., "fan"), values are states (e.g., "ON", "OFF").
   * @return Observable map of actuator states
   */
  public ObservableMap<String, String> getActuatorStates() { return actuatorStates; }

  /**
   * Returns the property tracking the timestamp of the last update.
   * Useful for displaying "last updated" information in the UI.
   * @return LongProperty containing the last update timestamp
   */
  public LongProperty lastUpdateProperty() { return lastUpdate; }

  // === Convenience methods for common sensors ===

  /**
   * Gets the current temperature reading.
   * @return Temperature in Celsius, or null if not available
   */
  public Double getTemperature() { return sensorData.get("temperature"); }

  /**
   * Gets the current humidity reading.
   * @return Humidity percentage (0-100), or null if not available
   */
  public Double getHumidity() { return sensorData.get("humidity"); }

  /**
   * Gets the current light level reading.
   * @return Light level in lux, or null if not available
   */
  public Double getLight() { return sensorData.get("light"); }

  /**
   * Gets the current pH reading.
   * @return pH level (0-14), or null if not available
   */
  public Double getPh() { return sensorData.get("ph"); }

  // === Convenience methods for common actuators ===

  /**
   * Gets the current fan state.
   * @return "ON", "OFF", or "UNKNOWN"
   */
  public String getFanState() { return actuatorStates.getOrDefault("fan", "UNKNOWN"); }

  /**
   * Gets the current water pump state.
   * @return "ON", "OFF", or "UNKNOWN"
   */
  public String getPumpState() { return actuatorStates.getOrDefault("water_pump", "UNKNOWN"); }

  /**
   * Gets the current CO2 generator state.
   * @return "ON", "OFF", or "UNKNOWN"
   */
  public String getCo2State() { return actuatorStates.getOrDefault("co2", "UNKNOWN"); }

  /**
   * Gets the current window state.
   * @return "CLOSED", "HALF", "OPEN", or "UNKNOWN"
   */
  public String getWindowState() { return actuatorStates.getOrDefault("window", "UNKNOWN"); }

  @Override
  public String toString() {
    return String.format("Node{id='%s', name='%s', location='%s', ip='%s', sensors=%d, actuators=%d}",
            id, name, location, ipAddress, sensorTypes.size(), actuatorTypes.size());
  }
}