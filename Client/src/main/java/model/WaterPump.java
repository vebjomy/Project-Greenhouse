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
import java.text.DecimalFormat;


/* this class represents a Water Pump actuator in a smart irrigation system.
 * It allows setting dosing volumes and simulates the pump operation.
 */

public class WaterPump implements Actuator {

  // --- Enum for Dosing Volume ---
  public enum Volume {
    OFF(0.0, "OFF"),
    LOW(0.5, "LOW (0.5 L)"),
    MEDIUM(1.5, "MEDIUM (1.5 L)"),
    HIGH(3.0, "HIGH (3.0 L)");

    private final double liters;
    private final String label;

    /* Constructor for Volume enum */

    Volume(double liters, String label) {
      this.liters = liters;
      this.label = label;
    }

    /* Getter for liters */

    public double getLiters() {
      return liters;
    }

    @Override
    public String toString() {
      return label.split(" ")[0];
    }

    public String getDisplayLabel() {
      return label;
    }
  }

  /* Variables to track current volume and running state */

  private Volume currentVolume;
  private boolean isRunning;
  private static final DecimalFormat df = new DecimalFormat("0.0");


  public WaterPump() {
    this.isRunning = false;
    this.currentVolume = Volume.OFF;
  }

  // --- Control Methods ---

  /**
   * Simulates the pump running to dispense the set volume.
   * In a real system, this would trigger the physical mechanism.
   */
  public void startDosing() {
    if (this.currentVolume != Volume.OFF) {
      this.isRunning = true;
      double volume = this.currentVolume.getLiters();
      System.out.println("Water Pump activated. Dosing " + df.format(volume) + " L.");

      // Simulate the operation finished immediately for UI purposes
      // In a real app, this might be a delayed event.
      this.isRunning = false;
    } else {
      System.out.println("Water Pump is OFF. Set a volume to start dosing.");
    }
  }

  /* Setter for volume */
  public void setVolume(Volume newVolume) {
    this.currentVolume = newVolume;

    if (newVolume == Volume.OFF) {
      this.isRunning = false;
      System.out.println("Water Pump is idle (OFF).");
    } else {
      System.out.println("Water Pump volume set to: " + df.format(newVolume.getLiters()) + " L.");
    }
  }

  // --- Actuator Interface Methods ---

  @Override
  public String getActuatorName() {
    return "Water Pump";
  }

  private Label statusLabel;
  private SVGPath pumpIcon;


  /* Method to create the visual representation of the Water Pump actuator */
  @Override
  public Pane getVisualRepresentation() {
    // --- Icon ---
    // M12,14L10,12L12,10V11.5H16V12.5H12V14M10.88,2C10.5,2 10.12,2.07 9.75,2.21C6.9,3.31 5,6.17 5,9.5C5,14.07 9.17,17.7 13.5,17.96V20.5C13.5,20.84 13.23,21.11 12.9,21.11H11.1C10.77,21.11 10.5,20.84 10.5,20.5V19.89C6.88,19.36 4,16.03 4,12C4,8.22 6.13,5.03 9.25,3.44V3.5C9.25,3.83 9.52,4.1 9.85,4.1H11.35C11.68,4.1 11.95,3.83 11.95,3.5V2.42C12.07,2.28 12.19,2.14 12.33,2Z
    pumpIcon = new SVGPath();
    pumpIcon.setContent("M12,14L10,12L12,10V11.5H16V12.5H12V14M10.88,2C10.5,2 10.12,2.07 9.75,2.21C6.9,3.31" +
        " 5,6.17 5,9.5C5,14.07 9.17,17.7 13.5,17.96V20.5C13.5,20.84 13.23,21.11 12.9,21.11H11.1C10.77,21.11 " +
        "10.5,20.84 10.5,20.5V19.89C6.88,19.36 4,16.03 4,12C4,8.22 6.13,5.03 9.25,3.44V3.5C9.25,3.83 9.52,4.1 " +
        "9.85,4.1H11.35C11.68,4.1 11.95,3.83 11.95,3.5V2.42C12.07,2.28 12.19,2.14 12.33,2Z");
    pumpIcon.setScaleX(1.2);
    pumpIcon.setScaleY(1.2);

    // --- Labels ---
    Label nameLabel = new Label(getActuatorName());
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

    statusLabel = new Label();
    statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));

    updateUI();

    VBox titleBox = new VBox(-2, nameLabel, statusLabel);
    HBox topPane = new HBox(10, pumpIcon, titleBox);
    topPane.setPadding(new Insets(0, 0, 10, 0));

    // --- Control Buttons (Volume Select) ---
    Button offButton = new Button("IDLE");
    Button lowVolumeButton = new Button("LOW");
    Button mediumVolumeButton = new Button("MED");
    Button highVolumeButton = new Button("HIGH");

    /* Main Action Button
    * This button starts the dosing operation based on the selected volume.
    * */
    Button startDosingButton = new Button("START DOSE");
    startDosingButton.getStyleClass().add("action-button"); // For potential CSS styling

    // Set Actions
    offButton.setOnAction(e -> {
      setVolume(Volume.OFF);
      updateUI();
    });

    lowVolumeButton.setOnAction(e -> {
      setVolume(Volume.LOW);
      updateUI();
    });

    mediumVolumeButton.setOnAction(e -> {
      setVolume(Volume.MEDIUM);
      updateUI();
    });

    highVolumeButton.setOnAction(e -> {
      setVolume(Volume.HIGH);
      updateUI();
    });

    startDosingButton.setOnAction(e -> {
      startDosing();
      updateUI(); // Update UI after dosing (status becomes idle again)
    });


    HBox volumeControls = new HBox(5, lowVolumeButton, mediumVolumeButton, highVolumeButton);
    volumeControls.setAlignment(Pos.CENTER);

    // Layout for controls: Volume selection and the main action button
    VBox controlsLayout = new VBox(10, volumeControls, offButton, startDosingButton);
    controlsLayout.setAlignment(Pos.CENTER);


    VBox layout = new VBox(10, topPane, controlsLayout);
    layout.setPadding(new Insets(10));
    layout.setMinWidth(250);

    return layout;
  }

  /* Method to update the UI elements based on current state */

  private void updateUI() {
    Color statusColor;
    String statusText;

    // Status text logic
    if (isRunning) {
      statusColor = Color.web("#F44336"); // Red/Active color
      statusText = "DOSING " + df.format(currentVolume.getLiters()) + " L...";
    } else if (currentVolume == Volume.OFF) {
      statusColor = Color.web("#B0BEC5"); // Gray/Idle color
      statusText = "IDLE";
    } else {
      // Ready to dose
      statusColor = Color.web("#81C784"); // Green/Ready color
      statusText = "READY - " + df.format(currentVolume.getLiters()) + " L";
    }
    pumpIcon.setFill(statusColor);
    statusLabel.setText(statusText);
    statusLabel.setTextFill(statusColor);
  }
}