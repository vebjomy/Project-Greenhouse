package ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Visual component for displaying temperature sensor readings with icon.
 * Uses the temp_sensor.png icon from resources.
 *
 * @author Green House Control Team
 * @version 2.0
 */
public class TemperatureSensorView {

  // Temperature ranges for color coding (Celsius)
  private static final double TEMP_COLD_THRESHOLD = 5.0;
  private static final double TEMP_HOT_THRESHOLD = 28.0;

  /**
   * Creates a visual representation of a temperature sensor with icon.
   *
   * @param name Display name for the sensor (e.g., "Temperature")
   * @param value Current temperature value in Celsius
   * @param unit Unit string to display (e.g., "°C")
   * @return A Pane containing the complete sensor visualization
   */
  public static Pane create(String name, double value, String unit) {
    // === Icon ===
    ImageView icon = null;
    try {
      Image image = new Image(
              TemperatureSensorView.class.getResourceAsStream("/icons/temp_sensor.png")
      );
      icon = new ImageView(image);
      icon.setFitWidth(40);
      icon.setFitHeight(40);
      icon.setPreserveRatio(true);
    } catch (Exception e) {
      System.err.println("⚠️ Could not load temperature icon: " + e.getMessage());
    }

    // === Color coding based on temperature ===
    Color statusColor;
    String statusText;
    if (value < TEMP_COLD_THRESHOLD) {
      statusColor = Color.web("#4FC3F7"); // Light Blue - Cold
      statusText = "COLD";
    } else if (value > TEMP_HOT_THRESHOLD) {
      statusColor = Color.web("#E57373"); // Light Red - Hot
      statusText = "HOT";
    } else {
      statusColor = Color.web("#81C784"); // Light Green - Normal
      statusText = "NORMAL";
    }

    // === Labels ===
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
    nameLabel.setTextFill(Color.web("#202124"));

    Label valueLabel = new Label(String.format("%.1f%s", value, unit));
    valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
    valueLabel.setTextFill(statusColor);

    Label statusLabel = new Label(statusText);
    statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));
    statusLabel.setTextFill(statusColor);

    // === Layout ===
    VBox textBox = new VBox(0, nameLabel, valueLabel, statusLabel);
    textBox.setAlignment(Pos.CENTER_LEFT);

    HBox layout = new HBox(10);
    if (icon != null) {
      layout.getChildren().add(icon);
    }
    layout.getChildren().add(textBox);
    layout.setAlignment(Pos.CENTER_LEFT);
    layout.setPadding(new Insets(8, 10, 8, 10));
    layout.setStyle(
            "-fx-background-color: #f8f9fa;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: " + toHexString(statusColor) + ";" +
                    "-fx-border-width: 2;" +
                    "-fx-border-radius: 8;"
    );

    return layout;
  }

  /**
   * Converts a Color to hex string format.
   */
  private static String toHexString(Color color) {
    return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
  }

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private TemperatureSensorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}
