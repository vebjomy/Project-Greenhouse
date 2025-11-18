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
import model.Node;

/**
 * Dialog for editing an existing greenhouse node. Allows updating name, location, IP, and
 * components (sensors/actuators).
 *
 * <p>FIXES:
 * <ul>
 * <li>PH Sensor now properly pre-selects
 * <li>Component deselection now works correctly
 * <li>Server format conversion is accurate
 * </ul>
 *
 * @author Green House Control Team
 * @version 1.1
 */
public class EditNodeDialog extends Dialog<EditNodeDialog.NodeEditResult> {

  private final TextField nameField = new TextField();
  private final TextField locationField = new TextField();
  private final TextField ipField = new TextField();
  private final List<String> selectedComponents = new ArrayList<>();
  private final Node originalNode;

  /**
   * Data class to hold the result of node editing.
   */
  public static class NodeEditResult {

    public final String name;
    public final String location;
    public final String ip;
    public final List<String> components;

    /**
     * Constructor.
     *
     * @param name       The name of the node
     * @param location   The location of the node
     * @param ip         The IP address of the node
     * @param components The list of components
     */
    public NodeEditResult(String name, String location, String ip, List<String> components) {
      this.name = name;
      this.location = location;
      this.ip = ip;
      this.components = components;
    }
  }

  /**
   * Constructs an EditNodeDialog pre-populated with the node's current data.
   *
   * @param node The node to edit
   */
  public EditNodeDialog(Node node) {
    this.originalNode = node;

    setTitle("Edit Node: " + node.getName());
    setHeaderText("Modify the node's properties and components.");

    VBox mainLayout = new VBox(25);
    mainLayout.setPadding(new Insets(30, 25, 20, 25));

    // === Form Grid ===
    GridPane grid = new GridPane();
    grid.setHgap(20);
    grid.setVgap(20);
    grid.getStyleClass().add("material-form-grid");

    // Node Name
    grid.add(new Label("Node Name:"), 0, 0);
    nameField.setText(node.getName());
    nameField.setPromptText("e.g., Greenhouse A-1");
    grid.add(nameField, 1, 0);

    // Location
    grid.add(new Label("Location:"), 0, 1);
    locationField.setText(node.getLocation());
    locationField.setPromptText("e.g., North-West Corner");
    grid.add(locationField, 1, 1);

    // IP Address
    grid.add(new Label("IP Address:"), 0, 2);
    ipField.setText(node.getIpAddress());
    ipField.setPromptText("e.g., 192.168.1.50");
    grid.add(ipField, 1, 2);

    // === Component Selection ===
    Label componentLabel = new Label("Select components (replaces current list):");
    componentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 1.1em;");

    TilePane tilePane = new TilePane();
    tilePane.setPadding(new Insets(10, 0, 10, 0));
    tilePane.setHgap(25);
    tilePane.setVgap(25);
    tilePane.setPrefColumns(4);

    // Pre-select existing components
    List<String> existingComponents = new ArrayList<>();

    // Map sensor types from server format to UI format
    for (String s : node.getSensorTypes()) {
      String uiName = mapSensorToUi(s);
      existingComponents.add(uiName);
      System.out.println("   Pre-selecting sensor: " + uiName);
    }

    // Map actuator types from server format to UI format
    for (String a : node.getActuatorTypes()) {
      String uiName = mapActuatorToUi(a);
      existingComponents.add(uiName);
      System.out.println("   Pre-selecting actuator: " + uiName);
    }

    selectedComponents.addAll(existingComponents);

    System.out.println("🔧 Existing components: " + existingComponents);

    tilePane.getChildren().addAll(
        createComponentToggle("Temperature Sensor", "/icons/temp_sensor.png",
            existingComponents.contains("Temperature Sensor")),
        createComponentToggle("Light Sensor", "/icons/light_sensor.png",
            existingComponents.contains("Light Sensor")),
        createComponentToggle("Humidity Sensor", "/icons/humidity_sensor.png",
            existingComponents.contains("Humidity Sensor")),
        createComponentToggle("PH Sensor", "/icons/Ph.png",
            existingComponents.contains("PH Sensor")),
        createComponentToggle("Fan", "/icons/fan.png", existingComponents.contains("Fan")),
        createComponentToggle("Water Pump", "/icons/waterpump.png",
            existingComponents.contains("Water Pump")),
        createComponentToggle("Window", "/icons/window.png", existingComponents.contains("Window")),
        createComponentToggle("CO2 Generator", "/icons/ceo2.png",
            existingComponents.contains("CO2 Generator"))
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

        System.out.println("✅ Dialog OK clicked - selected components: " + selectedComponents);

        return new NodeEditResult(
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
   * Creates a ToggleButton for component selection.
   *
   * @param name        Component name
   * @param iconPath    Path to icon resource
   * @param preSelected Whether this component should be pre-selected
   * @return Configured ToggleButton
   */
  private ToggleButton createComponentToggle(String name, String iconPath, boolean preSelected) {
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

    // Pre-select if needed
    if (preSelected) {
      button.setSelected(true);
      button.getStyleClass().add("selected");
      System.out.println("   ✅ Pre-selected: " + name);
    }

    button.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
      if (isSelected) {
        if (!selectedComponents.contains(name)) {
          selectedComponents.add(name);
          System.out.println(
              "   ➕ Selected: " + name + " (total: " + selectedComponents.size() + ")");
        }
        button.getStyleClass().add("selected");
      } else {
        selectedComponents.remove(name);
        System.out.println(
            "   ➖ Deselected: " + name + " (total: " + selectedComponents.size() + ")");
        button.getStyleClass().remove("selected");
      }
    });

    return button;
  }

  /**
   * Maps internal sensor names to UI display names. Server uses lowercase: "temperature",
   * "humidity", "light", "ph" UI uses: "Temperature Sensor", "Humidity Sensor", etc.
   */
  private String mapSensorToUi(String sensor) {
    return switch (sensor.toLowerCase()) {
      case "temperature" -> "Temperature Sensor";
      case "humidity" -> "Humidity Sensor";
      case "light" -> "Light Sensor";
      case "ph" -> "PH Sensor";
      default -> capitalize(sensor) + " Sensor";
    };
  }

  /**
   * Maps internal actuator names to UI display names.
   */
  private String mapActuatorToUi(String actuator) {
    return switch (actuator) {
      case "water_pump" -> "Water Pump";
      case "co2" -> "CO2 Generator";
      case "fan" -> "Fan";
      case "window" -> "Window";
      default -> capitalize(actuator);
    };
  }

  /**
   * Capitalizes first letter of string.
   */
  private String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

  /**
   * Validates IP address format.
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
   * Shows an error alert.
   */
  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Validation Error");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}