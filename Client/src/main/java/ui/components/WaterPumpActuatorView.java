package ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.Consumer;

/**
 * Visual component for displaying and controlling a water pump actuator in a modern card style.
 * LAYOUT MODIFIED: Separates Name/Status from Icon/Controls. Button text size reduced.
 *
 * @author Green House Control Team
 * @version 3.1 (Layout modified to separate status display from controls, button text smaller)
 */
public class WaterPumpActuatorView {

  // Visual constants
  private static final double CARD_WIDTH = 280.0;
  private static final double ICON_SIZE = 50.0;
  private static final Color CARD_BORDER_COLOR = Color.web("#CCCCCC"); // Light gray border

  /**
   * Creates a visual representation of a water pump actuator with control buttons.
   *
   * @param name Display name for the actuator (e.g., "Water Pump")
   * @param state Current state from server ("ON", "OFF", or "UNKNOWN")
   * @param onToggle Callback function to handle button clicks.
   * Called with true for ON button, false for OFF button.
   * @return A Pane containing the complete actuator visualization and controls
   */
  public static Pane create(String name, String state, Consumer<Boolean> onToggle) {
    // === Icon ===
    ImageView pumpIcon = null;
    try {
      Image image = new Image(
          WaterPumpActuatorView.class.getResourceAsStream("/icons/waterpump.png")
      );
      pumpIcon = new ImageView(image);
      pumpIcon.setFitWidth(ICON_SIZE);
      pumpIcon.setFitHeight(ICON_SIZE);
      pumpIcon.setPreserveRatio(true);
    } catch (Exception e) {
      System.err.println("⚠️ Could not load water pump icon: " + e.getMessage());
    }

    // === 1. Determine Status Color, Text, and Visuals ===
    Color baseColor;
    Color darkColor;
    Color activeIndicatorColor;
    String statusText;
    boolean isOn = "ON".equalsIgnoreCase(state);

    if (isOn) {
      baseColor = Color.web("#42A5F5"); // Lighter Blue - Active
      darkColor = Color.web("#1565C0"); // Darker Blue
      activeIndicatorColor = Color.web("#42A5F5");
      statusText = "PUMPING";
    } else if ("OFF".equalsIgnoreCase(state)) {
      baseColor = Color.web("#B0BEC5"); // Light Gray - Inactive
      darkColor = Color.web("#757575"); // Dark Gray
      activeIndicatorColor = Color.web("#9E9E9E");
      statusText = "OFF";
    } else {
      baseColor = Color.web("#FFCC80"); // Light Orange - Unknown
      darkColor = Color.web("#F57C00"); // Dark Orange
      activeIndicatorColor = Color.web("#F57C00");
      statusText = "UNKNOWN";
    }

    Color lightTextColor = Color.WHITE; // Status bar text color

    // === 2. Content Layout - Modified Structure ===

    // --- Active/Enabled Indicator (Circle) ---
    Circle activeIndicator = new Circle(4);
    activeIndicator.setFill(activeIndicatorColor);
    activeIndicator.setStroke(Color.WHITE);
    activeIndicator.setStrokeWidth(1.5);
    if (isOn) {
      activeIndicator.setEffect(new DropShadow(BlurType.GAUSSIAN, activeIndicatorColor, 8, 0.7, 0, 0));
    }

    // --- Status Label (e.g., "PUMPING") ---
    Label mainStatusLabel = new Label(statusText);
    mainStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
    mainStatusLabel.setTextFill(darkColor);

    // --- Name Label (e.g., "Water Pump") ---
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 16));
    nameLabel.setTextFill(Color.web("#555555"));

    // VBox for Name, Indicator, and Status (The main information block)
    VBox infoBlock = new VBox(5);
    infoBlock.setAlignment(Pos.TOP_LEFT);

    HBox titleAndIndicator = new HBox(5, nameLabel, activeIndicator);
    titleAndIndicator.setAlignment(Pos.CENTER_LEFT);

    infoBlock.getChildren().addAll(titleAndIndicator, mainStatusLabel);


    // --- 3. Control Buttons (Smaller Text) ---
    Button onButton = new Button("SET ON");
    onButton.setStyle(
        "-fx-background-color: #1565C0; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 11px; " + // Smaller font size
            "-fx-padding: 8 10; " + // Adjusted padding
            "-fx-cursor: hand; " +
            "-fx-background-radius: 5;"
    );

    // Retain existing logic
    onButton.setOnAction(e -> {
      if (onToggle != null) {
        onToggle.accept(true);
      }
      // Immediate visual feedback (retained from original logic)
      mainStatusLabel.setText("ON");
      mainStatusLabel.setTextFill(Color.web("#1565C0"));
    });

    Button offButton = new Button("SET OFF");
    offButton.setStyle(
        "-fx-background-color: #757575; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 11px; " + // Smaller font size
            "-fx-padding: 8 10; " + // Adjusted padding
            "-fx-cursor: hand; " +
            "-fx-background-radius: 5;"
    );
    // Retain existing logic
    offButton.setOnAction(e -> {
      if (onToggle != null) {
        onToggle.accept(false);
      }
      // Immediate visual feedback (retained from original logic)
      mainStatusLabel.setText("OFF");
      mainStatusLabel.setTextFill(Color.web("#757575"));
    });

    HBox controlsBox = new HBox(8, onButton, offButton);
    controlsBox.setAlignment(Pos.CENTER_RIGHT);

    // --- Icon Box ---
    VBox iconBox = new VBox();
    iconBox.setAlignment(Pos.CENTER);
    if (pumpIcon != null) {
      iconBox.getChildren().add(pumpIcon);
    }

    // --- HBox Combining Icon and Controls ---
    HBox iconAndControls = new HBox(20);
    iconAndControls.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(controlsBox, Priority.ALWAYS); // Push controls to the right
    iconAndControls.getChildren().addAll(iconBox, controlsBox);

    // --- 4. Main Content Layout ---
    VBox contentBox = new VBox(20, infoBlock, iconAndControls);
    contentBox.setAlignment(Pos.TOP_LEFT);
    contentBox.setPadding(new Insets(15, 20, 20, 20));


    // --- 5. Status Bar and Menu Button (Card Top) ---
    Label indicatorLabel = new Label("ACTUATOR STATUS");
    indicatorLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
    indicatorLabel.setTextFill(lightTextColor);

    HBox statusBar = new HBox();
    statusBar.setAlignment(Pos.CENTER_LEFT);
    statusBar.setPadding(new Insets(5, 10, 5, 10));
    statusBar.setBackground(new Background(new BackgroundFill(baseColor, new CornerRadii(8, 8, 0, 0, false), Insets.EMPTY)));

    HBox statusBox = new HBox(indicatorLabel);
    statusBox.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(statusBox, Priority.ALWAYS);

    // Delete Menu Button
    Circle dot1 = new Circle(2.5, Color.web("#ffffff"));
    Circle dot2 = new Circle(2.5, Color.web("#ffffff"));
    Circle dot3 = new Circle(2.5, Color.web("#ffffff"));
    HBox dots = new HBox(3, dot1, dot2, dot3);
    dots.setAlignment(Pos.CENTER);

    Button menuButton = new Button();
    menuButton.setGraphic(dots);
    menuButton.setStyle("-fx-background-color: transparent; -fx-padding: 5 10 5 10; -fx-cursor: hand;");

    ContextMenu contextMenu = new ContextMenu();
    MenuItem deleteItem = new MenuItem("Delete this actuator");
    deleteItem.setStyle("-fx-text-fill: #0d0c0c;");
    contextMenu.getItems().add(deleteItem);

    menuButton.setOnMouseClicked(event -> {
      contextMenu.show(menuButton, event.getScreenX(), event.getScreenY());
    });

    statusBar.getChildren().addAll(statusBox, menuButton);

    // --- 6. Final Assembly (Card Style) ---
    VBox layout = new VBox();
    layout.getChildren().addAll(statusBar, contentBox);
    layout.setAlignment(Pos.TOP_CENTER);

    layout.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(8), Insets.EMPTY)));
    layout.setBorder(new Border(new BorderStroke(CARD_BORDER_COLOR, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
    layout.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.1), 10, 0, 0, 5));
    layout.setPrefWidth(CARD_WIDTH);
    layout.setMaxWidth(CARD_WIDTH);

    return layout;
  }

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private WaterPumpActuatorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}