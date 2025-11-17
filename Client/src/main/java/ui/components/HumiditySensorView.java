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
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Visual component for displaying humidity sensor readings in a modern card style
 * with a horizontal humidity spectrum bar and a context menu button for deletion.
 * Uses the same horizontal layout pattern as TemperatureSensorView.
 *
 * @author Green House Control Team
 * @version 3.2 (Added Humidity Range Scale below spectrum bar)
 */
public class HumiditySensorView {

  // Humidity thresholds for color coding (percentage)
  private static final double HUMIDITY_LOW_THRESHOLD = 30.0;
  private static final double HUMIDITY_HIGH_THRESHOLD = 70.0;
  private static final double HUMIDITY_MIN = 0.0;
  private static final double HUMIDITY_MAX = 100.0;

  // Visual constants
  private static final double CARD_WIDTH = 250.0;
  private static final double ICON_SIZE = 24.0;
  private static final double BAR_WIDTH = 210.0; // Wider bar for spectrum
  private static final double BAR_HEIGHT = 12.0;

  /**
   * Creates a visual representation of a humidity sensor in a modern card style.
   *
   * @param name Display name for the sensor (e.g., "Humidity")
   * @param value Current humidity value (0-100)
   * @param unit Unit string to display (e.g., "%")
   * @return A Pane containing the complete sensor visualization
   */
  public static Pane create(String name, double value, String unit) {
    // Clamp value to valid range
    double clampedValue = Math.max(HUMIDITY_MIN, Math.min(HUMIDITY_MAX, value));

    // --- 1. Determine Status Color and Text ---
    Color baseColor;
    Color darkColor;
    String statusText;

    if (clampedValue < HUMIDITY_LOW_THRESHOLD) {
      baseColor = Color.web("#FFB74D"); // Orange - Low
      darkColor = Color.web("#F57C00");
      statusText = "LOW (DRY)";
    } else if (clampedValue > HUMIDITY_HIGH_THRESHOLD) {
      baseColor = Color.web("#4FC3F7"); // Light Blue - High
      darkColor = Color.web("#0288D1");
      statusText = "HIGH (WET)";
    } else {
      baseColor = Color.web("#81C784"); // Light Green - Normal
      darkColor = Color.web("#388E3C");
      statusText = "NORMAL";
    }

    Color textColor = Color.WHITE; // Use white for high contrast status bar

    // --- 2. Labels & Icon Setup ---
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    nameLabel.setTextFill(Color.web("#333333"));

    Label valueLabel = new Label(String.format("%.1f", clampedValue));
    valueLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 36));
    valueLabel.setTextFill(darkColor);

    Label unitLabel = new Label(unit);
    unitLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    unitLabel.setTextFill(Color.web("#616161"));

    // HBox for Value + Unit
    HBox valueAndUnitBox = new HBox(3, valueLabel, unitLabel);
    valueAndUnitBox.setAlignment(Pos.BOTTOM_LEFT);

    // Icon
    ImageView icon = null;
    try {
      Image image = new Image(
          HumiditySensorView.class.getResourceAsStream("/icons/humidity_sensor.png")
      );
      icon = new ImageView(image);
      icon.setFitWidth(ICON_SIZE + 25);
      icon.setFitHeight(ICON_SIZE + 25);
      icon.setPreserveRatio(true);
      icon.setEffect(new DropShadow(BlurType.GAUSSIAN, darkColor.deriveColor(0, 1.0, 1.0, 0.5), 5, 0, 0, 0));
    } catch (Exception e) {
      System.err.println("⚠️ Could not load humidity icon: " + e.getMessage());
    }

    // HBox for Icon + Value/Unit
    HBox iconValueBox = new HBox(35);
    iconValueBox.setAlignment(Pos.CENTER_LEFT);
    if (icon != null) {
      iconValueBox.getChildren().add(icon);
    }
    iconValueBox.getChildren().add(valueAndUnitBox);

    // --- 3. Humidity Spectrum Bar (Full Range Visual) ---

    // Define the humidity gradient spectrum (Low=0%, Normal=30-70%, High=100%)
    LinearGradient spectrumGradient = new LinearGradient(
        0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
        new Stop(0.0, Color.web("#FFB74D")),                    // Orange - Low (0%)
        new Stop(HUMIDITY_LOW_THRESHOLD / HUMIDITY_MAX, Color.web("#FFD54F")), // Yellow transition
        new Stop(0.5, Color.web("#81C784")),                    // Green - Normal (50%)
        new Stop(HUMIDITY_HIGH_THRESHOLD / HUMIDITY_MAX, Color.web("#4FC3F7")), // Light Blue transition
        new Stop(1.0, Color.web("#0288D1"))                     // Blue - High (100%)
    );

    // Background bar (the spectrum itself)
    Rectangle spectrumBar = new Rectangle(BAR_WIDTH, BAR_HEIGHT);
    spectrumBar.setArcWidth(BAR_HEIGHT);
    spectrumBar.setArcHeight(BAR_HEIGHT);
    spectrumBar.setFill(spectrumGradient);
    spectrumBar.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.1), 3, 0, 0, 1));

    // Current humidity indicator (A simple vertical line)
    double positionRatio = clampedValue / HUMIDITY_MAX;
    double indicatorX = (BAR_WIDTH * positionRatio) - 1; // Center the indicator

    Rectangle indicator = new Rectangle(2, BAR_HEIGHT + 4); // Vertical line slightly taller than the bar
    indicator.setFill(Color.BLACK); // Marker color
    indicator.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 5, 0, 0, 0));

    // Container to position the indicator over the bar
    StackPane indicatorStack = new StackPane(indicator);
    indicatorStack.setPrefWidth(BAR_WIDTH);
    indicatorStack.setMaxWidth(BAR_WIDTH);
    indicatorStack.setMinWidth(BAR_WIDTH); // Fix the width for absolute positioning
    StackPane.setAlignment(indicator, Pos.CENTER_LEFT);
    indicator.setTranslateX(indicatorX);

    // --- 3.5 Humidity Range Scale (New Section) ---
    Label lowLabel = new Label("LOW");
    lowLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
    lowLabel.setTextFill(Color.web("#888888"));

    Label normalLabel = new Label("NORMAL");
    normalLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
    normalLabel.setTextFill(Color.web("#888888"));

    Label highLabel = new Label("HIGH");
    highLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
    highLabel.setTextFill(Color.web("#888888"));

    HBox scaleBox = new HBox();
    scaleBox.setPrefWidth(BAR_WIDTH);
    scaleBox.setMaxWidth(BAR_WIDTH);
    scaleBox.setMinWidth(BAR_WIDTH);
    scaleBox.setAlignment(Pos.CENTER_LEFT);
    scaleBox.setPadding(new Insets(2, 0, 0, 0)); // Little padding at the top

    // Add labels with spacers for proportional distribution
    scaleBox.getChildren().add(lowLabel);

    // Spacer to push the next label
    Region lowToNormalSpacer = new Region();
    HBox.setHgrow(lowToNormalSpacer, Priority.ALWAYS);
    scaleBox.getChildren().add(lowToNormalSpacer);

    // Normal label
    scaleBox.getChildren().add(normalLabel);

    // Spacer to push the last label
    Region normalToHighSpacer = new Region();
    HBox.setHgrow(normalToHighSpacer, Priority.ALWAYS);
    scaleBox.getChildren().add(normalToHighSpacer);

    // High label at the right
    scaleBox.getChildren().add(highLabel);

    // Final Bar Assembly
    VBox barAssembly = new VBox(
        spectrumBar,
        indicatorStack,
        scaleBox // ADDED the scale box
    );
    barAssembly.setSpacing(5);
    barAssembly.setAlignment(Pos.TOP_LEFT);

    // --- 4. Main Content Layout ---
    VBox leftContent = new VBox(5, nameLabel, iconValueBox);
    leftContent.setAlignment(Pos.CENTER_LEFT);

    VBox contentBox = new VBox(10, leftContent, barAssembly);
    contentBox.setAlignment(Pos.TOP_LEFT);
    contentBox.setPadding(new Insets(10, 20, 20, 20));

    // --- 5. Status Bar and Menu Button ---
    Label statusLabel = new Label(statusText);
    statusLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
    statusLabel.setTextFill(textColor);

    HBox statusBar = new HBox();
    statusBar.setAlignment(Pos.CENTER_LEFT);
    statusBar.setPadding(new Insets(5, 10, 5, 10));
    statusBar.setBackground(new Background(new BackgroundFill(baseColor, new CornerRadii(8, 8, 0, 0, false), Insets.EMPTY)));

    HBox statusBox = new HBox(statusLabel);
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
    MenuItem deleteItem = new MenuItem("Delete this sensor");
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
    layout.setBorder(new Border(new BorderStroke(Color.web("#EEEEEE"), BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
    layout.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.1), 10, 0, 0, 5));
    layout.setPrefWidth(CARD_WIDTH);
    layout.setMaxWidth(CARD_WIDTH);

    return layout;
  }

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private HumiditySensorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}