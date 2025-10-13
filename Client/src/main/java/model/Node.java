package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;

public class Node {
  private final ObservableList<Sensor> sensors;
  private final ObservableList<Actuator> actuators;
  private final String name;

  // Constructor 1: Original constructor for an empty node
  public Node(String name) {
    this(name, null, null);
  }

  // Constructor 2: Constructor for a node with only initial sensors
  public Node(String name, Collection<? extends Sensor> initialSensors) {
    this(name, initialSensors, null);
  }

  // Constructor 3: The main constructor handling all initial collections
  public Node(String name,
              Collection<? extends Sensor> initialSensors,
              Collection<? extends Actuator> initialActuators) {
    this.name = name;

    // Initialize ObservableLists
    this.sensors = FXCollections.observableArrayList();
    this.actuators = FXCollections.observableArrayList();

    // Add initial sensors if the collection is provided
    if (initialSensors != null) {
      this.sensors.addAll(initialSensors);
    }

    // Add initial actuators if the collection is provided
    if (initialActuators != null) {
      this.actuators.addAll(initialActuators);
    }
  }

  public String getName() {
    return name;
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