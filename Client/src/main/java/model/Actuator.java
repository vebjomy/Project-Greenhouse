package model;

import javafx.scene.layout.Pane;

/**
 * Represents an actuator in the smart home system.
 * An actuator is a device that can perform actions based on commands,
 * such as turning on a fan or adjusting a thermostat.
 */

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