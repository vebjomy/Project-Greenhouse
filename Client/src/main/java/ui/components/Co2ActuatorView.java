package ui.components;

import java.util.function.Consumer;
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
 * Visual component for displaying and controlling a CO₂ generator actuator in a modern card style.
 * LAYOUT MODIFIED: Separates Name/Status from Icon/Controls.
 *
 * @author Green House Control Team
 * @version 3.1 (Layout modified to separate status display from controls)
 */
public class Co2ActuatorView {

  // Visual constants
  private static final double CARD_WIDTH = 280.0;
  private static final double ICON_SIZE = 50.0;
  private static final Color CARD_BORDER_COLOR = Color.web("#CCCCCC"); // Light gray border

  /**
   * Creates a visual representation of a CO₂ generator actuator with control buttons.
   *
   * @param name     Display name for the actuator (e.g., "CO₂ Generator")
   * @param state    Current state from server ("ON", "OFF", or "UNKNOWN")
   * @param onToggle Callback function to handle button clicks. Called with true for ON button,
   *                 false for OFF button.
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
      co2Icon.setFitWidth(ICON_SIZE);
      co2Icon.setFitHeight(ICON_SIZE);
      co2Icon.setPreserveRatio(true);
    } catch (Exception e) {
      System.err.println("⚠️ Could not load CO2 icon: " + e.getMessage());
    }

    // === 1. Determine Status Color, Text, and Visuals ===
    Color baseColor;
    Color darkColor;
    Color activeIndicatorColor;
    String statusText;
    boolean isOn = "ON".equalsIgnoreCase(state);

    if (isOn) {
      baseColor = Color.web("#FF7043"); // Light Deep Orange - Active
      darkColor = Color.web("#E64A19"); // Darker Deep Orange
      activeIndicatorColor = Color.web("#FF7043");
      statusText = "GENERATING";
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

    // === 2. Content Layout - Modified Structure ===

    // --- Active/Enabled Indicator (Circle) ---
    Circle activeIndicator = new Circle(4);
    activeIndicator.setFill(activeIndicatorColor);
    activeIndicator.setStroke(Color.WHITE);
    activeIndicator.setStrokeWidth(1.5);
    if (isOn) {
      activeIndicator.setEffect(
          new DropShadow(BlurType.GAUSSIAN, activeIndicatorColor, 8, 0.7, 0, 0));
    }

    // --- Status Label (e.g., "GENERATING") ---
    Label mainStatusLabel = new Label(statusText);
    mainStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
    mainStatusLabel.setTextFill(darkColor);

    // --- Name Label (e.g., "CO₂ Generator") ---
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 16));
    nameLabel.setTextFill(Color.web("#555555"));

    // VBox for Name, Indicator, and Status (The main information block)
    VBox infoBlock = new VBox(5);
    infoBlock.setAlignment(Pos.TOP_LEFT);

    HBox titleAndIndicator = new HBox(5, nameLabel, activeIndicator);
    titleAndIndicator.setAlignment(Pos.CENTER_LEFT);

    infoBlock.getChildren().addAll(titleAndIndicator, mainStatusLabel);

    // --- Control Buttons ---
    Button onButton = new Button("SET ON");
    onButton.setStyle(
        "-fx-background-color: #E64A19; "
            + "-fx-text-fill: white; "
            + "-fx-font-size: 8px; " // Smaller font size
            + "-fx-font-weight: bold; "
            + "-fx-padding: 8 15; "
            + "-fx-cursor: hand; "
            + "-fx-background-radius: 5;"
    );
    // Retain existing logic
    onButton.setOnAction(e -> {
      if (onToggle != null) {
        onToggle.accept(true);
      }
      // Note: Full UI redraw/update is typically handled externally.
    });

    Button offButton = new Button("SET OFF");
    offButton.setStyle(
        "-fx-background-color: #757575; "
            + "-fx-text-fill: white; "
            + "-fx-font-size: 8px; " // Smaller font size
            + "-fx-font-weight: bold; "
            + "-fx-padding: 8 15; "
            + "-fx-cursor: hand; "
            + "-fx-background-radius: 5;"
    );
    // Retain existing logic
    offButton.setOnAction(e -> {
      if (onToggle != null) {
        onToggle.accept(false);
      }
      // Note: Full UI redraw/update is typically handled externally.
    });

    HBox controlsBox = new HBox(8, onButton, offButton);
    controlsBox.setAlignment(Pos.CENTER_RIGHT);

    // --- Icon Box ---
    VBox iconBox = new VBox();
    iconBox.setAlignment(Pos.CENTER);
    if (co2Icon != null) {
      iconBox.getChildren().add(co2Icon);
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
    Color lightTextColor = Color.WHITE; // Status bar text color
    indicatorLabel.setTextFill(lightTextColor);

    HBox statusBar = new HBox();
    statusBar.setAlignment(Pos.CENTER_LEFT);
    statusBar.setPadding(new Insets(5, 10, 5, 10));
    statusBar.setBackground(new Background(
        new BackgroundFill(baseColor, new CornerRadii(8, 8, 0, 0, false), Insets.EMPTY)));

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
    menuButton.setStyle(
        "-fx-background-color: transparent; -fx-padding: 5 10 5 10; -fx-cursor: hand;");

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
   * Converts a Color to hex string format. (Retained from original)
   */
  private static String toHexString(Color color) {
    return String.format("#%02X%02X%02X",
        (int) (color.getRed() * 255),
        (int) (color.getGreen() * 255),
        (int) (color.getBlue() * 255));
  }

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private Co2ActuatorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}