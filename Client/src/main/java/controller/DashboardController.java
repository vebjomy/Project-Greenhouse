package controller;
import App.MainApp;
import core.ClientApi;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
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
import java.util.stream.Collectors;

/**
 * The `DashboardController` class is responsible for managing the dashboard view and its interactions.
 * It handles the creation and management of nodes, sensors, and actuators, as well as refreshing the
 * dashboard data and updating the user interface.
 */
public class DashboardController {
  private final DashboardView view;
  private final MainApp mainApp; // mainApp reference for scene management
  private final List<Node> nodes = new ArrayList<>();
  private FlowPane nodesPane; // container for node views
  private Label lastUpdateLabel;
  private VBox logContent; // VBox for log entries
  private TextArea commandOutputArea; // NEW: Text area for command output/history
  private ClientApi api; //for server communication
  private Timeline refreshTimeline; // for auto-refresh
  private long refreshIntervalSeconds = 0; // in seconds, 0 means no auto-refresh
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");

  /**
   * Sets the ClientApi instance for server communication.
   * @param api The ClientApi instance to be used by this controller.
   */
  public void setApi(ClientApi api) {
    this.api = api;
    this.api.onSensorUpdate(ns -> {
      // Handle sensor update from server
      refreshData();
    });
  }
  /**
   * Constructs a new `DashboardController` with the specified dashboard view and MainApp instance.
   *
   * @param view The `DashboardView` instance associated with this controller.
   * @param mainApp The instance of the main application for UI navigation (e.g., logout).
   */
  public DashboardController(DashboardView view, MainApp mainApp,ClientApi api) {
    this.view = view;
    this.mainApp = mainApp; // save mainApp reference
    System.out.println("DashboardController initialized.");

    // update every second by default, can be changed later
    refreshTimeline = new Timeline(
        new KeyFrame(Duration.seconds(1), e -> refreshData())
    );
    refreshTimeline.setCycleCount(Timeline.INDEFINITE);
  }


  /**
   * Sets the UI components that the controller will manage.
   * @param nodesPane The FlowPane containing node views.
   * @param lastUpdateLabel The label displaying the last update time.
   * @param logContent The VBox containing log entries.
   * @param commandOutputArea The TextArea for command line output.
   */
  public void setUiComponents(FlowPane nodesPane, Label lastUpdateLabel, VBox logContent, TextArea commandOutputArea) {
    this.nodesPane = nodesPane;
    this.lastUpdateLabel = lastUpdateLabel;
    this.logContent = logContent;
    this.commandOutputArea = commandOutputArea; // NEW: save command output area
  }

  /**
   * Logs an activity to the log view with a timestamp, source, and message.
   * @param source The source of the activity (e.g., node name).
   * @param message The message describing the activity.
   *
   */
  public void logActivity(String source, String message) {
    Platform.runLater(() -> {
      String currentTime = LocalDateTime.now().format(FORMATTER);
      HBox entry = view.createLogEntry(currentTime, source, message);
      logContent.getChildren().add(0, entry); // add to the top
    });
  }

  /**
   * Opens a dialog to create a new node. If the user confirms,
   * the node is created and the dashboard is redrawn.
   */
  public void addNode() {
    AddNodeDialog dialog = new AddNodeDialog();
    Optional<AddNodeDialog.NodeCreationResult> result = dialog.showAndWait();

    result.ifPresent(nodeData -> {
      // 1. create new node
      Node newNode = new Node(nodeData.name, nodeData.location);

      // 2. add components
      int sensorsCount = addComponentsToNode(newNode, nodeData.components);

      nodes.add(newNode);
      redrawDashboard();

      // 3. log creation
      String componentSummary = String.format(
          "%d sensor(s), %d actuator(s).",
          sensorsCount,
          newNode.getActuators().size()
      );
      String logMessage = String.format(
          "New Node '%s' created at %s. Status: OK. Components: %s",
          newNode.getName(),
          newNode.getLocation(),
          componentSummary
      );
      logActivity(newNode.getName(), logMessage);
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
      if (!componentsToAdd.isEmpty()) {
        int addedCount = addComponentsToNode(node, componentsToAdd);
        redrawDashboard();

        // log the addition
        String componentList = componentsToAdd.stream()
            .map(c -> c.replace(" Sensor", "(S)").replace(" Pump",
                "(A)").replace(" Generator", "(A)").replace(" Window",
                "(A)").replace(" Fan", "(A)"))
            .collect(Collectors.joining(", "));
        String logMessage = String.format(
            "%d component(s) added. New items: %s. Total components: %d.",
            addedCount,
            componentList,
            node.getSensors().size() + node.getActuators().size()
        );
        logActivity(node.getName(), logMessage);
      }
    });
  }
  /**
   * A helper method to add multiple components to a node based on a list of names.
   * @param node The node to which components will be added.
   * @param componentNames The list of component names to add.
   * @return The number of sensors added.
   */
  private int addComponentsToNode(Node node, List<String> componentNames) {
    int sensorsAdded = 0;
    for (String componentName : componentNames) {
      switch (componentName) {
        case "Temperature Sensor":
          node.addSensor(new TemperatureSensor());
          sensorsAdded++;
          break;
        case "Light Sensor":
          node.addSensor(new LightSensor());
          sensorsAdded++;
          break;
        case "Humidity Sensor":
          node.addSensor(new HumiditySensor());
          sensorsAdded++;
          break;
        case "PH Sensor":
          node.addSensor(new PHSensor());
          sensorsAdded++;
          break;
        case "Water Pump":
          node.addActuator(new WaterPump());
          break;
        case "CO2 Generator":
          node.addActuator(new CO2Generator());
          break;
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
    return sensorsAdded;
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
      String currentTime = LocalDateTime.now().format(FULL_FORMATTER);
      lastUpdateLabel.setText("Last update: " + currentTime);
    }
    redrawDashboard();
    //log auto-refresh activity
    if (refreshIntervalSeconds > 0) {
      logActivity("System", "Auto-refresh: Data update OK.");
    }
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
    refreshTimeline.stop(); // always stop before changing
    if (seconds > 0) {
      refreshTimeline.getKeyFrames().setAll(
          new KeyFrame(Duration.seconds(seconds), e -> refreshData())
      );
      refreshTimeline.play();
      logActivity("System", "Auto-refresh started every " + seconds + " seconds.");
    } else {
      // interval 0 means stop auto-refresh
      logActivity("System", "Auto-refresh stopped.");
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
    // 1. stop auto-refresh
    refreshTimeline.stop();
    logActivity("User", "User logged out.");
    // 2. back to splash screen
    mainApp.showSplashScreen();
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
    logActivity(node.getName(), "Node deleted from the system.");
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
      logActivity(node.getName(), "Edit dialog opened.");
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
    node.getSensors().forEach(sensor ->
        sensorsContainer.getChildren().add(sensor.getVisualRepresentation()));
    node.getActuators().forEach(actuator ->
        sensorsContainer.getChildren().add(actuator.getVisualRepresentation()));
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
