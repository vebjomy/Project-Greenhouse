package ui.components;

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Visual component for displaying and controlling a window actuator in a modern card style with
 * status bar, control buttons, and icon color effects.
 *
 * @author Green House Control Team
 * @version 3.2 (Reorganized layout with buttons at bottom for better text fitting)
 */
public class WindowActuatorView {

  // Visual constants
  private static final double CARD_WIDTH = 280.0;
  private static final double ICON_SIZE = 50.0;
  private static final Color CARD_BORDER_COLOR = Color.web("#CCCCCC"); // Light gray border

  /**
   * Creates a visual representation of a window actuator with control buttons and color effects.
   *
   * @param name          Display name for the actuator (e.g., "Window")
   * @param state         Current state from server ("CLOSED", "HALF", "OPEN", or "UNKNOWN")
   * @param onLevelChange Callback function to handle button clicks. Called with "CLOSED", "HALF",
   *                      or "OPEN" string.
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
      windowIcon.setFitWidth(ICON_SIZE);
      windowIcon.setFitHeight(ICON_SIZE);
      windowIcon.setPreserveRatio(true);
      windowIcon.setEffect(colorAdjust);
    } catch (Exception e) {
      System.err.println("⚠️ Could not load window icon: " + e.getMessage());
    }

    // === 1. Determine Status Color, Text, and Visuals ===
    Color baseColor;
    Color darkColor;
    Color activeIndicatorColor;
    String statusText;

    // Use a final reference to colorAdjust for button handlers
    final ColorAdjust finalColorAdjust = colorAdjust;
    final String currentState = state != null ? state.toUpperCase() : "UNKNOWN";

    switch (currentState) {
      case "CLOSED":
        baseColor = Color.web("#B0BEC5"); // Light Gray - Status bar
        darkColor = Color.web("#757575"); // Dark Gray - Text
        activeIndicatorColor = Color.web("#9E9E9E"); // Indicator
        statusText = "CLOSED";
        colorAdjust.setHue(0.0);
        colorAdjust.setSaturation(-0.5);
        colorAdjust.setBrightness(-0.2);
        break;
      case "HALF":
        baseColor = Color.web("#FFB74D"); // Light Orange - Status bar
        darkColor = Color.web("#F57C00"); // Dark Orange - Text
        activeIndicatorColor = Color.web("#FFB74D"); // Indicator
        statusText = "HALF OPEN";
        colorAdjust.setHue(0.1);
        colorAdjust.setSaturation(0.3);
        colorAdjust.setBrightness(0.1);
        break;
      case "OPEN":
        baseColor = Color.web("#66BB6A"); // Light Green - Status bar
        darkColor = Color.web("#388E3C"); // Dark Green - Text
        activeIndicatorColor = Color.web("#66BB6A"); // Indicator
        statusText = "OPEN";
        colorAdjust.setHue(-0.3);
        colorAdjust.setSaturation(0.5);
        colorAdjust.setBrightness(0.2);
        break;
      default:
        baseColor = Color.web("#FFCC80"); // Light Orange - Unknown Status bar
        darkColor = Color.web("#F57C00"); // Orange - Unknown Text
        activeIndicatorColor = Color.web("#F57C00");
        statusText = "UNKNOWN";
        colorAdjust.setHue(0.0);
        colorAdjust.setSaturation(0.0);
        colorAdjust.setBrightness(0.0);
    }

    // === 2. Content Layout - Reorganized Structure ===

    // --- Active/Enabled Indicator (Circle) ---
    Circle activeIndicator = new Circle(4);
    activeIndicator.setFill(activeIndicatorColor);
    activeIndicator.setStroke(Color.WHITE);
    activeIndicator.setStrokeWidth(1.5);
    // Add glow effect if not CLOSED
    if (!"CLOSED".equals(currentState) && !"UNKNOWN".equals(currentState)) {
      activeIndicator.setEffect(
          new DropShadow(BlurType.GAUSSIAN, activeIndicatorColor, 8, 0.7, 0, 0));
    }

    // --- Name Label (e.g., "Window") ---
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 16));
    nameLabel.setTextFill(Color.web("#555555"));

    // --- Status Label (e.g., "OPEN") ---
    Label mainStatusLabel = new Label(statusText);
    mainStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
    mainStatusLabel.setTextFill(darkColor);

    // HBox for Name and Indicator (top row)
    HBox titleAndIndicator = new HBox(5, nameLabel, activeIndicator);
    titleAndIndicator.setAlignment(Pos.CENTER_LEFT);

    // VBox for Status and Icon (middle row)
    HBox statusAndIcon = new HBox(15);
    statusAndIcon.setAlignment(Pos.CENTER_LEFT);

    // Status text
    VBox statusBox = new VBox(2);
    statusBox.setAlignment(Pos.CENTER_LEFT);
    Label statusTextLabel = new Label("Status:");
    statusTextLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
    statusTextLabel.setTextFill(Color.web("#777777"));
    statusBox.getChildren().addAll(statusTextLabel, mainStatusLabel);

    // Icon box
    VBox iconBox = new VBox();
    iconBox.setAlignment(Pos.CENTER);
    if (windowIcon != null) {
      iconBox.getChildren().add(windowIcon);
    }

    statusAndIcon.getChildren().addAll(statusBox, iconBox);
    HBox.setHgrow(statusBox, Priority.ALWAYS);

    // === 3. Control Buttons (Bottom Row) ===
    final ImageView finalIcon = windowIcon;
    final Label finalStatusLabel = mainStatusLabel; // Reference for status text update

    Button closedButton = new Button("CLOSED");
    closedButton.setStyle(
        "-fx-background-color: #757575; "
            + "-fx-text-fill: white; "
            + "-fx-font-weight: bold; "
            + "-fx-font-size: 11px; "
            + "-fx-padding: 8 10; "
            + "-fx-cursor: hand; "
            + "-fx-background-radius: 5;"
    );
    closedButton.setOnAction(e -> {
      if (onLevelChange != null) {
        onLevelChange.accept("CLOSED");
      }
      finalStatusLabel.setText("CLOSED");
      finalStatusLabel.setTextFill(Color.web("#757575"));
      if (finalIcon != null) {
        finalColorAdjust.setHue(0.0);
        finalColorAdjust.setSaturation(-0.5);
        finalColorAdjust.setBrightness(-0.2);
      }
    });

    Button halfButton = new Button("HALF");
    halfButton.setStyle(
        "-fx-background-color: #F57C00; "
            + "-fx-text-fill: white; "
            + "-fx-font-weight: bold; "
            + "-fx-font-size: 11px; "
            + "-fx-padding: 8 10; "
            + "-fx-cursor: hand; "
            + "-fx-background-radius: 5;"
    );
    halfButton.setOnAction(e -> {
      if (onLevelChange != null) {
        onLevelChange.accept("HALF");
      }
      finalStatusLabel.setText("HALF OPEN");
      finalStatusLabel.setTextFill(Color.web("#F57C00"));
      if (finalIcon != null) {
        finalColorAdjust.setHue(0.1);
        finalColorAdjust.setSaturation(0.3);
        finalColorAdjust.setBrightness(0.1);
      }
    });

    Button openButton = new Button("OPEN");
    openButton.setStyle(
        "-fx-background-color: #388E3C; "
            + "-fx-text-fill: white; "
            + "-fx-font-weight: bold; "
            + "-fx-font-size: 11px; "
            + "-fx-padding: 8 10; "
            + "-fx-cursor: hand; "
            + "-fx-background-radius: 5;"
    );
    openButton.setOnAction(e -> {
      if (onLevelChange != null) {
        onLevelChange.accept("OPEN");
      }
      finalStatusLabel.setText("OPEN");
      finalStatusLabel.setTextFill(Color.web("#388E3C"));
      if (finalIcon != null) {
        finalColorAdjust.setHue(-0.3);
        finalColorAdjust.setSaturation(0.5);
        finalColorAdjust.setBrightness(0.2);
      }
    });

    HBox controls = new HBox(8, closedButton, halfButton, openButton);
    controls.setAlignment(Pos.CENTER);
    controls.setPadding(new Insets(10, 0, 0, 0));

    // === 4. Main Content Layout ===
    VBox contentBox = new VBox(15, titleAndIndicator, statusAndIcon, controls);
    contentBox.setAlignment(Pos.TOP_LEFT);
    contentBox.setPadding(new Insets(15, 20, 20, 20));

    // === 5. Status Bar and Menu Button (Card Top) ===
    Label indicatorLabel = new Label("ACTUATOR STATUS");
    indicatorLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
    Color lightTextColor = Color.WHITE; // Status bar text color
    indicatorLabel.setTextFill(lightTextColor);

    HBox statusBar = new HBox();
    statusBar.setAlignment(Pos.CENTER_LEFT);
    statusBar.setPadding(new Insets(5, 10, 5, 10));
    statusBar.setBackground(new Background(
        new BackgroundFill(baseColor, new CornerRadii(8, 8, 0, 0, false), Insets.EMPTY)));

    HBox statusBoxTop = new HBox(indicatorLabel);
    statusBoxTop.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(statusBoxTop, Priority.ALWAYS);

    // Delete Menu Button
    Circle dot1 = new Circle(2.5, Color.web("#ffffff"));
    Circle dot2 = new Circle(2.5, Color.web("#ffffff"));
    Circle dot3 = new Circle(2.5, Color.web("#ffffff"));
    HBox dots = new HBox(3, dot1, dot2, dot3);
    dots.setAlignment(Pos.CENTER);

    Button menuButton = new Button();
    menuButton.setGraphic(dots);
    menuButton.setStyle(
        "-fx-background-color: transparent; -fx-padding: 5 10 5 10; -fx-cursor: hand;");

    ContextMenu contextMenu = new ContextMenu();
    MenuItem deleteItem = new MenuItem("Delete this actuator");
    deleteItem.setStyle("-fx-text-fill: #0d0c0c;");
    contextMenu.getItems().add(deleteItem);

    menuButton.setOnMouseClicked(event -> {
      contextMenu.show(menuButton, event.getScreenX(), event.getScreenY());
    });

    statusBar.getChildren().addAll(statusBoxTop, menuButton);

    // === 6. Final Assembly (Card Style) ===
    VBox layout = new VBox();
    layout.getChildren().addAll(statusBar, contentBox);
    layout.setAlignment(Pos.TOP_CENTER);

    layout.setBackground(
        new Background(new BackgroundFill(Color.WHITE, new CornerRadii(8), Insets.EMPTY)));
    layout.setBorder(new Border(
        new BorderStroke(CARD_BORDER_COLOR, BorderStrokeStyle.SOLID, new CornerRadii(8),
            new BorderWidths(1))));
    layout.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.1), 10, 0, 0, 5));
    layout.setPrefWidth(CARD_WIDTH);
    layout.setMaxWidth(CARD_WIDTH);

    return layout;
  }

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private WindowActuatorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}