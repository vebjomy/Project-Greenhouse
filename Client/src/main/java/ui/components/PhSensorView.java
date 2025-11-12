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
 * Visual component for displaying pH sensor readings with icon.
 * Uses the Ph.png icon from resources.
 *
 * @author Green House Control Team
 * @version 2.0
 */
public class PhSensorView {

  // pH thresholds for color coding
  private static final double PH_ACIDIC_THRESHOLD = 6.0;
  private static final double PH_ALKALINE_THRESHOLD = 7.5;

  /**
   * Creates a visual representation of a pH sensor with icon.
   *
   * @param name Display name for the sensor (e.g., "pH Level")
   * @param value Current pH value (0-14)
   * @param unit Unit string to display (e.g., "pH")
   * @return A Pane containing the complete sensor visualization
   */
  public static Pane create(String name, double value, String unit) {
    // Clamp value to valid range
    double clampedValue = Math.max(0, Math.min(14, value));

    // === Icon ===
    ImageView icon = null;
    try {
      Image image = new Image(
              PhSensorView.class.getResourceAsStream("/icons/Ph.png")
      );
      icon = new ImageView(image);
      icon.setFitWidth(40);
      icon.setFitHeight(40);
      icon.setPreserveRatio(true);
    } catch (Exception e) {
      System.err.println("⚠️ Could not load pH icon: " + e.getMessage());
    }

    // === Color coding based on pH ===
    Color statusColor;
    String statusText;
    if (clampedValue < PH_ACIDIC_THRESHOLD) {
      statusColor = Color.web("#FF7043"); // Deep Orange - Acidic
      statusText = "ACIDIC";
    } else if (clampedValue > PH_ALKALINE_THRESHOLD) {
      statusColor = Color.web("#7E57C2"); // Purple - Alkaline
      statusText = "ALKALINE";
    } else {
      statusColor = Color.web("#81C784"); // Light Green - Neutral
      statusText = "NEUTRAL";
    }

    // === Labels ===
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
    nameLabel.setTextFill(Color.web("#202124"));

    Label valueLabel = new Label(String.format("%.1f %s", clampedValue, unit));
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
  private PhSensorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}
