package controller;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import model.TemperatureSensor;
import ui.DashboardView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DashboardController {
  private final DashboardView view;
  private final List<model.Node> nodes = new ArrayList<>();
  private FlowPane nodesPane; // The container for node views
  private Label lastUpdateLabel;

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");

  public DashboardController(DashboardView view) {
    this.view = view;
    System.out.println("DashboardController initialized.");
  }

  public void setUiComponents(FlowPane nodesPane, Label lastUpdateLabel) {
    this.nodesPane = nodesPane;
    this.lastUpdateLabel = lastUpdateLabel;
  }

  /**
   * Adds a new node to the farm and redraws the dashboard.
   */
  public void addNode() {
    int newNodeId = nodes.size() + 1;
    model.Node node = new model.Node("Farm Node " + newNodeId);
    nodes.add(node);
    redrawDashboard();
  }

  /**
   * Adds a new temperature sensor to a specific node.
   * @param node The model node to which the sensor will be added.
   */
  public void addTemperatureSensor(model.Node node) {
    node.addSensor(new TemperatureSensor());
    redrawDashboard();
  }

public void addLightSensor(model.Node node) {
    node.addSensor(new model.LightSensor());
    redrawDashboard();
  }
  /**
   * Adds a new humidity sensor to a specific node.
   * @param node The model node to which the sensor will be added.
   */
  public void addHumiditySensor(model.Node node) {
    node.addSensor(new model.HumiditySensor());
    redrawDashboard();
  }

  public void addFan(model.Node node) {
    node.addActuator(new model.Fan()); //
    redrawDashboard();
  }

  /**
   * Handles the refresh data button click.
   * This simply redraws the entire dashboard, which includes updating sensor readings.
   */
  public void refreshData() {
    if (lastUpdateLabel != null) {
      String currentTime = LocalDateTime.now().format(FORMATTER);
      lastUpdateLabel.setText("Last update: " + currentTime);
    }
    redrawDashboard();
  }

  /**
   * Clears and redraws all UI components from the model list.
   * This is the central method for updating the view.
   */
  private void redrawDashboard() {
    nodesPane.getChildren().clear();
    for (model.Node node : nodes) {
      nodesPane.getChildren().add(createNodeView(node));
    }
  }

  /**
   * Creates the UI View for a single Node.
   *
   * @param node The node model to represent.
   * @return A Pane representing the node.
   */
  private Pane createNodeView(model.Node node) {
    Label nodeTitle = new Label(node.getName());
    nodeTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

    Button addSensorBtn = new Button("Add Temperature Sensor");
    addSensorBtn.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
    addSensorBtn.setOnAction(e -> addTemperatureSensor(node));

    Button addLightSensorBtn = new Button("Add Light Sensor");
    addLightSensorBtn.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
    addLightSensorBtn.setOnAction(e -> addLightSensor(node));

    Button addHumiditySensorBtn = new Button("Add Humidity Sensor");
    addHumiditySensorBtn.setStyle("-fx-background-color: #FBBC05; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
    addHumiditySensorBtn.setOnAction(e -> addHumiditySensor(node));

    Button addFanBtn = new Button("Add Fan");
    addFanBtn.setStyle("-fx-background-color: #FF6D01; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
    addFanBtn.setOnAction(e -> addFan(node));

    VBox sensorsContainer = new VBox(10);
    node.getSensors().forEach(sensor -> {
      sensorsContainer.getChildren().add(sensor.getVisualRepresentation());
    });
    VBox actuatorsContainer = new VBox(10);
    node.getActuators().forEach(actuator -> {
      actuatorsContainer.getChildren().add(actuator.getVisualRepresentation());
    });
    sensorsContainer.getChildren().addAll(actuatorsContainer.getChildren());

    VBox nodePane = new VBox(15, nodeTitle, sensorsContainer, addSensorBtn, addLightSensorBtn, addHumiditySensorBtn, addFanBtn);
    nodePane.setPadding(new javafx.geometry.Insets(15));
    nodePane.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
    nodePane.setPrefWidth(250);
    nodePane.setMinWidth(250);

    return nodePane;
  }
}
