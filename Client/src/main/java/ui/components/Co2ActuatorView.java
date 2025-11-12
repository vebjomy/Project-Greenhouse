package ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.Consumer;

/**
 * Visual component for displaying and controlling a CO₂ generator actuator.
 *
 * This component provides:
 * - Visual indication of current CO₂ generator state
 * - ON/OFF control buttons
 * - Callback mechanism to send commands to the server
 *
 * @author Green House Control Team
 * @version 1.0
 */
public class Co2ActuatorView {

  // SVG path for CO₂/gas cylinder icon (Material Design)
  private static final String CO2_ICON_PATH =
          "M18,20H6V18H18V20M14,2V5C14,6.66 12.66,8 11,8C9.34,8 8,6.66 8,5V2H6V5C6,7.76 8.24," +
                  "10 11,10C13.76,10 16,7.76 16,5V2H14M11,12C7.31,12 4,15.14 4,19H18C18,15.14 14.69," +
                  "12 11,12Z";

  /**
   * Creates a visual representation of a CO₂ generator actuator with control buttons.
   *
   * @param name Display name for the actuator (e.g., "CO₂ Generator")
   * @param state Current state from server ("ON", "OFF", or "UNKNOWN")
   * @param onToggle Callback function to handle button clicks.
   *                 Called with true for ON button, false for OFF button.
   * @return A Pane containing the complete actuator visualization and controls
   */
  public static Pane create(String name, String state, Consumer<Boolean> onToggle) {
    // --- Icon ---
    SVGPath co2Icon = new SVGPath();
    co2Icon.setContent(CO2_ICON_PATH);
    co2Icon.setScaleX(1.2);
    co2Icon.setScaleY(1.2);

    // --- Labels ---
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

    Label statusLabel = new Label(state != null ? state : "UNKNOWN");
    statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));

    // --- Color coding based on state ---
    Color statusColor;
    if ("ON".equalsIgnoreCase(state)) {
      statusColor = Color.web("#FF5722"); // Deep Orange - Active/Generating
    } else if ("OFF".equalsIgnoreCase(state)) {
      statusColor = Color.web("#B0BEC5"); // Gray - Inactive
    } else {
      statusColor = Color.web("#FF9800"); // Orange - Unknown
    }

    co2Icon.setFill(statusColor);
    statusLabel.setTextFill(statusColor);

    // --- Top section layout ---
    VBox titleBox = new VBox(-2, nameLabel, statusLabel);
    titleBox.setAlignment(Pos.CENTER_LEFT);

    HBox topPane = new HBox(10, co2Icon, titleBox);
    topPane.setAlignment(Pos.CENTER_LEFT);
    topPane.setPadding(new Insets(0, 0, 10, 0));

    // --- Control buttons ---
    Button onButton = new Button("ON");
    onButton.setStyle(
            "-fx-background-color: #FF5722; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 5 15; " +
                    "-fx-cursor: hand; " +
                    "-fx-background-radius: 5;"
    );
    onButton.setOnAction(e -> {
      if (onToggle != null) {
        onToggle.accept(true);
      }
    });

    Button offButton = new Button("OFF");
    offButton.setStyle(
            "-fx-background-color: #757575; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 5 15; " +
                    "-fx-cursor: hand; " +
                    "-fx-background-radius: 5;"
    );
    offButton.setOnAction(e -> {
      if (onToggle != null) {
        onToggle.accept(false);
      }
    });

    HBox controls = new HBox(5, onButton, offButton);
    controls.setAlignment(Pos.CENTER);

    // --- Final layout assembly ---
    VBox layout = new VBox(10, topPane, controls);
    layout.setPadding(new Insets(10));
    layout.setAlignment(Pos.TOP_CENTER);
    layout.setMinWidth(200);

    return layout;
  }

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private Co2ActuatorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}
