package ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.Consumer;

/**
 * Visual component for displaying and controlling a CO₂ generator actuator.
 * Uses the ceo2.png icon from resources.
 *
 * @author Green House Control Team
 * @version 2.0
 */
public class Co2ActuatorView {

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
    // === Icon ===
    ImageView co2Icon = null;
    try {
      Image image = new Image(
              Co2ActuatorView.class.getResourceAsStream("/icons/ceo2.png")
      );
      co2Icon = new ImageView(image);
      co2Icon.setFitWidth(50);
      co2Icon.setFitHeight(50);
      co2Icon.setPreserveRatio(true);
    } catch (Exception e) {
      System.err.println("⚠️ Could not load CO2 icon: " + e.getMessage());
    }

    // === Color coding based on state ===
    Color statusColor;
    String statusText;
    boolean isOn = "ON".equalsIgnoreCase(state);

    if (isOn) {
      statusColor = Color.web("#FF7043"); // Deep Orange - Active/Generating
      statusText = "ON";
    } else if ("OFF".equalsIgnoreCase(state)) {
      statusColor = Color.web("#757575"); // Gray - Inactive
      statusText = "OFF";
    } else {
      statusColor = Color.web("#FF9800"); // Orange - Unknown
      statusText = "UNKNOWN";
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
    if (co2Icon != null) {
      topPane.getChildren().add(co2Icon);
    }
    topPane.getChildren().add(textBox);
    topPane.setAlignment(Pos.CENTER_LEFT);

    // === Control buttons ===
    Button onButton = new Button("ON");
    onButton.setStyle(
            "-fx-background-color: #FF7043; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 8 20; " +
                    "-fx-cursor: hand; " +
                    "-fx-background-radius: 5;"
    );
    onButton.setOnAction(e -> {
      if (onToggle != null) {
        onToggle.accept(true);
      }
      statusLabel.setText("ON");
      statusLabel.setTextFill(Color.web("#FF7043"));
    });

    Button offButton = new Button("OFF");
    offButton.setStyle(
            "-fx-background-color: #757575; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 8 20; " +
                    "-fx-cursor: hand; " +
                    "-fx-background-radius: 5;"
    );
    offButton.setOnAction(e -> {
      if (onToggle != null) {
        onToggle.accept(false);
      }
      statusLabel.setText("OFF");
      statusLabel.setTextFill(Color.web("#757575"));
    });

    HBox controls = new HBox(8, onButton, offButton);
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
  private Co2ActuatorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}
