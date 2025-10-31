package model;

import javafx.scene.layout.Pane;

/**
 * Interface for all sensor types and their behaviors.
 * this interface defines the methods that all sensor classes must implement.
 * @author Your Name
 * @version 1.0
 */
public interface Sensor {
  String getSensorName();
  String getReading();
  public abstract double getCurrentValue();
  double getNumericValue();
  Pane getVisualRepresentation();

  String getName();
}
