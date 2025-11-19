package controller;

import App.MainApp;
import core.ClientApi;
import dto.Topology;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Node;
import ui.AddNodeDialog;
import ui.DashboardView;
import ui.EditNodeDialog;
import ui.components.Co2ActuatorView;
import ui.components.FanActuatorView;
import ui.components.HumiditySensorView;
import ui.components.LightSensorView;
import ui.components.PhSensorView;
import ui.components.TemperatureSensorView;
import ui.components.WaterPumpActuatorView;
import ui.components.WindowActuatorView;

/**
 * Controller class for managing the dashboard view and its interactions.
 *
 * <p>This controller handles the complete lifecycle of greenhouse nodes, including:
 * <ul>
 *   <li>Creating, updating, and deleting nodes on the server</li>
 *   <li>Managing sensors and actuators for each node</li>
 *   <li>Synchronizing local UI state with server data via real-time updates</li>
 *   <li>Auto-refresh functionality for periodic updates</li>
 *   <li>Activity logging and user session management</li>
 * </ul>
 *
 * <p><b>Architecture:</b> This controller follows the MVC pattern where:
 * <ul>
 *   <li>Model: {@link Node} (stores data received from server)</li>
 *   <li>View: {@link DashboardView} + UI component classes</li>
 *   <li>Controller: This class (handles business logic and server communication)</li>
 * </ul>
 *
 * <p><b>Data Flow:</b>
 * <pre>
 * Server → ClientApi → ClientState → DashboardController → Node → UI Components
 * UI → DashboardController → ClientApi → Server
 * </pre>
 *
 * @author Green House Control Team
 * @version 3.0
 * @see DashboardView
 * @see ClientApi
 * @see Node
 * @since 1.0
 */
public class DashboardController {

  // ========== Dependencies ==========

  /**
   * The associated dashboard view.
   */
  private final DashboardView view;

  /**
   * Reference to the main application for navigation.
   */
  private final MainApp mainApp;

  /**
   * API client for server communication.
   */
  private ClientApi api;

  // ========== Data Storage ==========

  /**
   * Local cache of all greenhouse nodes (keyed by node ID). This map is synchronized with the
   * server via topology and node_change messages.
   */
  private final Map<String, Node> nodes = new HashMap<>();

  /**
   * UI cards for each node (keyed by node ID). Allows efficient updates without full dashboard
   * redraw.
   */
  private final Map<String, VBox> nodeCards = new HashMap<>();

  // ========== UI Components ==========

  /**
   * UI container for displaying node cards.
   */
  private FlowPane nodesPane;

  /**
   * Label showing the last data update timestamp.
   */
  private Label lastUpdateLabel;

  /**
   * Container for activity log entries.
   */
  private VBox logContent;

  /**
   * Text area for displaying command line output.
   */
  private TextArea commandOutputArea;

  // ========== Auto-Refresh ==========

  /**
   * Timeline for scheduling automatic data refreshes.
   */
  private Timeline refreshTimeline;

  /**
   * Auto-refresh interval in seconds (0 = disabled).
   */
  private long refreshIntervalSeconds = 0;

  // ========== Time Formatters ==========

  /**
   * Time formatter for log entries (HH:mm:ss).
   */
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm:ss");

  /**
   * Time formatter for full timestamps (HH:mm:ss dd.MM.yyyy).
   */
  private static final DateTimeFormatter FULL_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");

  // ========== Constructor ==========

  /**
   * Constructs a new DashboardController with the specified dependencies.
   *
   * <p>Initializes the controller with references to the view, main application,
   * and API client. Sets up the auto-refresh timeline with a default interval.
   *
   * @param view    The DashboardView instance this controller manages
   * @param mainApp The main application instance for navigation operations
   * @param api     The ClientApi instance for server communication (can be null initially)
   * @throws NullPointerException if view or mainApp is null
   */
  public DashboardController(DashboardView view, MainApp mainApp, ClientApi api) {
    if (view == null || mainApp == null) {
      throw new NullPointerException("View and MainApp cannot be null");
    }

    this.view = view;
    this.mainApp = mainApp;
    this.api = api;

    // Initialize auto-refresh timeline (not started by default)
    refreshTimeline = new Timeline(
        new KeyFrame(Duration.seconds(1), e -> manualRefresh())
    );
    refreshTimeline.setCycleCount(Timeline.INDEFINITE);

    Timeline realTimeClock = new Timeline(
        new KeyFrame(Duration.seconds(1), e -> updateTime())
    );
    realTimeClock.setCycleCount(Timeline.INDEFINITE);
    realTimeClock.play();
    System.out.println("DashboardController initialized.");
  }

  // ========== Initialization ==========

  /**
   * Sets the UI components that the controller will manage and update.
   *
   * <p>This method must be called before any UI operations are performed.
   * It establishes the connection between the controller and the view's components.
   *
   * @param nodesPane         The FlowPane container for node visualization cards
   * @param lastUpdateLabel   The label displaying the last data refresh timestamp
   * @param logContent        The VBox container for activity log entries
   * @param commandOutputArea The TextArea for command line output display
   */
  public void setUiComponents(FlowPane nodesPane, Label lastUpdateLabel,
      VBox logContent, TextArea commandOutputArea) {
    this.nodesPane = nodesPane;
    this.lastUpdateLabel = lastUpdateLabel;
    this.logContent = logContent;
    this.commandOutputArea = commandOutputArea;
  }

  /**
   * Updates the last update label with the current date and time.
   *
   * <p>This method is called every second by a Timeline to keep the timestamp
   * current. It formats the date and time using FULL_TIME_FORMATTER.
   */

  private void updateTime() {
    if (lastUpdateLabel != null) {
      String currentTime = LocalDateTime.now().format(FULL_TIME_FORMATTER);
      lastUpdateLabel.setText("Date and time: " + currentTime);
    }
  }


  /**
   * Sets the ClientApi instance and registers real-time update listeners.
   *
   * <p>This method configures the controller to receive real-time updates from the server:
   * <ul>
   *   <li>sensor_update: Updates sensor readings and actuator states</li>
   *   <li>node_change: Handles node additions, updates, and removals</li>
   * </ul>
   *
   * <p>All updates are processed on the JavaFX Application Thread for thread safety.
   *
   * @param api The ClientApi instance to be used for server communication
   * @throws NullPointerException if api is null
   */
  public void setApi(ClientApi api) {
    if (api == null) {
      throw new NullPointerException("ClientApi cannot be null");
    }

    this.api = api;

    // Register listener for sensor updates
    this.api.onSensorUpdate(nodeState -> {
      Platform.runLater(() -> updateNodeData(nodeState));
    });

    // Register listener for node changes (add/update/remove)
    this.api.onNodeChange(nodeState -> {
      Platform.runLater(() -> handleNodeChange(nodeState));
    });

    System.out.println("✅ ClientApi configured with update listeners");
  }

  // ========== Server Data Synchronization ==========

  /**
   * Updates node data from a sensor_update message received from the server.
   *
   * <p>This method is called automatically when the ClientApi receives a sensor_update.
   * It updates the local Node model and triggers a UI refresh for the affected node card.
   *
   * <p><b>Thread-safety:</b> This method must run on the JavaFX Application Thread.
   *
   * @param nodeState The state object containing updated sensor and actuator data
   */
  private void updateNodeData(core.ClientState.NodeState nodeState) {
    Node node = nodes.get(nodeState.nodeId);
    if (node == null) {
      System.err.println("⚠️ Received update for unknown node: " + nodeState.nodeId);
      return;
    }

    // Build data map combining sensors and actuators
    Map<String, Object> data = new HashMap<>();

    // Add sensor readings (numeric values)
    nodeState.sensorValues.forEach(data::put);

    // Add actuator states (string values)
    data.put("fan", nodeState.fanStatus.get());
    data.put("water_pump", nodeState.pumpStatus.get());
    data.put("co2", nodeState.co2Status.get());
    data.put("window", nodeState.windowStatus.get());

    // Update the model
    node.updateFromServer(data, System.currentTimeMillis());

    // Refresh the UI card for this node
    refreshNodeCard(node);
  }

  /**
   * Handles node addition, update, or removal events from the server.
   *
   * <p>This method is called when the ClientApi receives a node_change message.
   * It creates new node cards, updates existing ones, or removes deleted nodes.
   *
   * <p><b>Operations:</b>
   * <ul>
   *   <li>If nodeState.name is present: Node was added or updated</li>
   *   <li>If nodeState.name is null: Node was removed</li>
   * </ul>
   *
   * <p><b>Update Behavior:</b>
   * When a node is updated (e.g., sensors/actuators changed via Edit Node Dialog),
   * this method updates the Node model and refreshes the UI card to reflect the changes.
   *
   * @param nodeState The state object containing node information
   */
  private void handleNodeChange(core.ClientState.NodeState nodeState) {
    if (nodeState.name != null) {
      // Node was added or updated
      Node node = nodes.computeIfAbsent(nodeState.nodeId, id -> {
        System.out.println("➕ Creating new node: " + nodeState.name);
        return new Node(
            id,
            nodeState.name,
            nodeState.location,
            nodeState.ip,
            new ArrayList<>(nodeState.sensors),
            new ArrayList<>(nodeState.actuators)
        );
      });

      // Update existing node configuration if it already exists
      if (nodes.containsKey(nodeState.nodeId)) {
        System.out.println("🔄 Updating existing node: " + nodeState.nodeId);
        System.out.println("   Previous sensors: " + node.getSensorTypes());
        System.out.println("   New sensors: " + nodeState.sensors);
        System.out.println("   Previous actuators: " + node.getActuatorTypes());
        System.out.println("   New actuators: " + nodeState.actuators);

        // Update sensor and actuator lists via dedicated methods
        node.updateSensorTypes(new ArrayList<>(nodeState.sensors));
        node.updateActuatorTypes(new ArrayList<>(nodeState.actuators));

       logActivity(node.getName(), "Node configuration updated");
      }

      // Create UI card if it doesn't exist yet (new node)
      if (!nodeCards.containsKey(nodeState.nodeId)) {
        createNodeCard(node);
        logActivity(node.getName(), "Node added to dashboard");
        // Refresh the UI card to reflect the changes
        refreshNodeCard(node);
      }
    } else {
      // Node was removed
      System.out.println("➖ Removing node: " + nodeState.nodeId);
      removeNodeCard(nodeState.nodeId);

      // Update node count in UI
      view.updateNodeCount(nodes.size());
    }
  }

  // ========== Node Management (CRUD Operations) ==========

  /**
   * Opens a dialog to create a new greenhouse node and synchronizes it with the server.
   *
   * <p>This method performs the following operations:
   * <ol>
   *   <li>Displays an {@link AddNodeDialog} for user input</li>
   *   <li>Validates user input</li>
   *   <li>Converts UI component selections to protocol format</li>
   *   <li>Sends node creation request to server via {@link ClientApi#createNode}</li>
   *   <li>Creates local Node representation with server-assigned ID</li>
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
      System.out.println("🔧 Creating node: " + nodeData.name);

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
          System.out.println("   📡 Added sensor: " + sensorType);
        } else {
          String actuatorType = mapActuatorName(component);
          serverNode.actuators.add(actuatorType);
          System.out.println("   🎛️  Added actuator: " + actuatorType);
        }
      }

      // Send to server
      api.createNode(serverNode).thenAccept(nodeId -> {
        System.out.println("✅ Server responded with nodeId: " + nodeId);

        Platform.runLater(() -> {
          String componentSummary = String.format(
              "%d sensor(s), %d actuator(s)",
              serverNode.sensors.size(),
              serverNode.actuators.size()
          );

          logActivity("System", String.format(
              "Node '%s' created (ID: %s). Location: %s. IP: %s. Components: %s",
              serverNode.name, nodeId, serverNode.location,
              serverNode.ip, componentSummary
          ));
        });
      }).exceptionally(ex -> {
        System.err.println("❌ Server error: " + ex.getMessage());
        ex.printStackTrace();

        Platform.runLater(() -> {
          logActivity("System", "Failed to create node: " + ex.getMessage());
        });
        return null;
      });
    });
  }

  /**
   * Opens a dialog to edit an existing node and sends update request to server.
   *
   * <p>This method: 1. Shows EditNodeDialog pre-populated with current node data 2. Converts user
   * input to protocol format (sensors/actuators) 3. Sends update_node request to server via
   * ClientApi 4. Logs the operation
   *
   * @param node The node to edit
   */
  public void editNode(Node node) {
    if (api == null) {
      logActivity("System", "Cannot edit node: API not initialized");
      return;
    }

    EditNodeDialog dialog = new EditNodeDialog(node);
    Optional<EditNodeDialog.NodeEditResult> result = dialog.showAndWait();

    result.ifPresent(editData -> {
      System.out.println("🔧 Editing node: " + node.getId());

      // Build patch map with only changed fields
      Map<String, Object> patch = new HashMap<>();

      // Check if name changed
      if (!editData.name.equals(node.getName())) {
        patch.put("name", editData.name);
        System.out.println("   Name: " + editData.name);
      }

      // Check if location changed
      if (!editData.location.equals(node.getLocation())) {
        patch.put("location", editData.location);
        System.out.println("   Location: " + editData.location);
      }

      // Check if IP changed
      if (!editData.ip.equals(node.getIpAddress())) {
        patch.put("ip", editData.ip);
        System.out.println("   IP: " + editData.ip);
      }

      // Convert components to sensors/actuators lists
      List<String> newSensors = new ArrayList<>();
      List<String> newActuators = new ArrayList<>();

      for (String component : editData.components) {
        if (component.endsWith("Sensor")) {
          // Remove " Sensor" suffix and convert to lowercase
          String sensorType = component.replace(" Sensor", "").toLowerCase();

          // Special case for PH (server expects "ph" not "ph")
          if (sensorType.equals("ph")) {
            newSensors.add("ph");
          } else {
            newSensors.add(sensorType);
          }

          System.out.println("   📡 Added sensor: " + sensorType);
        } else {
          String actuatorType = mapActuatorName(component);
          newActuators.add(actuatorType);
          System.out.println("   🎛️  Added actuator: " + actuatorType);
        }
      }

      // Always send sensors/actuators (full replacement)
      patch.put("sensors", newSensors);
      patch.put("actuators", newActuators);

      System.out.println("   New Sensors: " + newSensors);
      System.out.println("   New Actuators: " + newActuators);

      // Send to server
      api.updateNode(node.getId(), patch).thenRun(() -> {
        System.out.println("✅ Server confirmed node update");

        Platform.runLater(() -> {
          String changes = buildChangesSummary(node, editData, newSensors, newActuators);
          logActivity("System", String.format(
              "Node '%s' (ID: %s) updated. Changes: %s",
              node.getName(), node.getId(), changes
          ));
        });
      }).exceptionally(ex -> {
        System.err.println("❌ Server error: " + ex.getMessage());
        ex.printStackTrace();

        Platform.runLater(() -> {
          logActivity("System", "Failed to update node: " + ex.getMessage());
        });
        return null;
      });
    });
  }

  /**
   * Builds a human-readable summary of what changed in the node.
   */
  private String buildChangesSummary(Node node, EditNodeDialog.NodeEditResult editData,
      List<String> newSensors, List<String> newActuators) {
    List<String> changes = new ArrayList<>();

    if (!editData.name.equals(node.getName())) {
      changes.add("name");
    }
    if (!editData.location.equals(node.getLocation())) {
      changes.add("location");
    }
    if (!editData.ip.equals(node.getIpAddress())) {
      changes.add("IP");
    }

    // Check if sensors changed
    if (!new HashSet<>(newSensors).equals(new HashSet<>(node.getSensorTypes()))) {
      changes.add("sensors");
    }

    // Check if actuators changed
    if (!new HashSet<>(newActuators).equals(new HashSet<>(node.getActuatorTypes()))) {
      changes.add("actuators");
    }

    return changes.isEmpty() ? "none" : String.join(", ", changes);
  }

  /**
   * Maps UI component names to server protocol actuator names.
   *
   * <p>This method ensures consistent naming between the user interface and
   * the server protocol. It converts human-readable names to protocol-compliant identifiers.
   *
   * <p><b>Supported mappings:</b>
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
   * Removes a node from the dashboard and the server.
   *
   * <p>This method removes the node from the local cache, the UI, and
   * sends a delete request to the server. The deletion is logged for audit purposes.
   *
   * @param node The node to be deleted
   * @throws NullPointerException if node is null
   */
  public void deleteNode(Node node) {
    if (api == null) {
      logActivity("System", "Cannot delete node: API not initialized");
      return;
    }

    String nodeId = node.getId();
    String nodeName = node.getName();

    // Send delete request to server
    api.deleteNode(nodeId).thenRun(() -> {
      Platform.runLater(() -> {
        removeNodeCard(nodeId);
        logActivity("System", String.format(
            "Node '%s' (ID: %s) deleted from server",
            nodeName, nodeId
        ));
      });
    }).exceptionally(ex -> {
      Platform.runLater(() -> {
        logActivity("System", "Failed to delete node: " + ex.getMessage());
      });
      return null;
    });
  }

  // ========== UI Card Management ==========

  /**
   * Creates a visual card for a node and adds it to the dashboard.
   *
   * <p>This method generates a Material Design-inspired card with:
   * <ul>
   *   <li>Title bar with node name and action menu</li>
   *   <li>Node information: ID, IP address, and location</li>
   *   <li>Container for sensor visualizations</li>
   *   <li>Container for actuator controls</li>
   *   <li>Action menu with options to add components, edit, or delete</li>
   * </ul>
   *
   * <p>The card uses a clean, modern design with rounded corners, subtle shadows,
   * and a neutral color palette for optimal readability.
   *
   * @param node The node to visualize
   */
  private void createNodeCard(Node node) {
    VBox card = new VBox(10);
    card.getStyleClass().add("node-card");
    card.setStyle(
        "-fx-background-color: #ffffff;"
            + "-fx-background-radius: 12;"
            + "-fx-border-radius: 12;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.1, 0, 2);"
            + "-fx-padding: 0;"
            + "-fx-min-width: 280;"
            + "-fx-max-width: 280;"
    );

    // === Title Bar ===
    Label nodeTitle = new Label(node.getName());
    nodeTitle.setStyle(
        "-fx-font-size: 16px;"
            + "-fx-font-weight: bold;"
            + "-fx-text-fill: #202124;"
    );

    // Action menu (three-dot menu)
    MenuButton actionsMenu = new MenuButton("⋮");
    actionsMenu.setStyle(
        "-fx-background-color: transparent;"
            + "-fx-font-size: 18px;"
            + "-fx-text-fill: #5f6368;"
            + "-fx-cursor: hand;"
            + "-fx-padding: 2 8;"
    );

    MenuItem editNodeItem = new MenuItem("✏️ Edit Node");
    editNodeItem.setOnAction(e -> editNode(node));

    MenuItem deleteNodeItem = new MenuItem("🗑️ Delete Node");
    deleteNodeItem.setOnAction(e -> deleteNode(node));

    actionsMenu.getItems().addAll(editNodeItem, deleteNodeItem);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox titleBar = new HBox(10, nodeTitle, spacer, actionsMenu);
    titleBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    titleBar.setPadding(new Insets(10, 10, 5, 15));
    titleBar.setStyle(
        "-fx-background-color: #f1f3f4;"
            + "-fx-background-radius: 12 12 0 0;"
    );

    // === Node Info ===
    Label idLabel = new Label("ID: " + node.getId());
    idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #5f6368;");

    Label ipLabel = new Label("IP: " + node.getIpAddress());
    ipLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #5f6368;");

    Label locationLabel = new Label("📍 " + node.getLocation());
    locationLabel.setStyle(
        "-fx-font-size: 11px; -fx-text-fill: #5f6368; -fx-font-style: italic;"
    );

    VBox nodeInfoBox = new VBox(2, idLabel, ipLabel, locationLabel);
    nodeInfoBox.setPadding(new Insets(5, 15, 5, 15));

    // === Sensors Container ===
    VBox sensorsBox = new VBox(10);
    sensorsBox.setId("sensors-" + node.getId());
    sensorsBox.setPadding(new Insets(10, 15, 5, 15));

    // === Actuators Container ===
    VBox actuatorsBox = new VBox(10);
    actuatorsBox.setId("actuators-" + node.getId());
    actuatorsBox.setPadding(new Insets(5, 15, 10, 15));

    // === Assemble Card ===
    card.getChildren().addAll(titleBar, nodeInfoBox, sensorsBox, actuatorsBox);

    // Store reference and add to dashboard
    nodeCards.put(node.getId(), card);
    nodesPane.getChildren().add(card);

    // Initial render of sensors and actuators
    refreshNodeCard(node);

    // Update node count in UI
    view.updateNodeCount(nodes.size());

    System.out.println("✅ Node card created for: " + node.getName());
  }

  /**
   * Refreshes a node's card by redrawing sensors and actuators.
   *
   * <p>This method clears and recreates the sensor and actuator visualizations
   * based on the current data in the Node model. It's called whenever sensor_update messages are
   * received from the server.
   *
   * <p><b>Performance:</b> This method performs a full redraw of the card's content.
   * For better performance with frequent updates, consider using JavaFX property binding.
   *
   * @param node The node whose card should be refreshed
   */
  private void refreshNodeCard(Node node) {
    VBox card = nodeCards.get(node.getId());
    if (card == null) {
      System.err.println("⚠️ Cannot refresh card: not found for node " + node.getId());
      return;
    }

    VBox sensorsBox = (VBox) card.lookup("#sensors-" + node.getId());
    VBox actuatorsBox = (VBox) card.lookup("#actuators-" + node.getId());

    if (sensorsBox == null || actuatorsBox == null) {
      System.err.println("⚠️ Cannot refresh card: containers not found");
      return;
    }

    // === Redraw Sensors ===
    sensorsBox.getChildren().clear();

    if (node.getSensorTypes().contains("temperature")) {
      Double temp = node.getTemperature();
      if (temp != null) {
        sensorsBox.getChildren().add(
            TemperatureSensorView.create("Temperature", temp, "°C")
        );
      }
    }

    if (node.getSensorTypes().contains("humidity")) {
      Double humidity = node.getHumidity();
      if (humidity != null) {
        sensorsBox.getChildren().add(
            HumiditySensorView.create("Humidity", humidity, "%")
        );
      }
    }

    if (node.getSensorTypes().contains("light")) {
      Double light = node.getLight();
      if (light != null) {
        sensorsBox.getChildren().add(
            LightSensorView.create("Light", light, "lx")
        );
      }
    }

    if (node.getSensorTypes().contains("ph")) {
      Double ph = node.getPh();
      if (ph != null) {
        sensorsBox.getChildren().add(
            PhSensorView.create("pH Level", ph, "pH")
        );
      }
    }

    // === Redraw Actuators ===
    actuatorsBox.getChildren().clear();

    if (node.getActuatorTypes().contains("fan")) {
      actuatorsBox.getChildren().add(
          FanActuatorView.create(
              "Fan",
              node.getFanState(),
              on -> sendCommand(node.getId(), "fan", on)
          )
      );
    }

    if (node.getActuatorTypes().contains("water_pump")) {
      actuatorsBox.getChildren().add(
          WaterPumpActuatorView.create(
              "Water Pump",
              node.getPumpState(),
              on -> sendCommand(node.getId(), "water_pump", on)
          )
      );
    }

    if (node.getActuatorTypes().contains("co2")) {
      actuatorsBox.getChildren().add(
          Co2ActuatorView.create(
              "CO₂ Generator",
              node.getCo2State(),
              on -> sendCommand(node.getId(), "co2", on)
          )
      );
    }

    if (node.getActuatorTypes().contains("window")) {
      actuatorsBox.getChildren().add(
          WindowActuatorView.create(
              "Window",
              node.getWindowState(),
              level -> sendWindowCommand(node.getId(), level)
          )
      );

      // Update node count in UI
      view.updateNodeCount(nodes.size());
    }
  }

  /**
   * Removes a node card from the dashboard.
   *
   * @param nodeId The ID of the node whose card should be removed
   */
  private void removeNodeCard(String nodeId) {
    VBox card = nodeCards.remove(nodeId);
    if (card != null) {
      nodesPane.getChildren().remove(card);
    }
    nodes.remove(nodeId);

    System.out.println("🗑️ Node card removed: " + nodeId);

    // Update node count in UI
    view.updateNodeCount(nodes.size());
  }

  // ========== Command Sending ==========

  /**
   * Sends a control command to an actuator on the server.
   *
   * <p>This method constructs a command message and sends it via ClientApi.
   * The result is logged, and activity is recorded for the user.
   *
   * <p><b>Protocol:</b> Sends a "command" message with:
   * <pre>
   * {
   *   "type": "command",
   *   "nodeId": "node-1",
   *   "target": "fan",
   *   "action": "set",
   *   "params": {"on": true}
   * }
   * </pre>
   *
   * @param nodeId   ID of the node containing the actuator
   * @param actuator Name of the actuator (e.g., "fan", "water_pump")
   * @param on       Desired state: true for ON, false for OFF
   */
  private void sendCommand(String nodeId, String actuator, boolean on) {
    if (api == null) {
      logActivity("System", "Cannot send command: API not initialized");
      return;
    }

    Map<String, Object> params = Map.of("on", on);

    api.sendCommand(nodeId, actuator, "set", params)
        .thenRun(() -> {
          String action = on ? "ON" : "OFF";
          System.out.println("✅ Command sent: " + actuator + " = " + action);

          Platform.runLater(() -> {
            Node node = nodes.get(nodeId);
            String nodeName = node != null ? node.getName() : nodeId;
            logActivity(nodeName, String.format(
                "%s turned %s",
                capitalize(actuator.replace("_", " ")),
                action
            ));
          });
        })
        .exceptionally(ex -> {
          System.err.println("❌ Command failed: " + ex.getMessage());
          Platform.runLater(() -> {
            logActivity("System", "Failed to send command: " + ex.getMessage());
          });
          return null;
        });
  }

  /**
   * Sends a window control command with a specific level.
   *
   * <p>Window commands use a different parameter format than boolean actuators.
   *
   * @param nodeId ID of the node containing the window
   * @param level  Window level: "CLOSED", "HALF", or "OPEN"
   */
  private void sendWindowCommand(String nodeId, String level) {
    if (api == null) {
      logActivity("System", "Cannot send command: API not initialized");
      return;
    }

    Map<String, Object> params = Map.of("level", level);

    api.sendCommand(nodeId, "window", "set", params)
        .thenRun(() -> {
          System.out.println("✅ Command sent: window = " + level);

          Platform.runLater(() -> {
            Node node = nodes.get(nodeId);
            String nodeName = node != null ? node.getName() : nodeId;
            logActivity(nodeName, "Window set to " + level);
          });
        })
        .exceptionally(ex -> {
          System.err.println("❌ Command failed: " + ex.getMessage());
          Platform.runLater(() -> {
            logActivity("System", "Failed to send command: " + ex.getMessage());
          });
          return null;
        });
  }

  // ========== Data Refresh ==========

  /**
   * Manually refreshes the dashboard by updating the timestamp.
   *
   * <p>This method is called:
   * <ul>
   *   <li>When the user clicks the "Refresh" button</li>
   *   <li>Periodically by the auto-refresh timeline</li>
   * </ul>
   *
   * <p><b>Note:</b> Actual data updates come from sensor_update messages,
   * not from this method. This only updates the "Last update" timestamp.
   */
  public void manualRefresh() {
    //    if (lastUpdateLabel != null) {
    //      String currentTime = LocalDateTime.now().format(FULL_TIME_FORMATTER);
    //      lastUpdateLabel.setText("Date and time : " + currentTime);
    //    }
    if (refreshIntervalSeconds > 0) {
      logActivity("System", "Auto-refresh: Dashboard timestamp updated");
    }
  }
  /**
   * Saves the current activity log to a JSON file in the datalog folder OUTSIDE project
   */
  public void saveLogToJson() {
    try {
      StringBuilder jsonBuilder = new StringBuilder();
      jsonBuilder.append("[\n");

      List<String> entries = new ArrayList<>();

      // Extract data from log content
      for (javafx.scene.Node node : logContent.getChildren()) {
        if (node instanceof HBox) {
          HBox entryBox = (HBox) node;
          if (entryBox.getChildren().size() >= 2) {
            Label timeLabel = (Label) entryBox.getChildren().get(0);
            Label messageLabel = (Label) entryBox.getChildren().get(1);

            String fullText = messageLabel.getText();
            String[] parts = fullText.split(": ", 2);

            String source = parts.length > 0 ? parts[0] : "Unknown";
            String message = parts.length > 1 ? parts[1] : fullText;

            // Create JSON object manually
            String entry = String.format(
                "  {\n    \"timestamp\": \"%s\",\n    \"source\": \"%s\",\n    \"message\": \"%s\",\n    \"fullEntry\": \"%s\"\n  }",
                timeLabel.getText().replace("\"", "\\\""),
                source.replace("\"", "\\\""),
                message.replace("\"", "\\\""),
                fullText.replace("\"", "\\\"")
            );

            entries.add(entry);
          }
        }
      }

      // Reverse to have chronological order (oldest first)
      Collections.reverse(entries);
      jsonBuilder.append(String.join(",\n", entries));
      jsonBuilder.append("\n]");

      java.nio.file.Path projectDir = java.nio.file.Paths.get("").toAbsolutePath();
      java.nio.file.Path parentDir = projectDir.getParent(); // Go up one level
      java.nio.file.Path datalogDir;

      if (parentDir != null) {
        datalogDir = parentDir.resolve("datalog"); // ../datalog/
      } else {
        // Fallback: use system temp directory
        datalogDir = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"), "greenhouse_datalog");
      }

      // Create directory if it doesn't exist
      if (!java.nio.file.Files.exists(datalogDir)) {
        java.nio.file.Files.createDirectories(datalogDir);
        System.out.println("✅ Created datalog directory: " + datalogDir.toAbsolutePath());
      }

      // Generate filename with timestamp
      String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
      String filename = "activity_log_" + timestamp + ".json";

      // Full path to the file
      java.nio.file.Path filePath = datalogDir.resolve(filename);

      // Write to file
      java.nio.file.Files.write(filePath, jsonBuilder.toString().getBytes());

      // Log success with full path
      String fullPath = filePath.toAbsolutePath().toString();
      logActivity("System", "Log saved to: " + fullPath);

      System.out.println("✅ Log saved successfully to: " + fullPath);

    } catch (Exception ex) {
      System.err.println("❌ Error saving log: " + ex.getMessage());
      logActivity("System", "Failed to save log: " + ex.getMessage());
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
    if (seconds < 0) {
      throw new IllegalArgumentException("Refresh interval cannot be negative");
    }

    this.refreshIntervalSeconds = seconds;
    refreshTimeline.stop();

    if (seconds > 0) {
      refreshTimeline.getKeyFrames().setAll(
          new KeyFrame(Duration.seconds(seconds), e -> manualRefresh())
      );
      refreshTimeline.play();
      logActivity("System", "Auto-refresh started: every " + seconds + " second(s)");
    } else {
      logActivity("System", "Auto-refresh stopped");
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

  // ========== Activity Logging ==========

  /**
   * Logs an activity to the dashboard's activity log with timestamp and source.
   *
   * <p>Log entries are added to the top of the log view (most recent first) and include
   * a formatted timestamp. This method is thread-safe and can be called from any thread.
   *
   * <p><b>Format:</b>
   * <pre>
   * [HH:mm:ss] Source: Message
   * </pre>
   *
   * @param source  The source or origin of the activity (e.g., node name or "System")
   * @param message The descriptive message about the activity
   */
  public void logActivity(String source, String message) {
    Platform.runLater(() -> {
      if (logContent == null) {
        System.err.println("⚠️ Cannot log: logContent is null");
        return;
      }

      String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
      HBox entry = view.createLogEntry(currentTime, source, message);
      logContent.getChildren().add(0, entry);

      // Limit log size to prevent memory issues
      if (logContent.getChildren().size() > 100) {
        logContent.getChildren().remove(100, logContent.getChildren().size());
      }
    });
  }

  // ========== Session Management ==========

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
   * <p><b>Note:</b> Node data is preserved in memory and will be available
   * if the user logs back in during the same application session.
   */
  public void logout() {
    refreshTimeline.stop();
    logActivity("User", "User logged out");
    mainApp.showLoginScreen();

    System.out.println("👋 User logged out");
  }

  // ========== Getters ==========

  /**
   * Returns an unmodifiable view of all nodes managed by this controller.
   *
   * @return Unmodifiable collection of all greenhouse nodes in the dashboard
   */
  public Collection<Node> getNodes() {
    return Collections.unmodifiableCollection(nodes.values());
  }

  // ========== Utility Methods ==========

  /**
   * Capitalizes the first letter of a string.
   *
   * @param str The string to capitalize
   * @return The capitalized string, or empty string if input is null/empty
   */
  private String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return "";
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

}
