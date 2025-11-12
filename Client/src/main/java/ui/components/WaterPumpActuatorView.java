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
 * Visual component for displaying and controlling a water pump actuator.
 *
 * This component provides:
 * - Visual indication of current pump state
 * - ON/OFF control buttons
 * - Callback mechanism to send commands to the server
 *
 * @author Green House Control Team
 * @version 1.0
 */
public class WaterPumpActuatorView {

  // SVG path for water pump icon (Material Design)
  private static final String PUMP_ICON_PATH =
          "M19,14V17H17.5V20H15V17H9V20H6.5V17H5V14H6.11C6.59,12.06 8.36,10.68 10.44,10.81C11.5," +
                  "10.87 12.5,11.31 13.21,12.03L14.21,13.03L15.21,12.03C16.29,10.95 17.93,10.61 19.32," +
                  "11.18C20.9,11.84 22,13.37 22,15.09C22,15.73 21.81,16.35 21.46,16.88C21.11,17.41 20.63," +
                  "17.84 20.07,18.11L19,14M12,9C10.9,9 10,8.11 10,7C10,5.89 10.9,5 12,5C13.11,5 14,5.89 " +
                  "14,7C14,8.11 13.11,9 12,9Z";

  /**
   * Creates a visual representation of a water pump actuator with control buttons.
   *
   * @param name Display name for the actuator (e.g., "Water Pump")
   * @param state Current state from server ("ON", "OFF", or "UNKNOWN")
   * @param onToggle Callback function to handle button clicks.
   *                 Called with true for ON button, false for OFF button.
   * @return A Pane containing the complete actuator visualization and controls
   */
  public static Pane create(String name, String state, Consumer<Boolean> onToggle) {
    // --- Icon ---
    SVGPath pumpIcon = new SVGPath();
    pumpIcon.setContent(PUMP_ICON_PATH);
    pumpIcon.setScaleX(1.2);
    pumpIcon.setScaleY(1.2);

    // --- Labels ---
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

    Label statusLabel = new Label(state != null ? state : "UNKNOWN");
    statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));

    // --- Color coding based on state ---
    Color statusColor;
    if ("ON".equalsIgnoreCase(state)) {
      statusColor = Color.web("#2196F3"); // Blue - Active/Pumping
    } else if ("OFF".equalsIgnoreCase(state)) {
      statusColor = Color.web("#B0BEC5"); // Gray - Inactive
    } else {
      statusColor = Color.web("#FF9800"); // Orange - Unknown
    }

    pumpIcon.setFill(statusColor);
    statusLabel.setTextFill(statusColor);

    // --- Top section layout ---
    VBox titleBox = new VBox(-2, nameLabel, statusLabel);
    titleBox.setAlignment(Pos.CENTER_LEFT);

    HBox topPane = new HBox(10, pumpIcon, titleBox);
    topPane.setAlignment(Pos.CENTER_LEFT);
    topPane.setPadding(new Insets(0, 0, 10, 0));

    // --- Control buttons ---
    Button onButton = new Button("ON");
    onButton.setStyle(
            "-fx-background-color: #2196F3; " +
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
  private WaterPumpActuatorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}
