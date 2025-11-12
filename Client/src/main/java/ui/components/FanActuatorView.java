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
 * Visual component for displaying and controlling a fan actuator.
 *
 * This component provides:
 * - Visual indication of current fan state
 * - ON/OFF control buttons
 * - Callback mechanism to send commands to the server
 *
 * @author Green House Control Team
 * @version 1.0
 */
public class FanActuatorView {

  // SVG path for fan icon (Material Design icon)
  private static final String FAN_ICON_PATH =
          "M12,11a1,1,0,1,0,1,1,1,1,0,0,0-1-1m.5-9C17,2,17.1,5.57,14.73,6.75a3.36," +
                  "3.36,0,0,0-1.62,2.47,3.17,3.17,0,0,1,1.23.91C18,8.13,22,8.92,22,12.5c0," +
                  "4.5-3.58,4.6-4.75,2.23a3.44,3.44,0,0,0-2.5-1.62,3.24,3.24,0,0,1-.91,1.23c2," +
                  "3.69,1.2,7.66-2.38,7.66C7,22,6.89,18.42,9.26,17.24a3.46,3.46,0,0,0,1.62-2.45," +
                  "3,3,0,0,1-1.25-.92C5.94,15.85,2,15.07,2,11.5,2,7,5.54,6.89,6.72,9.26A3.39," +
                  "3.39,0,0,0,9.2,10.87a2.91,2.91,0,0,1,.92-1.22C8.13,6,8.92,2,12.48,2Z";

  /**
   * Creates a visual representation of a fan actuator with control buttons.
   *
   * @param name Display name for the actuator (e.g., "Fan")
   * @param state Current state from server ("ON", "OFF", or "UNKNOWN")
   * @param onToggle Callback function to handle button clicks.
   *                 Called with true for ON button, false for OFF button.
   * @return A Pane containing the complete actuator visualization and controls
   */
  public static Pane create(String name, String state, Consumer<Boolean> onToggle) {
    // --- Icon ---
    SVGPath fanIcon = new SVGPath();
    fanIcon.setContent(FAN_ICON_PATH);
    fanIcon.setScaleX(1.2);
    fanIcon.setScaleY(1.2);

    // --- Labels ---
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

    Label statusLabel = new Label(state != null ? state : "UNKNOWN");
    statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));

    // --- Color coding based on state ---
    Color statusColor;
    if ("ON".equalsIgnoreCase(state)) {
      statusColor = Color.web("#4CAF50"); // Green - Active
    } else if ("OFF".equalsIgnoreCase(state)) {
      statusColor = Color.web("#B0BEC5"); // Gray - Inactive
    } else {
      statusColor = Color.web("#FF9800"); // Orange - Unknown
    }

    fanIcon.setFill(statusColor);
    statusLabel.setTextFill(statusColor);

    // --- Top section layout ---
    VBox titleBox = new VBox(-2, nameLabel, statusLabel);
    titleBox.setAlignment(Pos.CENTER_LEFT);

    HBox topPane = new HBox(10, fanIcon, titleBox);
    topPane.setAlignment(Pos.CENTER_LEFT);
    topPane.setPadding(new Insets(0, 0, 10, 0));

    // --- Control buttons ---
    Button onButton = new Button("ON");
    onButton.setStyle(
            "-fx-background-color: #4CAF50; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 5 15; " +
                    "-fx-cursor: hand;"
    );
    onButton.setOnAction(e -> {
      if (onToggle != null) {
        onToggle.accept(true);
      }
    });

    Button offButton = new Button("OFF");
    offButton.setStyle(
            "-fx-background-color: #f44336; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 5 15; " +
                    "-fx-cursor: hand;"
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
  private FanActuatorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}
