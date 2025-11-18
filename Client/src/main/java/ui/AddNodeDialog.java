package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

/**
 * A dialog for creating a new node, including its name, location, IP, and initial components.
 */
public class AddNodeDialog extends Dialog<AddNodeDialog.NodeCreationResult> {

  private final TextField nameField = new TextField();
  private final TextField locationField = new TextField();
  private final TextField ipField = new TextField();
  private final List<String> selectedComponents = new ArrayList<>();

  /**
   * Data class to hold the result of the node creation dialog.
   */
  public static class NodeCreationResult {

    public final String name;
    public final String location;
    public final String ip;
    public final List<String> components;

    /**
     * Constructs a NodeCreationResult.
     */
    public NodeCreationResult(String name, String location, String ip, List<String> components) {
      this.name = name;
      this.location = location;
      this.ip = ip;
      this.components = components;
    }
  }

  /**
   * Constructs the AddNodeDialog with input fields and component selection.
   */
  public AddNodeDialog() {
    setTitle("Create New Farm Node");
    setHeaderText("Enter the details for the new node.");

    VBox mainLayout = new VBox(25);
    mainLayout.setPadding(new Insets(30, 25, 20, 25));

    // Form grid
    GridPane grid = new GridPane();
    grid.setHgap(20);
    grid.setVgap(20);
    grid.getStyleClass().add("material-form-grid");

    // Node Name
    grid.add(new Label("Node Name:"), 0, 0);
    grid.add(nameField, 1, 0);
    nameField.setPromptText("e.g., Greenhouse A-1");

    // Location
    grid.add(new Label("Location:"), 0, 1);
    grid.add(locationField, 1, 1);
    locationField.setPromptText("e.g., North-West Corner");

    // IP Address with validation
    grid.add(new Label("IP Address:"), 0, 2);
    grid.add(ipField, 1, 2);
    ipField.setPromptText("e.g., 192.168.1.50");

    // Auto-generate IP button
    Button autoIpButton = new Button("Auto-generate");
    autoIpButton.setOnAction(e -> {
      // Generate next available IP
      ipField.setText(generateNextIp());
    });
    grid.add(autoIpButton, 2, 2);

    // Component selection
    Label componentLabel = new Label("Select initial components:");
    componentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 1.1em;");

    TilePane tilePane = new TilePane();
    tilePane.setPadding(new Insets(10, 0, 10, 0));
    tilePane.setHgap(25);
    tilePane.setVgap(25);
    tilePane.setPrefColumns(4);
    tilePane.getChildren().addAll(
        createComponentToggle("Temperature Sensor", "/icons/temp_sensor.png"),
        createComponentToggle("Light Sensor", "/icons/light_sensor.png"),
        createComponentToggle("Humidity Sensor", "/icons/humidity_sensor.png"),
        createComponentToggle("PH Sensor", "/icons/Ph.png"),
        createComponentToggle("Fan", "/icons/fan.png"),
        createComponentToggle("Water Pump", "/icons/waterpump.png"),
        createComponentToggle("Window", "/icons/window.png"),
        createComponentToggle("CO2 Generator", "/icons/ceo2.png")
    );

    ScrollPane scrollPane = new ScrollPane(tilePane);
    scrollPane.setFitToWidth(true);
    scrollPane.setPrefHeight(250);
    scrollPane.getStyleClass().add("material-scroll-pane");

    mainLayout.getChildren().addAll(grid, new Separator(), componentLabel, scrollPane);

    getDialogPane().setContent(mainLayout);
    getDialogPane().getStyleClass().add("material-dialog-pane");
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    okButton.getStyleClass().addAll("dialog-button", "primary");
    getDialogPane().lookupButton(ButtonType.CANCEL).getStyleClass().add("dialog-button");

    // Validation: name and IP required
    okButton.disableProperty().bind(
        nameField.textProperty().isEmpty()
            .or(ipField.textProperty().isEmpty())
    );

    getDialogPane().getStylesheets().add(
        Objects.requireNonNull(getClass().getResource("/test.css")).toExternalForm()
    );

    setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        String ip = ipField.getText().trim();
        if (!isValidIp(ip)) {
          showError("Invalid IP address format. Please use format: 192.168.1.X");
          return null;
        }

        return new NodeCreationResult(
            nameField.getText().trim(),
            locationField.getText().trim(),
            ip,
            new ArrayList<>(selectedComponents)
        );
      }
      return null;
    });
  }

  /**
   * Creates a ToggleButton with an icon and a label.
   */
  private ToggleButton createComponentToggle(String name, String iconPath) {
    ImageView icon = new ImageView();
    icon.setFitWidth(40);
    icon.setFitHeight(40);

    try {
      Image image = new Image(Objects.requireNonNull(
          getClass().getResourceAsStream(iconPath)), 40, 40, true, true);
      icon.setImage(image);
    } catch (Exception e) {
      System.err.println("Could not load icon: " + iconPath);
    }

    Label label = new Label(name);
    label.getStyleClass().add("component-label");

    VBox content = new VBox(5, icon, label);
    content.setAlignment(Pos.CENTER);

    ToggleButton button = new ToggleButton();
    button.setGraphic(content);
    button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    button.setMinWidth(90);
    button.setMinHeight(90);
    button.setMaxWidth(90);
    button.setMaxHeight(90);
    button.getStyleClass().add("component-toggle-button");

    button.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
      if (isSelected) {
        if (!selectedComponents.contains(name)) {
          selectedComponents.add(name);
        }
        button.getStyleClass().add("selected");
      } else {
        selectedComponents.remove(name);
        button.getStyleClass().remove("selected");
      }
    });

    return button;
  }

  /**
   * Simple IP counter for auto-generation.
   */
  private static int ipCounter = 50;

  private String generateNextIp() {
    String ip = "192.168.1." + ipCounter;
    ipCounter++;
    if (ipCounter > 254) {
      ipCounter = 50; // Wrap around
    }
    return ip;
  }

  /**
   * Validate IP address format.
   */
  private boolean isValidIp(String ip) {
    if (ip == null || ip.isEmpty()) {
      return false;
    }

    String[] parts = ip.split("\\.");
    if (parts.length != 4) {
      return false;
    }

    try {
      for (String part : parts) {
        int value = Integer.parseInt(part);
        if (value < 0 || value > 255) {
          return false;
        }
      }
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Show error alert.
   */
  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Validation Error");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}