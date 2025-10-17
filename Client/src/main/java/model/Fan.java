package model;

import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

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
  private RotateTransition rotateTransition;
  private Label statusLabel;
  private SVGPath fanIcon;

  public Fan() {
    this.isOn = false;
    this.currentSpeed = Speed.OFF;
  }


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

  @Override
  public Pane getVisualRepresentation() {
    // --- Icon ---
    fanIcon = new SVGPath();
    fanIcon.setContent("M12,11a1,1,0,1,0,1,1,1,1,0,0,0-1-1m.5-9C17,2,17.1,5.57,14.73,6.75a3.36,3.36,0,0,0-1.62,2.47,3.17,3.17,0,0,1,1.23.91C18,8.13,22,8.92,22,12.5c0,4.5-3.58,4.6-4.75,2.23a3.44,3.44,0,0,0-2.5-1.62,3.24,3.24,0,0,1-.91,1.23c2,3.69,1.2,7.66-2.38,7.66C7,22,6.89,18.42,9.26,17.24a3.46,3.46,0,0,0,1.62-2.45,3,3,0,0,1-1.25-.92C5.94,15.85,2,15.07,2,11.5,2,7,5.54,6.89,6.72,9.26A3.39,3.39,0,0,0,9.2,10.87a2.91,2.91,0,0,1,.92-1.22C8.13,6,8.92,2,12.48,2Z");
    fanIcon.setScaleX(1.2);
    fanIcon.setScaleY(1.2);


    rotateTransition = new RotateTransition(Duration.millis(1000), fanIcon);
    rotateTransition.setByAngle(360);
    rotateTransition.setCycleCount(RotateTransition.INDEFINITE);

    // --- Labels ---
    Label nameLabel = new Label(getActuatorName());
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

    statusLabel = new Label();
    statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));

    updateUI();

    VBox titleBox = new VBox(-2, nameLabel, statusLabel);
    HBox topPane = new HBox(10, fanIcon, titleBox);
    topPane.setPadding(new Insets(0, 0, 10, 0));

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
    Duration rotationDuration;

    if (currentSpeed == Speed.OFF) {
      statusColor = Color.web("#B0BEC5");
      rotationDuration = Duration.ZERO;

      if (rotateTransition != null) {
        rotateTransition.stop();
      }
      if (fanIcon != null) {
        fanIcon.setRotate(0);
      }

    } else {

      if (currentSpeed == Speed.LOW) {
        statusColor = Color.web("#4CAF50");
        rotationDuration = Duration.millis(3000);
      } else if (currentSpeed == Speed.MEDIUM) {
        statusColor = Color.web("#FFD54F");
        rotationDuration = Duration.millis(1000);
      } else { // HIGH
        statusColor = Color.web("#F44336");
        rotationDuration = Duration.millis(200);
      }

      if (rotateTransition != null) {
        rotateTransition.setDuration(rotationDuration);
        if (rotateTransition.getStatus() != RotateTransition.Status.RUNNING) {
          rotateTransition.play();
        }
      }
    }

    fanIcon.setFill(statusColor);

    String statusText = isOn ? "ON - " + currentSpeed.toString() : "OFF";
    statusLabel.setText(statusText);
    statusLabel.setTextFill(statusColor);
  }
}