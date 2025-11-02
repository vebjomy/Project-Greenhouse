package controller;
import App.MainApp;
import core.ClientApi;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
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

/**
 * The `DashboardController` class is responsible for managing the dashboard view and its interactions.
 * It handles the creation and management of nodes, sensors, and actuators, as well as refreshing the
 * dashboard data and updating the user interface.
 */
public class DashboardController {
  private final DashboardView view;
  private final MainApp mainApp; // link to MainApp for navigation
  private final List<Node> nodes = new ArrayList<>();
  private FlowPane nodesPane; // The container for node views
  private Label lastUpdateLabel;
  private ClientApi api; // For server communication
  private Timeline refreshTimeline; // New: For automatic refreshing
  private long refreshIntervalSeconds = 0; // New: Current interval
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");

  /**
   * Sets the ClientApi instance for server communication.
   * @param api The ClientApi instance to be used by this controller.
   */
  public void setApi(ClientApi api) {
    this.api = api;
    this.api.onSensorUpdate(ns -> {
      // Here can be logic to update nodes based on incoming sensor data
      // For now, we just refresh the data
      refreshData();
    });
  }
  /**
   * Constructs a new `DashboardController` with the specified dashboard view and MainApp instance.
   *
   * @param view The `DashboardView` instance associated with this controller.
   * @param mainApp The instance of the main application for UI navigation (e.g., logout).
   */
  public DashboardController(DashboardView view, MainApp mainApp) {
    this.view = view;
    this.mainApp = mainApp;
    System.out.println("DashboardController initialized.");
    // Initialize the Timeline for refreshing
    refreshTimeline = new Timeline(
        new KeyFrame(Duration.seconds(1), e -> refreshData())
    );
    refreshTimeline.setCycleCount(Timeline.INDEFINITE);
  }


  /**
   * Sets the UI components that the controller will manage.
   * @param nodesPane The FlowPane containing node views.
   * @param lastUpdateLabel The label displaying the last update time.
   */
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
      Node newNode = new Node(nodeData.name, nodeData.location);
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
  public void showAddComponentDialog(Node node) {
    AddComponentDialog dialog = new AddComponentDialog();
    Optional<List<String>> result = dialog.showAndWait();

    result.ifPresent(componentsToAdd -> {
      addComponentsToNode(node, componentsToAdd);
      redrawDashboard();
    });
  }
  /**
   * A helper method to add multiple components to a node based on a list of names.
   * @param node The node to which components will be added.
   * @param componentNames The list of component names to add.
   *
   */
  private void addComponentsToNode(Node node, List<String> componentNames) {
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
   * This method simulates data fetching and updates the UI accordingly.
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
  /**
   * Redraws the entire dashboard by clearing and recreating all node views.
   * This method is called whenever nodes are added or data is refreshed.
   */
  private void redrawDashboard() {
    if (nodesPane == null) return;
    nodesPane.getChildren().clear();
    for (Node node : nodes) {
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
   * @return The refresh interval in seconds.
   */
  public long getRefreshIntervalSeconds() {
    return refreshIntervalSeconds;
  }
  /**
   * Handles user logout: stops auto-refresh and returns to the splash screen.
   * The MainApp instance handles the actual scene navigation.
   */
  public void logout() {
    // 1. Stop the auto-refresh
    refreshTimeline.stop();
    // 2. Navigate back to splash screen using the MainApp instance
    mainApp.showSplashScreen();
    // Optionally: Perform any necessary session cleanup here (e.g., clear tokens)
  }
  /**
   * Returns the list of nodes managed by this controller.
   * @return The list of nodes.
   */
  public List<Node> getNodes() {
    return nodes;
  }
  /**
   * Deletes a node from the dashboard.
   * @param node The node to be deleted.
   */
  public void deleteNode(Node node) {
    nodes.remove(node);
    redrawDashboard();
  }
  // --- MODIFIED METHOD STARTS HERE ---
  /**
   * Creates the UI View for a single Node with a Material Design-inspired layout.
   * Includes a dropdown (MenuButton) for actions: Add Component, Edit, and Delete.
   */
  private Pane createNodeView(Node node) {
    // --- Node Title and Info ---
    Label nodeTitle = new Label(node.getName());
    nodeTitle.setStyle(
        "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #202124;"
    );

    Label idLabel = new Label("ID: " + node.getId());
    idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5f6368;");

    Label ipLabel = new Label("IP: " + node.getIpAddress());
    ipLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5f6368;");

    Label nodeLocation = new Label(node.getLocation());
    nodeLocation.setStyle("-fx-font-size: 12px; -fx-text-fill: #5f6368; -fx-font-style: italic;");

    VBox nodeInfoBox = new VBox(2, idLabel, ipLabel, nodeLocation);
    nodeInfoBox.setStyle("-fx-padding: 0 0 0 5;");

    // --- Material-style dropdown menu ---
    MenuButton actionsMenu = new MenuButton("⋮");
    actionsMenu.setStyle(
        "-fx-background-color: transparent;" +
            "-fx-font-size: 18px;" +
            "-fx-text-fill: #5f6368;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 2 8;"
    );

    // Menu items
    MenuItem addComponentItem = new MenuItem("+ Add Component");
    addComponentItem.setOnAction(e -> showAddComponentDialog(node));

    MenuItem editNodeItem = new MenuItem("Edit Node");
    editNodeItem.setOnAction(e -> {
      System.out.println("Edit Node functionality not implemented yet.");
    });

    MenuItem deleteNodeItem = new MenuItem("Delete Node");
    deleteNodeItem.setOnAction(e -> deleteNode(node));

    actionsMenu.getItems().addAll(addComponentItem, editNodeItem, deleteNodeItem);

    // --- Title bar (Node name + menu on the right) ---
    HBox titleBar = new HBox();
    titleBar.setSpacing(10);
    titleBar.setPadding(new Insets(5, 10, 5, 10));
    titleBar.setStyle(
        "-fx-alignment: center-left;" +
            "-fx-background-color: #f1f3f4;" +
            "-fx-background-radius: 12 12 0 0;"
    );

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    titleBar.getChildren().addAll(nodeTitle, spacer, actionsMenu);

    // --- Containers for sensors and actuators ---
    VBox sensorsContainer = new VBox(10);
    node.getSensors().forEach(sensor -> sensorsContainer.getChildren().add(sensor.getVisualRepresentation()));
    node.getActuators().forEach(actuator -> sensorsContainer.getChildren().add(actuator.getVisualRepresentation()));
    sensorsContainer.setPadding(new Insets(10, 15, 10, 15));

    // --- Combine info and sensors ---
    VBox contentBox = new VBox(8, nodeInfoBox, sensorsContainer);
    contentBox.setPadding(new Insets(10, 10, 10, 10));

    // --- Final node card layout ---
    VBox nodePane = new VBox(titleBar, contentBox);
    nodePane.setStyle(
        "-fx-background-color: #ffffff;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.1, 0, 2);" +
            "-fx-border-color: transparent;"
    );
    nodePane.setPrefWidth(250);
    nodePane.setMinWidth(250);

    return nodePane;
  }
}
