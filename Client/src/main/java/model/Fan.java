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

public class Fan implements Actuator {

  // --- Enum for Fan Speed ---
  public enum Speed {
    OFF(0, "OFF"),
    LOW(1, "LOW"),
    MEDIUM(2, "MEDIUM"),
    HIGH(3, "HIGH");

    private final int level;
    private final String label;

    Speed(int level, String label) {
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

  private Speed currentSpeed;
  private boolean isOn;

  public Fan() {
    this.isOn = false;
    this.currentSpeed = Speed.OFF;
  }

  // --- Control Methods ---

  public void turnOn() {
    if (!this.isOn) {
      this.isOn = true;
      // Set a default speed when turned on, e.g., LOW
      if (this.currentSpeed == Speed.OFF) {
        this.currentSpeed = Speed.LOW;
      }
      System.out.println("Fan turned ON. Speed: " + this.currentSpeed);
    }
  }

  public void turnOff() {
    this.isOn = false;
    this.currentSpeed = Speed.OFF;
    System.out.println("Fan turned OFF.");
  }

  public void setSpeed(Speed newSpeed) {
    if (newSpeed == Speed.OFF) {
      turnOff();
    } else {
      turnOn(); // Ensure it's on if speed is set
      this.currentSpeed = newSpeed;
      System.out.println("Fan speed set to: " + this.currentSpeed);
    }
  }

  // --- Actuator Interface Methods ---

  @Override
  public String getActuatorName() {
    return "Fan";
  }
  private Label statusLabel;
  private SVGPath fanIcon;

  @Override
  public Pane getVisualRepresentation() {
    // --- Icon ---
    fanIcon = new SVGPath();
    fanIcon.setContent("M12,2A10,10 0 0,0 2,12C2,17.25 5.82,21.56 11,21.93V17.93C7.94,17.47 5.5,14.97 5.5,12A6.5,6.5 0 0,1 12,5.5A6.5,6.5 0 0,1 18.5,12C18.5,14.97 16.06,17.47 13,17.93V21.93C18.18,21.56 22,17.25 22,12A10,10 0 0,0 12,2M13.25,9V14.5L18.66,12.35L13.25,9Z");
    fanIcon.setScaleX(1.2);
    fanIcon.setScaleY(1.2);

    // --- Labels ---
    Label nameLabel = new Label(getActuatorName());
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

    statusLabel = new Label();
    statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));

    updateUI();

    VBox titleBox = new VBox(-2, nameLabel, statusLabel);
    HBox topPane = new HBox(10, fanIcon, titleBox);
    topPane.setPadding(new Insets(0, 0, 10, 0));

    // --- Control Buttons ---
    Button offButton = new Button("OFF");
    Button lowButton = new Button("LOW");
    Button mediumButton = new Button("MED");
    Button highButton = new Button("HIGH");

    offButton.setOnAction(e -> {
      setSpeed(Speed.OFF);
      updateUI();
    });

    lowButton.setOnAction(e -> {
      setSpeed(Speed.LOW);
      updateUI();
    });

    mediumButton.setOnAction(e -> {
      setSpeed(Speed.MEDIUM);
      updateUI();
    });

    highButton.setOnAction(e -> {
      setSpeed(Speed.HIGH);
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

    if (currentSpeed == Speed.OFF) {
      statusColor = Color.web("#B0BEC5");
    } else if (currentSpeed == Speed.LOW) {
      statusColor = Color.web("#4CAF50");
    } else if (currentSpeed == Speed.MEDIUM) {
      statusColor = Color.web("#FFD54F");
    } else if (currentSpeed == Speed.HIGH) {
      statusColor = Color.web("#F44336");
    } else {
      statusColor = Color.web("#B0BEC5");
    }

    fanIcon.setFill(statusColor);

    String statusText = isOn ? "ON - " + currentSpeed.toString() : "OFF";
    statusLabel.setText(statusText);
    statusLabel.setTextFill(statusColor);
  }
  }

