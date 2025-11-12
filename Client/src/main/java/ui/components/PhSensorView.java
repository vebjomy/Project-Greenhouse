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
 * Visual component for displaying pH sensor readings in a modern card style
 * with a horizontal pH spectrum bar and a context menu button for deletion.
 *
 * @author Green House Control Team
 * @version 3.2 (Added pH Range Scale below spectrum bar)
 */
public class PhSensorView {

  // pH thresholds for color coding (retained)
  private static final double PH_ACIDIC_THRESHOLD = 6.0;
  private static final double PH_ALKALINE_THRESHOLD = 7.5;
  private static final double PH_MIN = 0.0;
  private static final double PH_MAX = 14.0;

  // Visual constants
  private static final double CARD_WIDTH = 250.0;
  private static final double ICON_SIZE = 24.0;
  private static final double BAR_WIDTH = 210.0; // Wider bar for spectrum
  private static final double BAR_HEIGHT = 12.0;

  /**
   * Creates a visual representation of a pH sensor in a modern card style.
   *
   * @param name Display name for the sensor (e.g., "pH Level")
   * @param value Current pH value (0-14)
   * @param unit Unit string to display (e.g., "pH")
   * @return A Pane containing the complete sensor visualization
   */
  public static Pane create(String name, double value, String unit) {
    // Clamp value to valid range
    double clampedValue = Math.max(PH_MIN, Math.min(PH_MAX, value));

    // --- 1. Determine Status Color and Text ---
    Color baseColor;
    Color darkColor;
    String statusText;

    if (clampedValue < PH_ACIDIC_THRESHOLD) {
      baseColor = Color.web("#FF7043"); // Deep Orange - Acidic
      darkColor = Color.web("#E64A19");
      statusText = "ACIDIC";
    } else if (clampedValue > PH_ALKALINE_THRESHOLD) {
      baseColor = Color.web("#7E57C2"); // Purple - Alkaline
      darkColor = Color.web("#512DA8");
      statusText = "ALKALINE";
    } else {
      baseColor = Color.web("#b7d667"); // yellow - Neutral
      darkColor = Color.web("#388E3C");
      statusText = "NEUTRAL";
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
          PhSensorView.class.getResourceAsStream("/icons/Ph.png")
      );
      icon = new ImageView(image);
      icon.setFitWidth(ICON_SIZE+25);
      icon.setFitHeight(ICON_SIZE+25);
      icon.setPreserveRatio(true);
      icon.setEffect(new DropShadow(BlurType.GAUSSIAN, darkColor.deriveColor(0, 1.0, 1.0, 0.5), 5, 0, 0, 0));
    } catch (Exception e) {
      System.err.println("⚠️ Could not load pH icon: " + e.getMessage());
    }

    // HBox for Icon + Value/Unit
    HBox iconValueBox = new HBox(35);
    iconValueBox.setAlignment(Pos.CENTER_LEFT);
    if (icon != null) {
      iconValueBox.getChildren().add(icon);
    }
    iconValueBox.getChildren().add(valueAndUnitBox);


    // --- 3. pH Spectrum Bar (Full Range Visual) ---

    // Define the pH gradient spectrum (Acidic=0, Neutral=7, Alkaline=14)
    LinearGradient spectrumGradient = new LinearGradient(
        0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
        new Stop(0.0, Color.web("#E64A19")),   // Acidic (pH 0)
        new Stop(PH_ACIDIC_THRESHOLD / PH_MAX, Color.web("#FFC107")), // Yellowish transition
        new Stop(7.0 / PH_MAX, Color.web("#4CAF50")), // Neutral (pH 7)
        new Stop(PH_ALKALINE_THRESHOLD / PH_MAX, Color.web("#4FC3F7")),// Bluish transition
        new Stop(1.0, Color.web("#512DA8"))    // Alkaline (pH 14)
    );

    // Background bar (the spectrum itself)
    Rectangle spectrumBar = new Rectangle(BAR_WIDTH, BAR_HEIGHT);
    spectrumBar.setArcWidth(BAR_HEIGHT);
    spectrumBar.setArcHeight(BAR_HEIGHT);
    spectrumBar.setFill(spectrumGradient);
    spectrumBar.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.1), 3, 0, 0, 1));

    // Current pH indicator (A simple vertical line or small circle)
    double positionRatio = clampedValue / PH_MAX;
    double indicatorX = (BAR_WIDTH * positionRatio) - (BAR_HEIGHT / 2); // Center the indicator

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

    // --- 3.5 pH Range Scale (New Section) ---
    Label acidicLabel = new Label("Acidic");
    acidicLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
    acidicLabel.setTextFill(Color.web("#888888"));

    Label neutralLabel = new Label("Neutral");
    neutralLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
    neutralLabel.setTextFill(Color.web("#888888"));

    Label alkalineLabel = new Label("Alkaline");
    alkalineLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
    alkalineLabel.setTextFill(Color.web("#888888"));

    HBox scaleBox = new HBox();
    scaleBox.setPrefWidth(BAR_WIDTH);
    scaleBox.setMaxWidth(BAR_WIDTH);
    scaleBox.setMinWidth(BAR_WIDTH);
    scaleBox.setAlignment(Pos.CENTER_LEFT);
    scaleBox.setPadding(new Insets(2, 0, 0, 0)); // Little padding at the top

    // Calculate approximate widths/spacers for alignment
    // This is a simple approximation. For perfect alignment, you might need a Grid/TilePane
    // or more complex calculation based on label widths.
    double barCenter = BAR_WIDTH / 2.0;

    // Width for Acidic/Neutral
    double acidicWidth = BAR_WIDTH * (PH_ACIDIC_THRESHOLD / PH_MAX); // 6.0/14.0 * 210
    double neutralWidth = BAR_WIDTH * ((PH_ALKALINE_THRESHOLD - PH_ACIDIC_THRESHOLD) / PH_MAX); // (7.5-6.0)/14.0 * 210
    double alkalineWidth = BAR_WIDTH * ((PH_MAX - PH_ALKALINE_THRESHOLD) / PH_MAX); // (14-7.5)/14.0 * 210

    // Spacer before neutral label (to center it around pH 7.0/7.5)
    double spacer1Width = acidicWidth - (acidicLabel.getWidth() / 2.0) - 20; // Estimate positioning
    if (spacer1Width < 0) spacer1Width = 0;

    Region spacer1 = new Region();
    HBox.setHgrow(spacer1, Priority.ALWAYS);

    Region spacer2 = new Region();
    HBox.setHgrow(spacer2, Priority.ALWAYS);


    // A simpler way: use HBox with spacing/grow and center text positions
    // Acidic label at the left
    scaleBox.getChildren().add(acidicLabel);

    // Spacer to push the next label closer to the center (Neutral area)
    Region acidToNeutralSpacer = new Region();
    HBox.setHgrow(acidToNeutralSpacer, Priority.ALWAYS);
    scaleBox.getChildren().add(acidToNeutralSpacer);

    // Neutral label near the center
    scaleBox.getChildren().add(neutralLabel);

    // Spacer to push the last label to the right (Alkaline area)
    Region neutralToAlkalineSpacer = new Region();
    HBox.setHgrow(neutralToAlkalineSpacer, Priority.ALWAYS);
    scaleBox.getChildren().add(neutralToAlkalineSpacer);

    // Alkaline label at the right
    scaleBox.getChildren().add(alkalineLabel);

    // Force a better visual alignment (optional: uncomment for a more proportional layout)
    /*
    scaleBox.getChildren().clear();
    scaleBox.getChildren().addAll(acidicLabel, spacer1, neutralLabel, spacer2, alkalineLabel);
    HBox.setMargin(acidicLabel, new Insets(0, 0, 0, 0)); // Start at 0
    HBox.setMargin(neutralLabel, new Insets(0, 0, 0, (BAR_WIDTH * (7.0/PH_MAX)) - (acidicLabel.getWidth()/2) - (neutralLabel.getWidth()/2) - 10)); // Rough center alignment
    HBox.setMargin(alkalineLabel, new Insets(0, 0, 0, BAR_WIDTH - (BAR_WIDTH * (7.5/PH_MAX)) - alkalineLabel.getWidth())); // Rough end alignment
    */

    // Final Bar Assembly
    VBox barAssembly = new VBox(
        spectrumBar,
        indicatorStack,
        scaleBox // ADDED the scale box
    );
    barAssembly.setSpacing(5); // Overlap the indicator line onto the bar, slightly adjusting
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
  private PhSensorView() {
    throw new UnsupportedOperationException("Utility class");
  }
}