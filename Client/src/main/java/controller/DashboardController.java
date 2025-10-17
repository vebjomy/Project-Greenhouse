package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.*; // Import all models
import ui.AddComponentDialog;
import ui.AddNodeDialog;
import ui.DashboardView;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DashboardController {
  private final DashboardView view;
  private final List<model.Node> nodes = new ArrayList<>();
  private FlowPane nodesPane; // The container for node views
  private Label lastUpdateLabel;

  private Timeline refreshTimeline; // New: For automatic refreshing
  private long refreshIntervalSeconds = 0; // New: Current interval

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");

  public DashboardController(DashboardView view) {
    this.view = view;
    System.out.println("DashboardController initialized.");
    // Initialize the Timeline for refreshing
    refreshTimeline = new Timeline(
        new KeyFrame(Duration.seconds(1), e -> refreshData())
    );
    refreshTimeline.setCycleCount(Timeline.INDEFINITE);
  }

  public void setUiComponents(FlowPane nodesPane, Label lastUpdateLabel) {
    this.nodesPane = nodesPane;
    this.lastUpdateLabel = lastUpdateLabel;
  }

  /**
   * Opens a dialog to create a new node. If the user confirms,
   * the node is created and the dashboard is redrawn.
   */
  public void addNode() {
    AddNodeDialog dialog = new AddNodeDialog();
    Optional<AddNodeDialog.NodeCreationResult> result = dialog.showAndWait();

    result.ifPresent(nodeData -> {
      // Create the new node with name and location
      model.Node newNode = new model.Node(nodeData.name, nodeData.location);

      // Add the components the user selected in the dialog
      addComponentsToNode(newNode, nodeData.components);

      nodes.add(newNode);
      redrawDashboard();
    });
  }

  /**
   * Opens a dialog to add components to an existing node.
   * @param node The node to which components will be added.
   */
  public void showAddComponentDialog(model.Node node) {
    AddComponentDialog dialog = new AddComponentDialog();
    Optional<List<String>> result = dialog.showAndWait();

    result.ifPresent(componentsToAdd -> {
      addComponentsToNode(node, componentsToAdd);
      redrawDashboard();
    });
  }

  /**
   * A helper method to add multiple components to a node based on a list of names.
   */
  private void addComponentsToNode(model.Node node, List<String> componentNames) {
    for (String componentName : componentNames) {
      switch (componentName) {
        case "Temperature Sensor":
          node.addSensor(new TemperatureSensor());
          break;
        case "Light Sensor":
          node.addSensor(new LightSensor());
          break;
        case "Humidity Sensor":
          node.addSensor(new HumiditySensor());
          break;
        case "PH Sensor":
          node.addSensor(new PHSensor());
          break;
        case "Water Pump":
          node.addActuator(new WaterPump());
          break;
        case "CO2 Generator":
          node.addActuator(new CO2Generator());
          break; // Added missing break
        case "Fan":
          node.addActuator(new Fan());
          break;
        case "Window":
          node.addActuator(new Window());
          break;
        default:
          System.err.println("Unknown component: " + componentName);
      }
    }
  }

  /**
   * Manually refreshes the data and updates the last update label.
   */
  public void refreshData() {
    // Only update data if the controller has UI components set
    if (nodesPane == null) return;

    // Simulate data fetching/update for all nodes (e.g., calling node.updateData())
    // For now, just redraw and update time.

    if (lastUpdateLabel != null) {
      String currentTime = LocalDateTime.now().format(FORMATTER);
      lastUpdateLabel.setText("Last update: " + currentTime);
    }
    redrawDashboard();
  }

  private void redrawDashboard() {
    if (nodesPane == null) return;
    nodesPane.getChildren().clear();
    for (model.Node node : nodes) {
      nodesPane.getChildren().add(createNodeView(node));
    }
  }

  /**
   * Sets the interval for automatic refreshing.
   * @param seconds The refresh interval in seconds (0 to stop).
   */
  public void setAutoRefreshInterval(long seconds) {
    this.refreshIntervalSeconds = seconds;

    refreshTimeline.stop(); // Always stop before setting a new interval or stopping

    if (seconds > 0) {
      refreshTimeline.getKeyFrames().setAll(
          new KeyFrame(Duration.seconds(seconds), e -> refreshData())
      );
      refreshTimeline.play();
      System.out.println("Auto-refresh started every " + seconds + " seconds.");
    } else {
      // Interval is 0, so refresh is stopped
      System.out.println("Auto-refresh stopped.");
    }
  }

  /**
   * Returns the current auto-refresh interval.
   */
  public long getRefreshIntervalSeconds() {
    return refreshIntervalSeconds;
  }

  public List<model.Node> getNodes() {
    return nodes;
  }

  /**
   * Creates the UI View for a single Node.
   * This is heavily modified to show new info and use the new dialog.
   */
  private Pane createNodeView(model.Node node) {
    // ... (unchanged createNodeView method body)
    // --- Node Title and Location ---
    Label nodeTitle = new Label(node.getName());
    nodeTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

    Label nodeLocation = new Label(node.getLocation());
    nodeLocation.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-style: italic;");
    VBox titleBox = new VBox(2, nodeTitle, nodeLocation);

    // --- Containers for sensors and actuators ---
    VBox sensorsContainer = new VBox(10);
    node.getSensors().forEach(sensor -> {
      sensorsContainer.getChildren().add(sensor.getVisualRepresentation());
    });
    node.getActuators().forEach(actuator -> {
      sensorsContainer.getChildren().add(actuator.getVisualRepresentation());
    });

    // --- The single button to add new components ---
    Button addComponentBtn = new Button("+ Add Component");
    addComponentBtn.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
    addComponentBtn.setOnAction(e -> showAddComponentDialog(node)); // This now calls the dialog

    // --- Final layout for the node card ---
    VBox nodePane = new VBox(15, titleBox, sensorsContainer, addComponentBtn);
    nodePane.setPadding(new javafx.geometry.Insets(15));
    nodePane.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #d0d0d0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
    nodePane.setPrefWidth(250);
    nodePane.setMinWidth(250);

    return nodePane;
  }
}