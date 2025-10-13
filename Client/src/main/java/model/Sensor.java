package model;

import javafx.scene.layout.Pane;

/**
 * Interface for all sensor types.
 */
public interface Sensor {
  String getSensorName();
  String getReading();
  double getNumericValue();
  Pane getVisualRepresentation();
}
