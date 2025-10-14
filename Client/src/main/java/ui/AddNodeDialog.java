package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A dialog for creating a new node, including its name, location, and initial components.
 */
public class AddNodeDialog extends Dialog<AddNodeDialog.NodeCreationResult> {

  private final TextField nameField = new TextField();
  private final TextField locationField = new TextField();
  private final List<String> selectedComponents = new ArrayList<>();

  public static class NodeCreationResult {
    public final String name;
    public final String location;
    public final List<String> components;

    public NodeCreationResult(String name, String location, List<String> components) {
      this.name = name;
      this.location = location;
      this.components = components;
    }
  }

  public AddNodeDialog() {
    setTitle("Create New Farm Node");
    setHeaderText("Enter the details for the new node.");

    VBox mainLayout = new VBox(20);
    mainLayout.setPadding(new Insets(20));

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.add(new Label("Node Name:"), 0, 0);
    grid.add(nameField, 1, 0);
    nameField.setPromptText("e.g., Greenhouse A-1");
    grid.add(new Label("Location:"), 0, 1);
    grid.add(locationField, 1, 1);
    locationField.setPromptText("e.g., North-West Corner");

    Label componentLabel = new Label("Select initial components:");
    componentLabel.setStyle("-fx-font-weight: bold;");

    TilePane tilePane = new TilePane();
    tilePane.setPadding(new Insets(10, 0, 10, 0));
    tilePane.setHgap(15);
    tilePane.setVgap(15);
    tilePane.setPrefColumns(4);

    // Use ToggleButtons for reliable hitbox and selected state
    tilePane.getChildren().addAll(
        createComponentToggle("Temperature Sensor", "/icons/temp_sensor.png"),
        createComponentToggle("Light Sensor", "/icons/light_sensor.png"),
        createComponentToggle("Humidity Sensor", "/icons/humidity_sensor.png"),
        createComponentToggle("Ph Sernor", "/icons/Ph.png"),
        createComponentToggle("Fan", "/icons/fan.png"),
        createComponentToggle("Waterpump", "/icons/waterpump.png"),
        createComponentToggle("Window", "/icons/window.png"),
        createComponentToggle("CO2 Generatod", "/icons/ceo2.png")
    );

    ScrollPane scrollPane = new ScrollPane(tilePane);
    scrollPane.setFitToWidth(true);
    scrollPane.setPrefHeight(150);

    mainLayout.getChildren().addAll(grid, new Separator(), componentLabel, scrollPane);
    getDialogPane().setContent(mainLayout);

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    okButton.disableProperty().bind(nameField.textProperty().isEmpty());

    getDialogPane().getStylesheets().add(
        Objects.requireNonNull(getClass().getResource("/client.css")).toExternalForm()
    );

    setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        return new NodeCreationResult(
            nameField.getText(),
            locationField.getText(),
            new ArrayList<>(selectedComponents) // copy for safety
        );
      }
      return null;
    });
  }

  /**
   * Creates a ToggleButton with an icon and a label. ToggleButton confines the hitbox
   * to the button area and provides built-in selected state.
   */
  private ToggleButton createComponentToggle(String name, String iconPath) {
    ImageView icon = new ImageView();
    icon.getStyleClass().add("image-view");
    try {
      Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath)), 48, 48, true, true);
      icon.setImage(image);
    } catch (Exception e) {
      System.err.println("Could not load icon: " + iconPath);
      // leave icon empty if not found
    }

    Label label = new Label(name);
    label.getStyleClass().add("label");
    VBox content = new VBox(5, icon, label);
    content.setAlignment(Pos.CENTER);

    ToggleButton button = new ToggleButton();
    button.setGraphic(content);
    button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

    // Make sizes follow the preferred size of content
    button.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    button.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

    // Optional: add css class for styling
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
