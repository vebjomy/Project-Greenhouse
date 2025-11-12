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
 * Visual component for displaying pH sensor readings.
 *
 * Provides a simple icon-based visualization with color-coded indicators:
 * - Blue: Acidic (< 6.5)
 * - Green: Neutral/Optimal (6.5-8.5)
 * - Orange: Alkaline (> 8.5)
 *
 * @author Green House Control Team
 * @version 1.0
 */
public class PhSensorView {

  // pH thresholds for color coding
  private static final double PH_ACIDIC_THRESHOLD = 6.5;
  private static final double PH_ALKALINE_THRESHOLD = 8.5;

  // SVG path for pH/water drop icon (Material Design)
  private static final String PH_ICON_PATH =
          "M12,2C15.31,2 18,4.66 18,7.95C18,12.41 12,19 12,19C12,19 6,12.41 6,7.95C6,4.66 8.69," +
                  "2 12,2M12,4A3.5,3.5 0 0,0 8.5,7.5A3.5,3.5 0 0,0 12,11A3.5,3.5 0 0,0 15.5,7.5A3.5,3.5 " +
                  "0 0,0 12,4M12,5.5A2,2 0 0,1 14,7.5A2,2 0 0,1 12,9.5A2,2 0 0,1 10,7.5A2,2 0 0,1 12,5.5Z";

  /**
   * Creates a visual representation of a pH sensor.
   *
   * The visualization includes:
   * - A water drop icon
   * - Color-coded display based on pH level
   * - Numeric value with pH unit
   *
   * @param name Display name for the sensor (e.g., "pH Level")
   * @param value Current pH value (0-14)
   * @param unit Unit string to display (e.g., "pH")
   * @return A Pane containing the complete sensor visualization
   */
  public static Pane create(String name, double value, String unit) {
    // Clamp value to valid pH range
    double clampedValue = Math.max(0, Math.min(14, value));

    // --- Icon ---
    SVGPath phIcon = new SVGPath();
    phIcon.setContent(PH_ICON_PATH);
    phIcon.setScaleX(1.2);
    phIcon.setScaleY(1.2);

    // --- Color coding based on pH level ---
    Color statusColor;
    if (clampedValue < PH_ACIDIC_THRESHOLD) {
      statusColor = Color.web("#4FC3F7"); // Light Blue - Acidic
    } else if (clampedValue > PH_ALKALINE_THRESHOLD) {
      statusColor = Color.web("#FF7043"); // Orange - Alkaline
    } else {
      statusColor = Color.web("#81C784"); // Light Green - Optimal
    }
    phIcon.setFill(statusColor);

    // --- Labels ---
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

    Label valueLabel = new Label(String.format("%.2f %s", clampedValue, unit));
    valueLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));
    valueLabel.setTextFill(statusColor);

    // --- Layout assembly ---
    VBox titleBox = new VBox(-2, nameLabel, valueLabel);
    titleBox.setAlignment(Pos.CENTER_LEFT);

    HBox topPane = new HBox(10, phIcon, titleBox);
    topPane.setAlignment(Pos.CENTER_LEFT);
    topPane.setPadding(new Insets(10));

    return topPane;
  }

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private PhSensorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}
