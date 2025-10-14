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

public class CO2Generator implements Actuator {

  // --- Enum for CO2 Generation Rate ---
  public enum Rate {
    OFF(0, "OFF"),
    LOW(1, "LOW"),
    MEDIUM(2, "MEDIUM"),
    HIGH(3, "HIGH");

    private final int level;
    private final String label;

    Rate(int level, String label) {
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

  private Rate currentRate;
  private boolean isRunning;

  public CO2Generator() {
    this.isRunning = false;
    this.currentRate = Rate.OFF;
  }

  // --- Control Methods ---

  public void turnOn() {
    if (!this.isRunning) {
      this.isRunning = true;
      // Set a default rate when turned on, e.g., LOW
      if (this.currentRate == Rate.OFF) {
        this.currentRate = Rate.LOW;
      }
      System.out.println("CO2 Generator turned ON. Rate: " + this.currentRate);
    }
  }

  public void turnOff() {
    this.isRunning = false;
    this.currentRate = Rate.OFF;
    System.out.println("CO2 Generator turned OFF.");
  }

  public void setRate(Rate newRate) {
    if (newRate == Rate.OFF) {
      turnOff();
    } else {
      turnOn(); // Ensure it's running if a rate is set
      this.currentRate = newRate;
      System.out.println("CO2 Generator rate set to: " + this.currentRate);
    }
  }

  // --- Actuator Interface Methods ---

  @Override
  public String getActuatorName() {
    return "CO2 Generator";
  }

  private Label statusLabel;
  private SVGPath co2Icon;

  @Override
  public Pane getVisualRepresentation() {
    // --- Icon ---
    // CO2 Icon (Gas Cylinder): M18,20H6V18H18V20M14,2V5C14,6.66 12.66,8 11,8C9.34,8 8,6.66 8,5V2H6V5C6,7.76 8.24,10 11,10C13.76,10 16,7.76 16,5V2H14M11,12C7.31,12 4,15.14 4,19H18C18,15.14 14.69,12 11,12Z
    co2Icon = new SVGPath();
    co2Icon.setContent("M18,20H6V18H18V20M14,2V5C14,6.66 12.66,8 11,8C9.34,8 8,6.66 8,5V2H6V5C6,7.76 8.24,10 11,10C13.76,10 16,7.76 16,5V2H14M11,12C7.31,12 4,15.14 4,19H18C18,15.14 14.69,12 11,12Z");
    co2Icon.setScaleX(1.2);
    co2Icon.setScaleY(1.2);

    // --- Labels ---
    Label nameLabel = new Label(getActuatorName());
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

    statusLabel = new Label();
    statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));

    updateUI();

    VBox titleBox = new VBox(-2, nameLabel, statusLabel);
    HBox topPane = new HBox(10, co2Icon, titleBox);
    topPane.setPadding(new Insets(0, 0, 10, 0));

    // --- Control Buttons ---
    Button offButton = new Button("OFF");
    Button lowButton = new Button("LOW");
    Button mediumButton = new Button("MED");
    Button highButton = new Button("HIGH");

    // Set Actions
    offButton.setOnAction(e -> {
      setRate(Rate.OFF);
      updateUI();
    });

    lowButton.setOnAction(e -> {
      setRate(Rate.LOW);
      updateUI();
    });

    mediumButton.setOnAction(e -> {
      setRate(Rate.MEDIUM);
      updateUI();
    });

    highButton.setOnAction(e -> {
      setRate(Rate.HIGH);
      updateUI();
    });

    HBox controlsPane = new HBox(5, offButton, lowButton, mediumButton, highButton);
    controlsPane.setAlignment(Pos.CENTER);

    VBox layout = new VBox(10, topPane, controlsPane);
    layout.setPadding(new Insets(10));
    layout.setMinWidth(250);

    return layout;
  }

  private void updateUI() {
    Color statusColor;
    String statusText;

    // Color logic based on activity rate
    if (currentRate == Rate.OFF) {
      statusColor = Color.web("#B0BEC5"); // Gray/Idle
      statusText = "OFF";
    } else if (currentRate == Rate.LOW) {
      statusColor = Color.web("#81C784"); // Green/Low activity
      statusText = "ON - " + currentRate.toString();
    } else if (currentRate == Rate.MEDIUM) {
      statusColor = Color.web("#FFD54F"); // Yellow/Medium activity
      statusText = "ON - " + currentRate.toString();
    } else if (currentRate == Rate.HIGH) {
      statusColor = Color.web("#F44336"); // Red/High activity
      statusText = "ON - " + currentRate.toString();
    } else {
      statusColor = Color.web("#B0BEC5");
      statusText = "OFF";
    }

    co2Icon.setFill(statusColor);
    statusLabel.setText(statusText);
    statusLabel.setTextFill(statusColor);
  }
}