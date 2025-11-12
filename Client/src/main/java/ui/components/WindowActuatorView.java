package ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.Consumer;

/**
 * Visual component for displaying and controlling a window actuator with color changes.
 * Uses the window.png icon from resources.
 *
 * @author Green House Control Team
 * @version 2.0
 */
public class WindowActuatorView {

  /**
   * Creates a visual representation of a window actuator with control buttons and color effects.
   *
   * @param name Display name for the actuator (e.g., "Window")
   * @param state Current state from server ("CLOSED", "HALF", "OPEN", or "UNKNOWN")
   * @param onLevelChange Callback function to handle button clicks.
   *                      Called with "CLOSED", "HALF", or "OPEN" string.
   * @return A Pane containing the complete actuator visualization and controls
   */
  public static Pane create(String name, String state, Consumer<String> onLevelChange) {
    // === Icon with color adjustment ===
    ImageView windowIcon = null;
    ColorAdjust colorAdjust = new ColorAdjust();

    try {
      Image image = new Image(
              WindowActuatorView.class.getResourceAsStream("/icons/window.png")
      );
      windowIcon = new ImageView(image);
      windowIcon.setFitWidth(50);
      windowIcon.setFitHeight(50);
      windowIcon.setPreserveRatio(true);
      windowIcon.setEffect(colorAdjust);
    } catch (Exception e) {
      System.err.println("⚠️ Could not load window icon: " + e.getMessage());
    }

    // === Color coding based on state ===
    Color statusColor;
    String statusText;

    switch (state != null ? state.toUpperCase() : "UNKNOWN") {
      case "CLOSED":
        statusColor = Color.web("#757575"); // Gray - Closed
        statusText = "CLOSED";
        colorAdjust.setHue(0.0);
        colorAdjust.setSaturation(-0.5);
        colorAdjust.setBrightness(-0.2);
        break;
      case "HALF":
        statusColor = Color.web("#FFB74D"); // Orange - Half open
        statusText = "HALF";
        colorAdjust.setHue(0.1);
        colorAdjust.setSaturation(0.3);
        colorAdjust.setBrightness(0.1);
        break;
      case "OPEN":
        statusColor = Color.web("#66BB6A"); // Green - Fully open
        statusText = "OPEN";
        colorAdjust.setHue(-0.3);
        colorAdjust.setSaturation(0.5);
        colorAdjust.setBrightness(0.2);
        break;
      default:
        statusColor = Color.web("#FF9800"); // Orange - Unknown
        statusText = "UNKNOWN";
        colorAdjust.setHue(0.0);
        colorAdjust.setSaturation(0.0);
        colorAdjust.setBrightness(0.0);
    }

    // === Labels ===
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
    nameLabel.setTextFill(Color.web("#202124"));

    Label statusLabel = new Label(statusText);
    statusLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
    statusLabel.setTextFill(statusColor);

    // === Layout ===
    VBox textBox = new VBox(0, nameLabel, statusLabel);
    textBox.setAlignment(Pos.CENTER_LEFT);

    HBox topPane = new HBox(10);
    if (windowIcon != null) {
      topPane.getChildren().add(windowIcon);
    }
    topPane.getChildren().add(textBox);
    topPane.setAlignment(Pos.CENTER_LEFT);

    // === Control buttons (three levels) ===
    final ImageView finalIcon = windowIcon;

    Button closedButton = new Button("CLOSED");
    closedButton.setStyle(
            "-fx-background-color: #757575; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 6 12; " +
                    "-fx-cursor: hand; " +
                    "-fx-background-radius: 5; " +
                    "-fx-font-size: 10px;"
    );
    closedButton.setOnAction(e -> {
      if (onLevelChange != null) {
        onLevelChange.accept("CLOSED");
      }
      statusLabel.setText("CLOSED");
      statusLabel.setTextFill(Color.web("#757575"));
      if (finalIcon != null) {
        colorAdjust.setHue(0.0);
        colorAdjust.setSaturation(-0.5);
        colorAdjust.setBrightness(-0.2);
      }
    });

    Button halfButton = new Button("HALF");
    halfButton.setStyle(
            "-fx-background-color: #FFB74D; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 6 12; " +
                    "-fx-cursor: hand; " +
                    "-fx-background-radius: 5; " +
                    "-fx-font-size: 10px;"
    );
    halfButton.setOnAction(e -> {
      if (onLevelChange != null) {
        onLevelChange.accept("HALF");
      }
      statusLabel.setText("HALF");
      statusLabel.setTextFill(Color.web("#FFB74D"));
      if (finalIcon != null) {
        colorAdjust.setHue(0.1);
        colorAdjust.setSaturation(0.3);
        colorAdjust.setBrightness(0.1);
      }
    });

    Button openButton = new Button("OPEN");
    openButton.setStyle(
            "-fx-background-color: #66BB6A; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 6 12; " +
                    "-fx-cursor: hand; " +
                    "-fx-background-radius: 5; " +
                    "-fx-font-size: 10px;"
    );
    openButton.setOnAction(e -> {
      if (onLevelChange != null) {
        onLevelChange.accept("OPEN");
      }
      statusLabel.setText("OPEN");
      statusLabel.setTextFill(Color.web("#66BB6A"));
      if (finalIcon != null) {
        colorAdjust.setHue(-0.3);
        colorAdjust.setSaturation(0.5);
        colorAdjust.setBrightness(0.2);
      }
    });

    HBox controls = new HBox(5, closedButton, halfButton, openButton);
    controls.setAlignment(Pos.CENTER);
    controls.setPadding(new Insets(10, 0, 0, 0));

    // === Final assembly ===
    VBox layout = new VBox(10, topPane, controls);
    layout.setPadding(new Insets(10));
    layout.setAlignment(Pos.TOP_LEFT);
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
  private WindowActuatorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}
