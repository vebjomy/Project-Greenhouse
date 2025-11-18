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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Visual component for displaying light sensor readings in a modern card style with a horizontal
 * light spectrum bar and a context menu button for deletion. Uses the same horizontal layout
 * pattern as TemperatureSensorView and HumiditySensorView.
 *
 * @author Green House Control Team
 * @version 3.2 (Added Light Range Scale below spectrum bar)
 */
public class LightSensorView {

  // Light level thresholds for color coding (lux)
  private static final double LIGHT_LOW_THRESHOLD = 5000.0;
  private static final double LIGHT_HIGH_THRESHOLD = 25000.0;
  private static final double LIGHT_MIN = 0.0;
  private static final double LIGHT_MAX = 50000.0;

  // Visual constants
  private static final double CARD_WIDTH = 250.0;
  private static final double ICON_SIZE = 24.0;
  private static final double BAR_WIDTH = 210.0; // Wider bar for spectrum
  private static final double BAR_HEIGHT = 12.0;

  /**
   * Creates a visual representation of a light sensor in a modern card style.
   *
   * @param name  Display name for the sensor (e.g., "Light")
   * @param value Current light value in lux
   * @param unit  Unit string to display (e.g., "lx")
   * @return A Pane containing the complete sensor visualization
   */
  public static Pane create(String name, double value, String unit) {
    // Clamp value to valid range
    double clampedValue = Math.max(LIGHT_MIN, Math.min(LIGHT_MAX, value));

    // --- 1. Determine Status Color and Text ---
    Color baseColor;
    Color darkColor;
    String statusText;

    if (clampedValue < LIGHT_LOW_THRESHOLD) {
      baseColor = Color.web("#5C6BC0"); // Indigo - Dark
      darkColor = Color.web("#3949AB");
      statusText = "DARK";
    } else if (clampedValue > LIGHT_HIGH_THRESHOLD) {
      baseColor = Color.web("#FFD54F"); // Yellow - Bright
      darkColor = Color.web("#FFA000");
      statusText = "BRIGHT";
    } else {
      baseColor = Color.web("#66BB6A"); // Green - Normal
      darkColor = Color.web("#388E3C");
      statusText = "NORMAL";
    }

    Color textColor = Color.WHITE;
    if (baseColor.equals(Color.web("#FFD54F"))) {
      textColor = Color.web("#333333"); // Dark text for bright background
    }

    // --- 2. Labels & Icon Setup ---
    Label nameLabel = new Label(name);
    nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    nameLabel.setTextFill(Color.web("#333333"));

    Label valueLabel = new Label(String.format("%.0f", clampedValue));
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
          LightSensorView.class.getResourceAsStream("/icons/light_sensor.png")
      );
      icon = new ImageView(image);
      icon.setFitWidth(ICON_SIZE + 25);
      icon.setFitHeight(ICON_SIZE + 25);
      icon.setPreserveRatio(true);
      icon.setEffect(
          new DropShadow(BlurType.GAUSSIAN, darkColor.deriveColor(0, 1.0, 1.0, 0.5), 5, 0, 0, 0));
    } catch (Exception e) {
      System.err.println("⚠️ Could not load light icon: " + e.getMessage());
    }

    // HBox for Icon + Value/Unit
    HBox iconValueBox = new HBox(35);
    iconValueBox.setAlignment(Pos.CENTER_LEFT);
    if (icon != null) {
      iconValueBox.getChildren().add(icon);
    }
    iconValueBox.getChildren().add(valueAndUnitBox);

    // --- 3. Light Spectrum Bar (Full Range Visual) ---

    double darkToNormalStop = LIGHT_LOW_THRESHOLD / LIGHT_MAX; // ~5k/50k = 0.1
    double normalToBrightStop = LIGHT_HIGH_THRESHOLD / LIGHT_MAX; // ~25k/50k = 0.5

    LinearGradient spectrumGradient = new LinearGradient(
        0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
        new Stop(0.0, Color.web("#5C6BC0")),                     // Indigo - DARK start
        new Stop(darkToNormalStop, Color.web("#66BB6A")),         // Green - NORMAL start
        new Stop(normalToBrightStop - 0.05, Color.web("#66BB6A")), // Green end,
        new Stop(normalToBrightStop, Color.web("#DCE775")),        // LIMEx)
        new Stop(normalToBrightStop + 0.05, Color.web("#FFD54F")), // Yellow star

        new Stop(1.0, Color.web("#FFA000"))                      // Amber - BRIGHT end
    );

    // Background bar (the spectrum itself)
    Rectangle spectrumBar = new Rectangle(BAR_WIDTH, BAR_HEIGHT);
    spectrumBar.setArcWidth(BAR_HEIGHT);
    spectrumBar.setArcHeight(BAR_HEIGHT);
    spectrumBar.setFill(spectrumGradient);
    spectrumBar.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.1), 3, 0, 0, 1));

    // Current light indicator (A simple vertical line)
    double positionRatio = clampedValue / LIGHT_MAX;
    double indicatorX = (BAR_WIDTH * positionRatio) - 1; // Center the indicator

    Rectangle indicator = new Rectangle(2,
        BAR_HEIGHT + 4); // Vertical line slightly taller than the bar
    indicator.setFill(Color.BLACK); // Marker color
    indicator.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 5, 0, 0, 0));

    // Container to position the indicator over the bar
    StackPane indicatorStack = new StackPane(indicator);
    indicatorStack.setPrefWidth(BAR_WIDTH);
    indicatorStack.setMaxWidth(BAR_WIDTH);
    indicatorStack.setMinWidth(BAR_WIDTH); // Fix the width for absolute positioning
    StackPane.setAlignment(indicator, Pos.CENTER_LEFT);
    indicator.setTranslateX(indicatorX);

    // --- 3.5 Light Range Scale (New Section) ---
    Label darkLabel = new Label("DARK");
    darkLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
    darkLabel.setTextFill(Color.web("#888888"));

    Label normalLabel = new Label("NORMAL");
    normalLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
    normalLabel.setTextFill(Color.web("#888888"));

    Label brightLabel = new Label("BRIGHT");
    brightLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
    brightLabel.setTextFill(Color.web("#888888"));

    HBox scaleBox = new HBox();
    scaleBox.setPrefWidth(BAR_WIDTH);
    scaleBox.setMaxWidth(BAR_WIDTH);
    scaleBox.setMinWidth(BAR_WIDTH);
    scaleBox.setAlignment(Pos.CENTER_LEFT);
    scaleBox.setPadding(new Insets(2, 0, 0, 0)); // Little padding at the top

    // Add labels with spacers for proportional distribution
    scaleBox.getChildren().add(darkLabel);

    // Spacer to push the next label
    Region darkToNormalSpacer = new Region();
    HBox.setHgrow(darkToNormalSpacer, Priority.ALWAYS);
    scaleBox.getChildren().add(darkToNormalSpacer);

    // Normal label
    scaleBox.getChildren().add(normalLabel);

    // Spacer to push the last label
    Region normalToBrightSpacer = new Region();
    HBox.setHgrow(normalToBrightSpacer, Priority.ALWAYS);
    scaleBox.getChildren().add(normalToBrightSpacer);

    // Bright label at the right
    scaleBox.getChildren().add(brightLabel);

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
    statusBar.setBackground(new Background(
        new BackgroundFill(baseColor, new CornerRadii(8, 8, 0, 0, false), Insets.EMPTY)));

    HBox statusBox = new HBox(statusLabel);
    statusBox.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(statusBox, Priority.ALWAYS);

    // Delete Menu Button
    Circle dot1 = new Circle(2.5, textColor);
    Circle dot2 = new Circle(2.5, textColor);
    Circle dot3 = new Circle(2.5, textColor);
    HBox dots = new HBox(3, dot1, dot2, dot3);
    dots.setAlignment(Pos.CENTER);

    Button menuButton = new Button();
    menuButton.setGraphic(dots);
    menuButton.setStyle(
        "-fx-background-color: transparent; -fx-padding: 5 10 5 10; -fx-cursor: hand;");

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

    layout.setBackground(
        new Background(new BackgroundFill(Color.WHITE, new CornerRadii(8), Insets.EMPTY)));
    layout.setBorder(new Border(
        new BorderStroke(Color.web("#EEEEEE"), BorderStrokeStyle.SOLID, new CornerRadii(8),
            new BorderWidths(1))));
    layout.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.1), 10, 0, 0, 5));
    layout.setPrefWidth(CARD_WIDTH);
    layout.setMaxWidth(CARD_WIDTH);

    return layout;
  }

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private LightSensorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}