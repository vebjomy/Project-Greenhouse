package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;


/**
 * A dialog that allows users to select and add new components (sensors and actuators) to a node.
 */
public class AddComponentDialog extends Dialog<List<String>> {

  private final List<String> selectedComponents = new ArrayList<>();

  /**
   * Constructs the AddComponentDialog with predefined components.
   */
  public AddComponentDialog() {
    setTitle("Add New Components");
    setHeaderText("Choose your sensors and actuators to add to the node.");

    TilePane tilePane = new TilePane();
    tilePane.setPadding(new Insets(25));
    tilePane.setHgap(20);
    tilePane.setVgap(20);
    tilePane.setPrefColumns(3);

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
    scrollPane.getStyleClass().add("material-scroll-pane");
    getDialogPane().setContent(scrollPane);

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    getDialogPane().lookupButton(ButtonType.OK).getStyleClass().addAll("dialog-button", "primary");
    getDialogPane().lookupButton(ButtonType.CANCEL).getStyleClass().add("dialog-button");

    getDialogPane().getStyleClass().add("material-dialog-pane");

    getDialogPane().getStylesheets().add(
        Objects.requireNonNull(getClass().getResource("/sensors.css")).toExternalForm()
    );

    setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        return new ArrayList<>(selectedComponents);
      }
      return null;
    });
  }

  /**
   * Creates a toggle button for a component with an icon and label.
   *
   * @param name     The name of the component.
   * @param iconPath The path to the component's icon.
   * @return A ToggleButton representing the component.
   */
  private ToggleButton createComponentToggle(String name, String iconPath) {

    final int iconSize = 40;
    ImageView icon = new ImageView();

    try {
      Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath)),
          iconSize, iconSize, true, true);
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

    final int tileSize = 100;
    button.setMinWidth(tileSize);
    button.setMinHeight(tileSize);
    button.setMaxWidth(tileSize);
    button.setMaxHeight(tileSize);

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
}