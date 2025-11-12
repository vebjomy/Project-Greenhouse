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
 * Visual component for displaying and controlling a window actuator.
 *
 * This component provides:
 * - Visual indication of current window state (CLOSED/HALF/OPEN)
 * - Three-level control buttons
 * - Callback mechanism to send commands to the server
 *
 * Unlike boolean actuators (ON/OFF), windows have three discrete states.
 *
 * @author Green House Control Team
 * @version 1.0
 */
public class WindowActuatorView {

  // SVG paths for different window states
  private static final String WINDOW_CLOSED_PATH =
          "M3,21V19H21V21H3M12,17V5H21V17H12M3,17V5H12V17H3Z";

  private static final String WINDOW_HALF_PATH =
          "M3,21V19H21V21H3M12,17V5H21V17M3,17V5H12V17H3Z";

  private static final String WINDOW_OPEN_PATH =
          "M3,21V19H21V21H3M3,17V5H21V17H3M12,5V17Z";

  /**
   * Creates a visual representation of a window actuator with control buttons.
   *
   * @param name Display name for the actuator (e.g., "Window")
   * @param state Current state from server ("CLOSED", "HALF", "OPEN", or "UNKNOWN")
   * @param onLevelChange Callback function to handle button clicks.
   *                      Called with "CLOSED", "HALF", or "OPEN" string.
   * @return A Pane containing the complete actuator visualization and controls
   */
  public static Pane create(String name, String state, Consumer<String> onLevelChange) {
    // --- Icon (changes based on state) ---
    SVGPath windowIcon = new SVGPath();

    // Select icon based on state
    String iconPath = switch (state != null ? state.toUpperCase() : "UNKNOWN") {
      case "CLOSED" -> WINDOW_CLOSED_PATH;
      case "HALF" -> WINDOW_HALF_PATH;
      case "OPEN" -> WINDOW_OPEN_PATH;
      default -> WINDOW_CLOSED_PATH;
    };

    windowIcon.setContent(iconPath);
    windowIcon.setScaleX(1.2);
    windowIcon.setScaleY(1.2);

    // --- Labels ---
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

    Label statusLabel = new Label(state != null ? state : "UNKNOWN");
    statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));

    // --- Color coding based on state ---
    Color statusColor = switch (state != null ? state.toUpperCase() : "UNKNOWN") {
      case "CLOSED" -> Color.web("#B0BEC5"); // Gray - Closed
      case "HALF" -> Color.web("#FFD54F");   // Yellow - Half open
      case "OPEN" -> Color.web("#81C784");   // Green - Fully open
      default -> Color.web("#FF9800");       // Orange - Unknown
    };

    windowIcon.setFill(statusColor);
    statusLabel.setTextFill(statusColor);

    // --- Top section layout ---
    VBox titleBox = new VBox(-2, nameLabel, statusLabel);
    titleBox.setAlignment(Pos.CENTER_LEFT);

    HBox topPane = new HBox(10, windowIcon, titleBox);
    topPane.setAlignment(Pos.CENTER_LEFT);
    topPane.setPadding(new Insets(0, 0, 10, 0));

    // --- Control buttons (three levels) ---
    Button closedButton = new Button("CLOSED");
    closedButton.setStyle(
            "-fx-background-color: #757575; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 5 10; " +
                    "-fx-cursor: hand; " +
                    "-fx-background-radius: 5; " +
                    "-fx-font-size: 11px;"
    );
    closedButton.setOnAction(e -> {
      if (onLevelChange != null) {
        onLevelChange.accept("CLOSED");
      }
    });

    Button halfButton = new Button("HALF");
    halfButton.setStyle(
            "-fx-background-color: #FFC107; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 5 10; " +
                    "-fx-cursor: hand; " +
                    "-fx-background-radius: 5; " +
                    "-fx-font-size: 11px;"
    );
    halfButton.setOnAction(e -> {
      if (onLevelChange != null) {
        onLevelChange.accept("HALF");
      }
    });

    Button openButton = new Button("OPEN");
    openButton.setStyle(
            "-fx-background-color: #4CAF50; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 5 10; " +
                    "-fx-cursor: hand; " +
                    "-fx-background-radius: 5; " +
                    "-fx-font-size: 11px;"
    );
    openButton.setOnAction(e -> {
      if (onLevelChange != null) {
        onLevelChange.accept("OPEN");
      }
    });

    HBox controls = new HBox(5, closedButton, halfButton, openButton);
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
  private WindowActuatorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}
