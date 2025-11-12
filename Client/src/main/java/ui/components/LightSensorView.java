package ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Visual component for displaying light sensor readings.
 *
 * Provides a simple icon-based visualization with color-coded indicators:
 * - Blue: Low light (< 200 lux)
 * - Green: Normal light (200-700 lux)
 * - Yellow: High light (> 700 lux)
 *
 * @author Green House Control Team
 * @version 1.0
 */
public class LightSensorView {

  // Light level thresholds for color coding (lux)
  private static final double LIGHT_LOW_THRESHOLD = 200.0;
  private static final double LIGHT_HIGH_THRESHOLD = 700.0;

  // SVG path for light bulb icon (Material Design)
  private static final String LIGHT_ICON_PATH =
          "M12,6A6,6 0 0,1 18,12C18,14.22 16.79,16.16 15,17.2V19A1,1 0 0,1 14,20H10A1,1 0 0,1 " +
                  "9,19V17.2C7.21,16.16 6,14.22 6,12A6,6 0 0,1 12,6M14,21V22A1,1 0 0,1 13,23H11A1,1 0 " +
                  "0,1 10,22V21H14M20,11H23V13H20V11M1,11H4V13H1V11M13,1V4H11V1H13M4.92,3.5L7.05,5.64L5.63," +
                  "7.05L3.5,4.93L4.92,3.5M16.95,5.63L19.07,3.5L20.5,4.93L18.37,7.05L16.95,5.63Z";

  /**
   * Creates a visual representation of a light sensor.
   *
   * The visualization includes:
   * - A light bulb icon
   * - Color-coded display based on light intensity
   * - Numeric value with lux unit
   *
   * @param name Display name for the sensor (e.g., "Light")
   * @param value Current light level in lux
   * @param unit Unit string to display (e.g., "lx")
   * @return A Pane containing the complete sensor visualization
   */
  public static Pane create(String name, double value, String unit) {
    // Clamp value to reasonable range
    double clampedValue = Math.max(0, Math.min(200_000, value));

    // --- Icon ---
    SVGPath lightIcon = new SVGPath();
    lightIcon.setContent(LIGHT_ICON_PATH);
    lightIcon.setScaleX(1.2);
    lightIcon.setScaleY(1.2);

    // --- Color coding based on light level ---
    Color statusColor;
    if (clampedValue < LIGHT_LOW_THRESHOLD) {
      statusColor = Color.web("#4FC3F7"); // Light Blue - Low light
    } else if (clampedValue > LIGHT_HIGH_THRESHOLD) {
      statusColor = Color.web("#FFD54F"); // Yellow - High light
    } else {
      statusColor = Color.web("#81C784"); // Light Green - Normal
    }
    lightIcon.setFill(statusColor);

    // --- Labels ---
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

    Label valueLabel = new Label(String.format("%.0f %s", clampedValue, unit));
    valueLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));
    valueLabel.setTextFill(statusColor);

    // --- Layout assembly ---
    VBox titleBox = new VBox(-2, nameLabel, valueLabel);
    titleBox.setAlignment(Pos.CENTER_LEFT);

    HBox topPane = new HBox(10, lightIcon, titleBox);
    topPane.setAlignment(Pos.CENTER_LEFT);
    topPane.setPadding(new Insets(10));

    return topPane;
  }

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private LightSensorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}
