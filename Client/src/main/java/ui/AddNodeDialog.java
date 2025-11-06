package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A dialog for creating a new node, including its name, location, and initial components.
 */
public class AddNodeDialog extends Dialog<AddNodeDialog.NodeCreationResult> {
  private final TextField id = new TextField();
  private final TextField nameField = new TextField();
  private final TextField locationField = new TextField();
  private final TextField ipField = new TextField();
  private final List<String> selectedComponents = new ArrayList<>();
  /** A simple data class to hold the result of the node creation dialog.
  */
  public static class NodeCreationResult {
    public final String id;
    public final String name;
    public final String location;
    public final String ip;
    public final List<String> components;
    /** Constructs a NodeCreationResult with the given parameters.
     * @param name The name of the node.
     * @param location The location of the node.
     * @param ip The IP address of the node.
     * @param components The list of initial components for the node.
     */
    public NodeCreationResult(String id, String name, String location, String ip, List<String> components) {
      this.id = id;
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
    GridPane grid = new GridPane();
    grid.setHgap(20);
    grid.setVgap(20);
    grid.getStyleClass().add("material-form-grid");
    grid.add(new Label("Node ID:"), 0, 0);
    grid.add(id, 1, 0);
    id.setPromptText("e.g., Node-1");
    grid.add(new Label("Node Name:"), 0, 1);
    grid.add(nameField, 1, 1);
    nameField.setPromptText("e.g., Greenhouse A-1");
    grid.add(new Label("Location:"), 0, 2);
    grid.add(locationField, 1, 2);
    locationField.setPromptText("e.g., North-West Corner");
    grid.add(new Label("IP address:"), 0, 3);
    grid.add(ipField, 1, 3);
    ipField.setPromptText("IP- number of the node");
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
    okButton.disableProperty().bind(nameField.textProperty().isEmpty());
    getDialogPane().getStylesheets().add(
        Objects.requireNonNull(getClass().getResource("/test.css")).toExternalForm()
    );
    setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        return new NodeCreationResult(
            id.getText(),
            nameField.getText(),
            locationField.getText(),ipField.getText(),
            new ArrayList<>(selectedComponents) // copy for safety
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
      Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath)), 40, 40, true, true);
      icon.setImage(image);
    } catch (Exception e) {
      System.err.println("Could not load icon: " + iconPath);
      // leave icon empty if not found
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

    // Sync selection list with the toggle state
    button.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
      if (isSelected) {
        if (!selectedComponents.contains(name)) selectedComponents.add(name);
        button.getStyleClass().add("selected");
      } else {
        selectedComponents.remove(name);
        button.getStyleClass().remove("selected");
      }
    });
    return button;
  }
}