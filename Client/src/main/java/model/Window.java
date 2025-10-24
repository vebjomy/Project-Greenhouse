package model;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


/* This class represents a Window actuator in a smart home system.
 * It allows setting the window openness level and provides a visual representation.
 */
public class Window implements Actuator {

  // --- Enum for Window Openness Level (й) ---
  public enum Openness {
    CLOSED(0, "CLOSED"),
    HALF(1, "HALF OPEN"),
    OPEN(2, "OPEN");

    private final int level;
    private final String label;

    Openness(int level, String label) {
      this.level = level;
      this.label = label;
    }

    public int getLevel() {
      return level;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  /* Variables to track current openness level */

  // --- SVG Icon Definitions ---
  private static final String SVG_CLOSED = "M3,21V19H21V21H3M12,17V5H21V17H12M3,17V5H12V17H3Z";
  private static final String SVG_HALF = "M3,21V19H21V21H3M12,17V5H21V17M3,17V5H12V17H3Z";
  private static final String SVG_OPEN = "M3,21V19H21V21H3M3,17V5H21V17H3M12,5V17Z"; //

  private Openness currentOpenness;

  public Window() {
    this.currentOpenness = Openness.CLOSED;
  }

  // --- Control Methods () ---

  public void open() {
    setOpenness(Openness.OPEN);
  }

  public void close() {
    setOpenness(Openness.CLOSED);
  }

  /* Set the window openness level */

  public void setOpenness(Openness newOpenness) {
    this.currentOpenness = newOpenness;
    System.out.println("Window set to: " + this.currentOpenness.toString());
  }

  // --- Actuator Interface Methods ---

  @Override
  public String getActuatorName() {
    return "Window Vent";
  }

  private Label statusLabel;
  private SVGPath windowIcon;

  @Override
  public Pane getVisualRepresentation() {
    // --- Icon ---

    windowIcon = new SVGPath();
    windowIcon.setScaleX(1.2);
    windowIcon.setScaleY(1.2);


    Label nameLabel = new Label(getActuatorName());
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

    statusLabel = new Label();
    statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));

    updateUI();

    VBox titleBox = new VBox(-2, nameLabel, statusLabel);
    HBox topPane = new HBox(10, windowIcon, titleBox);
    topPane.setPadding(new Insets(0, 0, 10, 0));


    Button closeButton = new Button("CLOSED");
    Button halfButton = new Button("HALF");
    Button openButton = new Button("OPEN");

    // Set Actions
    closeButton.setOnAction(e -> {
      setOpenness(Openness.CLOSED);
      updateUI();
    });

    halfButton.setOnAction(e -> {
      setOpenness(Openness.HALF);
      updateUI();
    });

    openButton.setOnAction(e -> {
      setOpenness(Openness.OPEN);
      updateUI();
    });

    HBox controlsPane = new HBox(5, closeButton, halfButton, openButton);
    controlsPane.setAlignment(Pos.CENTER);

    VBox layout = new VBox(10, topPane, controlsPane);
    layout.setPadding(new Insets(10));
    layout.setMinWidth(250);

    return layout;
  }

  /* Update the visual representation based on the current openness level */

  private void updateUI() {
    Color statusColor;
    String svgContent;

    // Logic based on the openness level
    if (currentOpenness == Openness.CLOSED) {
      statusColor = Color.web("#B0BEC5"); // Gray/Off
      svgContent = SVG_CLOSED;
    } else if (currentOpenness == Openness.HALF) {
      statusColor = Color.web("#FFD54F"); // Yellow/Medium
      svgContent = SVG_HALF;
    } else if (currentOpenness == Openness.OPEN) {
      statusColor = Color.web("#81C784"); // Green/Fully On
      svgContent = SVG_OPEN;
    } else {
      statusColor = Color.web("#B0BEC5");
      svgContent = SVG_CLOSED;
    }


    windowIcon.setContent(svgContent);
    windowIcon.setFill(statusColor);

    String statusText = currentOpenness.toString();
    statusLabel.setText(statusText);
    statusLabel.setTextFill(statusColor);
  }
}