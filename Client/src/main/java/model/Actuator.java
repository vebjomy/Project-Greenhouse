package model;

import javafx.scene.layout.Pane;

public interface Actuator {
  /**
   * @return The name of the actuator (e.g., "Fan", "Heater").
   */
  String getActuatorName();

  /**
   * @return A JavaFX Pane representing the actuator's visual controls and status.
   */
  Pane getVisualRepresentation();
}