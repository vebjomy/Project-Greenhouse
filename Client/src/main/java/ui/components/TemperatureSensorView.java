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
 * Visual component for displaying temperature sensor readings.
 * Does NOT contain data - only rendering logic.
 *
 * This is a stateless utility class that creates JavaFX UI components
 * based on provided values. The actual data is stored in the Node model.
 *
 * @author Green House Control Team
 * @version 1.0
 */
public class TemperatureSensorView {

  // Temperature ranges for color coding (Celsius)
  private static final double TEMP_COLD_THRESHOLD = 5.0;
  private static final double TEMP_HOT_THRESHOLD = 28.0;
  private static final double TEMP_MIN = -10.0;
  private static final double TEMP_MAX = 40.0;

  /**
   * Creates a visual representation of a temperature sensor.
   *
   * The visualization includes:
   * - A circular arc gauge showing the temperature
   * - Color-coded display (blue=cold, green=normal, red=hot)
   * - Numeric value with unit label
   *
   * @param name Display name for the sensor (e.g., "Temperature")
   * @param value Current temperature value in Celsius
   * @param unit Unit string to display (e.g., "°C")
   * @return A Pane containing the complete sensor visualization
   */
  public static Pane create(String name, double value, String unit) {
    // --- Background circle ---
    Circle backgroundCircle = new Circle(40);
    backgroundCircle.setFill(Color.web("#f0f0f0"));

    // --- Temperature arc (gauge) ---
    // Normalize value to 0-100 range for arc calculation
    double normalizedValue = Math.max(TEMP_MIN, Math.min(TEMP_MAX, value));
    double percentage = ((normalizedValue - TEMP_MIN) / (TEMP_MAX - TEMP_MIN)) * 100;
    double arcLength = 3.6 * percentage; // 360° * (percentage/100)
    double startAngle = 90 - arcLength;  // Start from bottom

    Arc tempArc = new Arc(0, 0, 36, 36, startAngle, arcLength);
    tempArc.setType(ArcType.OPEN);
    tempArc.setStrokeWidth(8);
    tempArc.setFill(null);
    tempArc.setStrokeLineCap(StrokeLineCap.ROUND);

    // --- Color coding based on temperature ---
    Color statusColor;
    if (value < TEMP_COLD_THRESHOLD) {
      statusColor = Color.web("#4FC3F7"); // Light Blue - Cold
    } else if (value > TEMP_HOT_THRESHOLD) {
      statusColor = Color.web("#E57373"); // Light Red - Hot
    } else {
      statusColor = Color.web("#81C784"); // Light Green - Normal
    }
    tempArc.setStroke(statusColor);

    // --- Labels ---
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
    nameLabel.setAlignment(Pos.CENTER);

    Label valueLabel = new Label(String.format("%.1f %s", value, unit));
    valueLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));
    valueLabel.setTextFill(statusColor);
    valueLabel.setAlignment(Pos.CENTER);

    // --- Layout assembly ---
    StackPane circlePane = new StackPane(backgroundCircle, tempArc, valueLabel);
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
  private TemperatureSensorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}
