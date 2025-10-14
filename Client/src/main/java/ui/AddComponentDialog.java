package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddComponentDialog extends Dialog<List<String>> {

  private final List<String> selectedComponents = new ArrayList<>();

  public AddComponentDialog() {
    setTitle("Add Components");
    setHeaderText("Select the sensors and actuators to add.");

    TilePane tilePane = new TilePane();
    tilePane.setPadding(new Insets(20));
    tilePane.setHgap(15);
    tilePane.setVgap(15);
    tilePane.setPrefColumns(3);

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
    getDialogPane().setContent(scrollPane);

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    getDialogPane().getStylesheets().add(
        Objects.requireNonNull(getClass().getResource("/client.css")).toExternalForm()
    );

    setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        return new ArrayList<>(selectedComponents);
      }
      return null;
    });
  }

  private ToggleButton createComponentToggle(String name, String iconPath) {
    ImageView icon = new ImageView();
    icon.getStyleClass().add("image-view");
    try {
      Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath)), 48, 48, true, true);
      icon.setImage(image);
    } catch (Exception e) {
      System.err.println("Could not load icon: " + iconPath);
    }

    Label label = new Label(name);
    label.getStyleClass().add("label");
    VBox content = new VBox(5, icon, label);
    content.setAlignment(Pos.CENTER);

    ToggleButton button = new ToggleButton();
    button.setGraphic(content);
    button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

    button.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    button.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

    button.getStyleClass().add("component-toggle-button");

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
