package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.List;

public class Node {
  private final ObservableList<Sensor> sensors;
  private final ObservableList<Actuator> actuators;
  private final String name;
  private final String location; // <<< NEW FIELD

  // Main constructor updated to include location
  public Node(String name, String location, Collection<Sensor> initialSensors, Collection<Actuator> initialActuators) {
    this.name = name;
    this.location = location; // <<< ASSIGN LOCATION

    this.sensors = FXCollections.observableArrayList();
    if (initialSensors != null) {
      this.sensors.addAll(initialSensors);
    }

    this.actuators = FXCollections.observableArrayList();
    if (initialActuators != null) {
      this.actuators.addAll(initialActuators);
    }
  }

  // Simplified constructor for convenience
  public Node(String name, String location) {
    this(name, location, null, null);
  }

  public String getName() {
    return name;
  }

  // <<< NEW GETTER
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
}