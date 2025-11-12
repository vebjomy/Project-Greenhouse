package ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Visual component for displaying humidity sensor readings.
 *
 * Provides a circular gauge visualization with color-coded indicators:
 * - Blue: Low humidity (< 30%)
 * - Green: Normal humidity (30-70%)
 * - Red: High humidity (> 70%)
 *
 * @author Green House Control Team
 * @version 1.0
 */
public class HumiditySensorView {

  // Humidity thresholds for color coding (percentage)
  private static final double HUMIDITY_LOW_THRESHOLD = 30.0;
  private static final double HUMIDITY_HIGH_THRESHOLD = 70.0;

  /**
   * Creates a visual representation of a humidity sensor.
   *
   * The visualization includes:
   * - A circular arc gauge showing humidity percentage
   * - Color-coded display (blue=low, green=normal, red=high)
   * - Numeric value with percentage label
   *
   * @param name Display name for the sensor (e.g., "Humidity")
   * @param value Current humidity value (0-100%)
   * @param unit Unit string to display (e.g., "%")
   * @return A Pane containing the complete sensor visualization
   */
  public static Pane create(String name, double value, String unit) {
    // Clamp value to valid range
    double clampedValue = Math.max(0, Math.min(100, value));

    // --- Background circle ---
    Circle backgroundCircle = new Circle(40);
    backgroundCircle.setFill(Color.web("#f0f0f0"));

    // --- Humidity arc (gauge) ---
    double arcLength = 3.6 * clampedValue; // 360° * (value/100)
    double startAngle = 90 - arcLength;    // Start from bottom

    Arc humidityArc = new Arc(0, 0, 36, 36, startAngle, arcLength);
    humidityArc.setType(ArcType.OPEN);
    humidityArc.setStrokeWidth(8);
    humidityArc.setFill(null);
    humidityArc.setStrokeLineCap(StrokeLineCap.ROUND);

    // --- Color coding based on humidity ---
    Color statusColor;
    if (clampedValue < HUMIDITY_LOW_THRESHOLD) {
      statusColor = Color.web("#4FC3F7"); // Light Blue - Low humidity
    } else if (clampedValue > HUMIDITY_HIGH_THRESHOLD) {
      statusColor = Color.web("#E57373"); // Light Red - High humidity
    } else {
      statusColor = Color.web("#81C784"); // Light Green - Normal
    }
    humidityArc.setStroke(statusColor);

    // --- Labels ---
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
    nameLabel.setAlignment(Pos.CENTER);

    Label valueLabel = new Label(String.format("%.1f%s", clampedValue, unit));
    valueLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));
    valueLabel.setTextFill(statusColor);
    valueLabel.setAlignment(Pos.CENTER);

    // --- Layout assembly ---
    StackPane circlePane = new StackPane(backgroundCircle, humidityArc, valueLabel);
    circlePane.setPrefSize(80, 80);
    circlePane.setPadding(new Insets(10));

    VBox layout = new VBox(5, nameLabel, circlePane);
    layout.setPadding(new Insets(10));
    layout.setAlignment(Pos.CENTER);

    return layout;
  }

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private HumiditySensorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}
