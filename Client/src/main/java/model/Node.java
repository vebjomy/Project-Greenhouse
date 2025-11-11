package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a Node that contains Sensors and Actuators.
 * Each Node has a name, location, and stable IP address.
 * @version 1.2
 */
public class Node {
  private static final AtomicInteger ipCounter = new AtomicInteger(50); // Start from 192.168.1.50

  private final ObservableList<Sensor> sensors;
  private final ObservableList<Actuator> actuators;
  private final String name;
  private final String location;
  private final String ipAddress; // Now stored as final field

  /**
   * Main constructor with location
   */
  public Node(String name, String location, Collection<Sensor> initialSensors, Collection<Actuator> initialActuators) {
    this.name = name;
    this.location = location;
    this.ipAddress = generateStableIp(); // Generate once and store

    this.sensors = FXCollections.observableArrayList();
    if (initialSensors != null) {
      this.sensors.addAll(initialSensors);
    }

    this.actuators = FXCollections.observableArrayList();
    if (initialActuators != null) {
      this.actuators.addAll(initialActuators);
    }
  }

  /**
   * Simplified constructor for convenience
   */
  public Node(String name, String location) {
    this(name, location, null, null);
  }

  /**
   * Generate a stable IP address that increments for each new node
   */
  private static String generateStableIp() {
    int lastOctet = ipCounter.getAndIncrement();
    // Wrap around if we exceed 254
    if (lastOctet > 254) {
      lastOctet = 50;
      ipCounter.set(51);
    }
    return "192.168.1." + lastOctet;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return name.replaceAll("\\s+", "_").toLowerCase();
  }

  /**
   * Returns the stable IP address assigned at creation
   */
  public String getIpAddress() {
    return ipAddress;
  }

  public String getLocation() {
    return location;
  }

  public void addActuator(Actuator actuator) {
    this.actuators.add(actuator);
  }

  public void addSensor(Sensor sensor) {
    this.sensors.add(sensor);
  }

  public ObservableList<Sensor> getSensors() {
    return sensors;
  }

  public ObservableList<Actuator> getActuators() {
    return actuators;
  }

  public void removeActuator(Actuator actuator) {
    this.actuators.remove(actuator);
  }

  public void removeSensor(Sensor sensor) {
    this.sensors.remove(sensor);
  }

  @Override
  public String toString() {
    return String.format("Node{name='%s', location='%s', ip='%s', sensors=%d, actuators=%d}",
            name, location, ipAddress, sensors.size(), actuators.size());
  }

  /**
   * Reset the IP counter (useful for testing)
   */
  public static void resetIpCounter() {
    ipCounter.set(50);
  }
}