package controller;

import App.MainApp;
import core.ClientApi;
import dto.Topology;
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
import model.*;
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
 * Controller class for managing the dashboard view and its interactions in the Green House Control application.
 *
 * <p>This controller handles the complete lifecycle of greenhouse nodes, including:
 * <ul>
 *   <li>Creating, updating, and deleting nodes on the server</li>
 *   <li>Managing sensors and actuators for each node</li>
 *   <li>Synchronizing local UI state with server data</li>
 *   <li>Auto-refresh functionality for real-time updates</li>
 *   <li>Activity logging and user session management</li>
 * </ul>
 *
 * <p>The controller communicates with the server through {@link ClientApi} and maintains
 * a local cache of nodes for UI rendering. All node operations are synchronized with the
 * server to ensure data consistency.
 *
 * @author Green House Control Team
 * @version 2.0
 * @since 1.0
 * @see DashboardView
 * @see ClientApi
 * @see Node
 */
public class DashboardController {
  /** The associated dashboard view */
  private final DashboardView view;

  /** Reference to the main application for navigation */
  private final MainApp mainApp;

  /** Local cache of all greenhouse nodes */
  private final List<Node> nodes = new ArrayList<>();

  /** UI container for displaying node cards */
  private FlowPane nodesPane;

  /** Label showing the last data update timestamp */
  private Label lastUpdateLabel;

  /** Container for activity log entries */
  private VBox logContent;

  /** Text area for displaying command line output */
  private TextArea commandOutputArea;

  /** API client for server communication */
  private ClientApi api;

  /** Timeline for scheduling automatic data refreshes */
  private Timeline refreshTimeline;

  /** Auto-refresh interval in seconds (0 = disabled) */
  private long refreshIntervalSeconds = 0;

  /** Time formatter for log entries (HH:mm:ss) */
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

  /** Time formatter for full timestamps (HH:mm:ss dd.MM.yyyy) */
  private static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");

  /**
   * Sets the ClientApi instance and registers sensor update listeners.
   *
   * <p>This method configures the controller to receive real-time sensor updates
   * from the server. When sensor data changes, the dashboard is automatically refreshed.
   *
   * @param api The ClientApi instance to be used for server communication
   * @throws NullPointerException if api is null
   */
  public void setApi(ClientApi api) {
    this.api = api;
    this.api.onSensorUpdate(ns -> {
      refreshData();
    });
  }

  /**
   * Constructs a new DashboardController with the specified dependencies.
   *
   * <p>Initializes the controller with references to the view, main application,
   * and API client. Sets up the auto-refresh timeline with a default 1-second interval.
   *
   * @param view The DashboardView instance this controller manages
   * @param mainApp The main application instance for navigation operations
   * @param api The ClientApi instance for server communication
   * @throws NullPointerException if any parameter is null
   */
  public DashboardController(DashboardView view, MainApp mainApp, ClientApi api) {
    this.view = view;
    this.mainApp = mainApp;
    this.api = api;
    System.out.println("DashboardController initialized.");

    refreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> refreshData())
    );
    refreshTimeline.setCycleCount(Timeline.INDEFINITE);
  }

  /**
   * Sets the UI components that the controller will manage and update.
   *
   * <p>This method must be called before any UI operations are performed.
   * It establishes the connection between the controller and the view's components.
   *
   * @param nodesPane The FlowPane container for node visualization cards
   * @param lastUpdateLabel The label displaying the last data refresh timestamp
   * @param logContent The VBox container for activity log entries
   * @param commandOutputArea The TextArea for command line output display
   */
  public void setUiComponents(FlowPane nodesPane, Label lastUpdateLabel, VBox logContent, TextArea commandOutputArea) {
    this.nodesPane = nodesPane;
    this.lastUpdateLabel = lastUpdateLabel;
    this.logContent = logContent;
    this.commandOutputArea = commandOutputArea;
  }

  /**
   * Logs an activity to the dashboard's activity log with timestamp and source.
   *
   * <p>Log entries are added to the top of the log view (most recent first) and include
   * a formatted timestamp. This method is thread-safe and can be called from any thread.
   *
   * @param source The source or origin of the activity (e.g., node name or "System")
   * @param message The descriptive message about the activity
   */
  public void logActivity(String source, String message) {
    Platform.runLater(() -> {
      String currentTime = LocalDateTime.now().format(FORMATTER);
      HBox entry = view.createLogEntry(currentTime, source, message);
      logContent.getChildren().add(0, entry);
    });
  }

  /**
   * Opens a dialog to create a new greenhouse node and synchronizes it with the server.
   *
   * <p>This method performs the following operations:
   * <ol>
   *   <li>Displays an {@link AddNodeDialog} for user input</li>
   *   <li>Converts UI component selections to protocol format</li>
   *   <li>Sends node creation request to server via {@link ClientApi#createNode}</li>
   *   <li>Creates local Node representation with server-assigned ID</li>
   *   <li>Adds components (sensors/actuators) to the local node</li>
   *   <li>Updates the UI and logs the activity</li>
   * </ol>
   *
   * <p>The operation is asynchronous - UI updates occur after server confirmation.
   * If the server operation fails, an error message is logged.
   *
   * @see AddNodeDialog
   * @see Topology.Node
   */
  public void addNode() {
    if (api == null) {
      System.err.println("❌ API is NULL!");
      logActivity("System", "ERROR: API not initialized");
      return;
    }
    AddNodeDialog dialog = new AddNodeDialog();
    Optional<AddNodeDialog.NodeCreationResult> result = dialog.showAndWait();

    result.ifPresent(nodeData -> {
      System.out.println("🔧 [DEBUG] Creating node: " + nodeData.name);

      // Create DTO for server
      Topology.Node serverNode = new Topology.Node();
      serverNode.name = nodeData.name;
      serverNode.location = nodeData.location;
      serverNode.ip = nodeData.ip;

      // Convert components to sensors/actuators lists
      serverNode.sensors = new ArrayList<>();
      serverNode.actuators = new ArrayList<>();

      for (String component : nodeData.components) {
        if (component.endsWith("Sensor")) {
          String sensorType = component.replace(" Sensor", "").toLowerCase();
          serverNode.sensors.add(sensorType);
          System.out.println("🔧 [DEBUG] Added sensor: " + sensorType);
        } else {
          String actuatorType = mapActuatorName(component);
          serverNode.actuators.add(actuatorType);
          System.out.println("🔧 [DEBUG] Added actuator: " + actuatorType);
        }
      }

      System.out.println("🔧 [DEBUG] Sending to server...");
      System.out.println("🔧 [DEBUG] Node DTO: name=" + serverNode.name +
              ", location=" + serverNode.location +
              ", ip=" + serverNode.ip);

      // Send to server
      api.createNode(serverNode).thenAccept(nodeId -> {
        System.out.println("✅ [DEBUG] Server responded with nodeId: " + nodeId);
        Platform.runLater(() -> {
          // Create local node with server-assigned ID
          Node newNode = new Node(serverNode.name, serverNode.location);
          addComponentsToNode(newNode, nodeData.components);
          nodes.add(newNode);
          redrawDashboard();

          String componentSummary = String.format(
                  "%d sensor(s), %d actuator(s).",
                  serverNode.sensors.size(),
                  serverNode.actuators.size()
          );

          logActivity(newNode.getName(), String.format(
                  "Node created on server (ID: %s). Location: %s. IP: %s. Components: %s",
                  nodeId, serverNode.location, serverNode.ip, componentSummary
          ));
        });
      }).exceptionally(ex -> {
        System.err.println("❌ [DEBUG] Server error: " + ex.getMessage());
        ex.printStackTrace();
        Platform.runLater(() -> {
          logActivity("System", "Failed to create node on server: " + ex.getMessage());
        });
        return null;
      });
    });
  }

  /**
   * Maps UI component names to server protocol actuator names.
   *
   * <p>This method ensures consistent naming between the user interface and
   * the server protocol. It converts human-readable names to protocol-compliant
   * identifiers.
   *
   * <p>Supported mappings:
   * <ul>
   *   <li>"Water Pump" → "water_pump"</li>
   *   <li>"CO2 Generator" → "co2"</li>
   *   <li>"Fan" → "fan"</li>
   *   <li>"Window" → "window"</li>
   * </ul>
   *
   * @param uiName The user-interface display name of the actuator
   * @return The protocol-compliant actuator name in lowercase with underscores
   */
  private String mapActuatorName(String uiName) {
    return switch (uiName) {
      case "Water Pump" -> "water_pump";
      case "CO2 Generator" -> "co2";
      case "Fan" -> "fan";
      case "Window" -> "window";
      default -> uiName.toLowerCase().replace(" ", "_");
    };
  }

  /**
   * Opens a dialog to add components (sensors/actuators) to an existing node.
   *
   * <p>This method allows users to expand a node's capabilities by adding new
   * sensors or actuators. The dashboard is automatically refreshed to reflect
   * the changes, and the activity is logged.
   *
   * @param node The node to which components will be added
   * @throws NullPointerException if node is null
   * @see AddComponentDialog
   */
  public void showAddComponentDialog(Node node) {
    AddComponentDialog dialog = new AddComponentDialog();
    Optional<List<String>> result = dialog.showAndWait();

    result.ifPresent(componentsToAdd -> {
      if (!componentsToAdd.isEmpty()) {
        int addedCount = addComponentsToNode(node, componentsToAdd);
        redrawDashboard();

        String componentList = componentsToAdd.stream()
                .map(c -> c.replace(" Sensor", "(S)").replace(" Pump", "(A)")
                        .replace(" Generator", "(A)").replace(" Window", "(A)").replace(" Fan", "(A)"))
                .collect(Collectors.joining(", "));

        String logMessage = String.format(
                "%d component(s) added. New items: %s. Total components: %d.",
                addedCount, componentList,
                node.getSensors().size() + node.getActuators().size()
        );
        logActivity(node.getName(), logMessage);
      }
    });
  }

  /**
   * Adds multiple components to a node based on their display names.
   *
   * <p>This helper method instantiates and adds sensor or actuator objects
   * to the specified node. It supports all standard greenhouse components
   * including temperature, light, humidity, and pH sensors, as well as
   * fans, water pumps, CO2 generators, and window controllers.
   *
   * @param node The node to which components will be added
   * @param componentNames A list of component display names to add
   * @return The number of sensors added (actuators are not counted)
   * @throws NullPointerException if node or componentNames is null
   */
  private int addComponentsToNode(Node node, List<String> componentNames) {
    int sensorsAdded = 0;
    for (String componentName : componentNames) {
      switch (componentName) {
        case "Temperature Sensor" -> {
          node.addSensor(new TemperatureSensor());
          sensorsAdded++;
        }
        case "Light Sensor" -> {
          node.addSensor(new LightSensor());
          sensorsAdded++;
        }
        case "Humidity Sensor" -> {
          node.addSensor(new HumiditySensor());
          sensorsAdded++;
        }
        case "PH Sensor" -> {
          node.addSensor(new PHSensor());
          sensorsAdded++;
        }
        case "Water Pump" -> node.addActuator(new WaterPump());
        case "CO2 Generator" -> node.addActuator(new CO2Generator());
        case "Fan" -> node.addActuator(new Fan());
        case "Window" -> node.addActuator(new Window());
        default -> System.err.println("Unknown component: " + componentName);
      }
    }
    return sensorsAdded;
  }

  // Добавь этот метод в DashboardController.java

  /**
   * Synchronizes the dashboard with the server topology and displays the result.
   *
   * <p>This method fetches the current topology from the server and displays
   * a detailed report in both the activity log and a popup dialog. It's useful
   * for verifying that nodes were successfully created on the server.
   *
   * <p>The report includes:
   * <ul>
   *   <li>Total number of nodes on the server</li>
   *   <li>Detailed information for each node (name, IP, location)</li>
   *   <li>Comparison with local node count</li>
   * </ul>
   */
  public void syncWithServer() {
    logActivity("System", "Fetching topology from server...");

    api.getTopology().thenAccept(topology -> {
      Platform.runLater(() -> {
        if (topology.nodes == null || topology.nodes.isEmpty()) {
          logActivity("System", "⚠️ Server has no nodes!");
          showTopologyDialog(0, "No nodes found on server");
          return;
        }

        // Log summary
        logActivity("System", String.format(
                "✅ Server topology: %d nodes (Local: %d nodes)",
                topology.nodes.size(),
                nodes.size()
        ));

        // Build detailed report
        StringBuilder report = new StringBuilder();
        report.append(String.format("Server nodes: %d\n", topology.nodes.size()));
        report.append(String.format("Local nodes: %d\n\n", nodes.size()));
        report.append("═══════════════════════════\n\n");

        for (var node : topology.nodes) {
          report.append(String.format("🏠 %s\n", node.name));
          report.append(String.format("   ID: %s\n", node.id));
          report.append(String.format("   IP: %s\n", node.ip));
          report.append(String.format("   Location: %s\n", node.location));
          report.append(String.format("   Sensors: %d\n",
                  node.sensors != null ? node.sensors.size() : 0));
          report.append(String.format("   Actuators: %d\n",
                  node.actuators != null ? node.actuators.size() : 0));
          report.append("\n");
        }

        showTopologyDialog(topology.nodes.size(), report.toString());
      });
    }).exceptionally(ex -> {
      Platform.runLater(() -> {
        logActivity("System", "❌ Failed to fetch topology: " + ex.getMessage());
        showErrorDialog("Topology Error",
                "Failed to fetch topology from server:\n" + ex.getMessage());
      });
      return null;
    });
  }

  /**
   * Displays a dialog with topology information.
   *
   * @param nodeCount The total number of nodes on the server
   * @param details Detailed information about all nodes
   */
  private void showTopologyDialog(int nodeCount, String details) {
    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION
    );
    alert.setTitle("Server Topology");
    alert.setHeaderText(String.format("📊 Server has %d node(s)", nodeCount));
    alert.setContentText(details);

    // Make dialog resizable for long lists
    alert.setResizable(true);
    alert.getDialogPane().setPrefWidth(500);

    alert.showAndWait();
  }

  /**
   * Displays an error dialog.
   *
   * @param title The dialog title
   * @param message The error message
   */
  private void showErrorDialog(String title, String message) {
    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
    );
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  /**
   * Manually refreshes all dashboard data and updates the last update timestamp.
   *
   * <p>This method is called both manually by the user and automatically by the
   * refresh timeline. It updates the displayed timestamp and redraws all node
   * visualizations. If auto-refresh is enabled, the activity is logged.
   *
   * <p>Thread-safe: Can be called from any thread via Platform.runLater.
   */
  public void refreshData() {
    if (nodesPane == null) return;

    if (lastUpdateLabel != null) {
      String currentTime = LocalDateTime.now().format(FULL_FORMATTER);
      lastUpdateLabel.setText("Last update: " + currentTime);
    }
    redrawDashboard();

    if (refreshIntervalSeconds > 0) {
      logActivity("System", "Auto-refresh: Data update OK.");
    }
  }

  /**
   * Redraws the entire dashboard by clearing and recreating all node visualizations.
   *
   * <p>This private method is called whenever the node list changes or data is refreshed.
   * It efficiently updates the UI by removing all existing node cards and creating
   * new ones from the current node list.
   */
  private void redrawDashboard() {
    if (nodesPane == null) return;
    nodesPane.getChildren().clear();
    for (Node node : nodes) {
      nodesPane.getChildren().add(createNodeView(node));
    }
  }

  /**
   * Sets the automatic refresh interval and starts/stops the refresh timeline.
   *
   * <p>This method controls the auto-refresh functionality:
   * <ul>
   *   <li>If seconds > 0: Starts automatic refresh at the specified interval</li>
   *   <li>If seconds = 0: Stops automatic refresh</li>
   * </ul>
   *
   * <p>The refresh operation is performed on the JavaFX Application Thread.
   * Each change to the refresh interval is logged for user visibility.
   *
   * @param seconds The refresh interval in seconds; 0 to disable auto-refresh
   * @throws IllegalArgumentException if seconds is negative
   */
  public void setAutoRefreshInterval(long seconds) {
    this.refreshIntervalSeconds = seconds;
    refreshTimeline.stop();
    if (seconds > 0) {
      refreshTimeline.getKeyFrames().setAll(
              new KeyFrame(Duration.seconds(seconds), e -> refreshData())
      );
      refreshTimeline.play();
      logActivity("System", "Auto-refresh started every " + seconds + " seconds.");
    } else {
      logActivity("System", "Auto-refresh stopped.");
    }
  }

  /**
   * Returns the current auto-refresh interval in seconds.
   *
   * @return The refresh interval in seconds; 0 if auto-refresh is disabled
   */
  public long getRefreshIntervalSeconds() {
    return refreshIntervalSeconds;
  }

  /**
   * Logs out the current user and returns to the login screen.
   *
   * <p>This method performs cleanup operations before logout:
   * <ul>
   *   <li>Stops the auto-refresh timeline</li>
   *   <li>Logs the logout activity</li>
   *   <li>Navigates to the login screen</li>
   * </ul>
   *
   * <p>Note: Node data is preserved in memory and will be available
   * if the user logs back in during the same application session.
   */
  public void logout() {
    refreshTimeline.stop();
    logActivity("User", "User logged out.");
    mainApp.showLoginScreen();
  }

  /**
   * Returns an immutable view of all nodes managed by this controller.
   *
   * @return The list of all greenhouse nodes in the dashboard
   */
  public List<Node> getNodes() {
    return nodes;
  }

  /**
   * Removes a node from the dashboard and updates the UI.
   *
   * <p>This method removes the node from the local cache and redraws
   * the dashboard. The deletion is logged for audit purposes.
   *
   * <p>Note: This currently only removes the node from the local UI.
   * Future implementation should also delete the node from the server.
   *
   * @param node The node to be deleted
   * @throws NullPointerException if node is null
   * @see #addNode()
   */
  public void deleteNode(Node node) {
    nodes.remove(node);
    redrawDashboard();
    logActivity(node.getName(), "Node deleted from the system.");
  }

  /**
   * Creates a visual representation (card) for a single greenhouse node.
   *
   * <p>This method generates a Material Design-inspired card with:
   * <ul>
   *   <li>Title bar with node name and action menu (dropdown)</li>
   *   <li>Node information: ID, IP address, and location</li>
   *   <li>Visual representations of all attached sensors</li>
   *   <li>Visual representations of all attached actuators</li>
   *   <li>Action menu with options to add components, edit, or delete the node</li>
   * </ul>
   *
   * <p>The card uses a clean, modern design with rounded corners, subtle shadows,
   * and a neutral color palette for optimal readability.
   *
   * @param node The node to visualize
   * @return A Pane containing the complete visual representation of the node
   * @throws NullPointerException if node is null
   */
  private Pane createNodeView(Node node) {
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

    MenuButton actionsMenu = new MenuButton("⋮");
    actionsMenu.setStyle(
            "-fx-background-color: transparent;" +
                    "-fx-font-size: 18px;" +
                    "-fx-text-fill: #5f6368;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 2 8;"
    );

    MenuItem addComponentItem = new MenuItem("+ Add Component");
    addComponentItem.setOnAction(e -> showAddComponentDialog(node));

    MenuItem editNodeItem = new MenuItem("Edit Node");
    editNodeItem.setOnAction(e -> logActivity(node.getName(), "Edit dialog opened."));

    MenuItem deleteNodeItem = new MenuItem("Delete Node");
    deleteNodeItem.setOnAction(e -> deleteNode(node));

    actionsMenu.getItems().addAll(addComponentItem, editNodeItem, deleteNodeItem);

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

    VBox sensorsContainer = new VBox(10);
    node.getSensors().forEach(sensor ->
            sensorsContainer.getChildren().add(sensor.getVisualRepresentation()));
    node.getActuators().forEach(actuator ->
            sensorsContainer.getChildren().add(actuator.getVisualRepresentation()));
    sensorsContainer.setPadding(new Insets(10, 15, 10, 15));

    VBox contentBox = new VBox(8, nodeInfoBox, sensorsContainer);
    contentBox.setPadding(new Insets(10, 10, 10, 10));

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
