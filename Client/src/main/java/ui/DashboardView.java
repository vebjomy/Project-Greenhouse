package ui;

import App.MainApp;
import controller.DashboardController;
import core.ClientApi;
import core.CommandProcessor;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * DashboardView sets up the main dashboard UI for the Farm Management System. It includes a top
 * bar, left sidebar for navigation, center content area for nodes, a right panel for activity logs,
 * and a bottom panel for command input (Terminal Panel).
 *
 * @author Green House Control Team
 * @version 3.0
 */
public class DashboardView {

  private final BorderPane root = new BorderPane();
  private final ClientApi clientApi;
  private final DashboardController controller;
  private FlowPane nodesPane;
  private Label lastUpdateLabel;
  private Label userGreetingLabel;
  private VBox dashboardContent;
  private BorderPane usersContent;
  private BorderPane statisticsContent;

  // Field for log content (Activity Log)
  private VBox logContent;

  // Field for command output/history (Command Panel)
  private TextArea commandOutputArea;

  // Reference to mainApp
  private final MainApp mainApp;

  // Initialization flag
  private boolean isInitialized = false;

  /**
   * Constructor for DashboardView.
   *
   * @param mainApp Reference to the main application
   * @param api     ClientApi instance for server communication
   */
  public DashboardView(MainApp mainApp, ClientApi api) {
    this.mainApp = mainApp;
    this.clientApi = api;
    controller = new DashboardController(this, mainApp, api);

    setupUi();
  }

  /**
   * Sets up all UI components and layout. Does NOT perform data initialization - that's done in
   * initNetwork().
   */
  private void setupUi() {
    // --- Overall Styling ---
    root.setStyle("-fx-background-color: #ffffff;");
    root.getStylesheets().add(getClass().getResource("/client.css").toExternalForm());

    // --- 1. TOP BAR (Title + Greeting + Info) ---
    root.setTop(createTopBar());

    // --- 2. LEFT SIDEBAR (Navigation) ---
    root.setLeft(createLeftSidebar());

    // --- 3. CENTER CONTENT (Controls + Nodes) ---
    dashboardContent = createCenterContent();
    root.setCenter(dashboardContent);

    // --- 4. RIGHT PANEL (Status/Log) ---
    root.setRight(createRightPanel());

    // --- 5. BOTTOM COMMAND PANEL (Terminal) ---
    root.setBottom(createBottomPanel());

    // --- INITIALIZE CONTROLLER WITH UI COMPONENTS ---
    controller.setUiComponents(nodesPane, lastUpdateLabel,
        logContent, commandOutputArea);
  }

  // --- TOP BAR CREATION ---
  private HBox createTopBar() {
    Label titledash = new Label("Green House\nFarm Management System");
    titledash.getStyleClass().add("title-dash");

    // Greeting and additional information
    userGreetingLabel = new Label("Hello + !!username!!");
    Label additionalInfo = new Label("System Status:" + " NODE SIZE ");
    additionalInfo.setId("system-status-label");
    userGreetingLabel.setStyle("-fx-font-size: 31px; -fx-font-weight: 500;"
        + " -fx-text-fill: #333333;");
    additionalInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #00796b;");

    VBox userInfo = new VBox(5, userGreetingLabel, additionalInfo);
    userInfo.setAlignment(Pos.CENTER_LEFT);

    HBox topBar = new HBox(20, titledash, userInfo);
    topBar.setPadding(new Insets(20));
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0; "
        + "-fx-border-width: 0 0 1 0;");
    return topBar;
  }

  /**
   * Updates the system status label with the current number of nodes. Should be called whenever
   * nodes are added, removed, or on initial load.
   *
   * @param nodeCount The current number of nodes in the system
   */
  public void updateNodeCount(int nodeCount) {
    Platform.runLater(() -> {
      Label statusLabel = (Label) root.lookup("#system-status-label");
      if (statusLabel != null) {
        statusLabel.setText("System Status: " + nodeCount + " node(s)");
      }
    });
  }

  // --- LEFT SIDEBAR ---
  private VBox createLeftSidebar() {
    VBox sidebar = new VBox(10);
    sidebar.setPadding(new Insets(15, 0, 15, 0));
    sidebar.setPrefWidth(200);
    sidebar.setAlignment(Pos.TOP_CENTER);
    sidebar.setStyle("-fx-background-color: #2c3e50;");

    String buttonStyle = "-fx-min-width: 180px;"
        + "-fx-alignment: CENTER_LEFT; "
        + "-fx-padding: 10 15;"
        + "-fx-background-color: transparent;"
        + "-fx-text-fill: #ecf0f1;"
        + " -fx-font-size: 14px;"
        + "-fx-border-width: 0;";
    String buttonHoverStyle = "-fx-background-color: #34495e;"
        + "-fx-cursor: hand;";
    String buttonActiveStyle = "-fx-background-color: #1a73e8; -fx-text-fill: white;";

    Button dashboardBtn = new Button("  Dashboard");
    Button usersBtn = new Button("  Users");
    Button statsBtn = new Button("  Statistics");
    Button logoutBtn = new Button("  Log out");

    VBox.setMargin(logoutBtn, new Insets(550, 0, 0, 0));

    Button[] navButtons = {dashboardBtn, usersBtn, statsBtn, logoutBtn};

    // --- Logic for switching views ---
    dashboardBtn.setOnAction(e -> {
      root.setCenter(dashboardContent);
      setActiveButton(dashboardBtn, navButtons, buttonStyle, buttonActiveStyle);
    });

    usersBtn.setOnAction(e -> {
      if (usersContent == null) {
        usersContent = new UsersView(clientApi).getView();
      }
      root.setCenter(usersContent);
      setActiveButton(usersBtn, navButtons, buttonStyle, buttonActiveStyle);
    });

    statsBtn.setOnAction(e -> {
      // Create the view, passing the nodes from the controller
      statisticsContent = new StatisticsView(controller.getNodes()).getView();
      root.setCenter(statisticsContent);
      setActiveButton(statsBtn, navButtons, buttonStyle, buttonActiveStyle);
    });

    for (Button btn : navButtons) {
      btn.setStyle(buttonStyle);
      btn.setOnMouseEntered(e -> {
        if (!btn.getStyle().contains(buttonActiveStyle)) {
          btn.setStyle(buttonStyle + buttonHoverStyle);
        }
      });
      btn.setOnMouseExited(e -> {
        if (!btn.getStyle().contains(buttonActiveStyle)) {
          btn.setStyle(buttonStyle);
        }
      });
    }

    // --- Logout Logic ---
    logoutBtn.setOnAction(e -> controller.logout());

    setActiveButton(dashboardBtn, navButtons, buttonStyle, buttonActiveStyle);

    sidebar.getChildren().addAll(dashboardBtn, usersBtn, statsBtn, logoutBtn);
    return sidebar;
  }

  // --- Helper to change the active button ---
  private void setActiveButton(Button activeButton, Button[] allButtons,
      String baseStyle, String activeStyle) {
    for (Button btn : allButtons) {
      if (btn == activeButton) {
        btn.setStyle(baseStyle + activeStyle);
      } else {
        btn.setStyle(baseStyle);
      }
    }
  }

  // --- RIGHT PANEL CREATION ---
  private VBox createRightPanel() {
    VBox rightPanel = new VBox(10);
    rightPanel.setPadding(new Insets(10));
    rightPanel.setPrefWidth(250);
    rightPanel.setStyle("-fx-background-color: #f0f4f8; -fx-border-color: #e0e0e0; "
        + "-fx-border-width: 0 0 0 1;");

    Label header = new Label("Activity Log");
    header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;"
        + " -fx-text-fill: #333333;");

    // Initialize log content
    logContent = new VBox(5);
    logContent.setPadding(new Insets(5));

    ScrollPane scrollLog = new ScrollPane(logContent);
    scrollLog.setFitToWidth(true);
    scrollLog.setPrefHeight(VBox.USE_COMPUTED_SIZE);
    scrollLog.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

    VBox.setVgrow(scrollLog, Priority.ALWAYS);

    // Control buttons
    Button clearLogBtn = new Button("Clear Log");
    clearLogBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;"
        + " -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
    clearLogBtn.setOnAction(e -> logContent.getChildren().clear());

    Button saveLogBtn = new Button("Save Log");
    saveLogBtn.setStyle("-fx-background-color: #34A853; -fx-text-fill: white;"
        + " -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
    saveLogBtn.setOnAction(e -> controller.saveLogToJson());

    rightPanel.getChildren().addAll(header, scrollLog, clearLogBtn,saveLogBtn);
    rightPanel.setAlignment(Pos.TOP_LEFT);
    return rightPanel;
  }

  /**
   * Helper to create a log entry.
   */
  public HBox createLogEntry(String time, String source, String message) {
    Label timeLabel = new Label(time);
    timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999; -fx-font-weight: bold;");

    Label messageLabel = new Label(source + ": " + message);
    messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
    messageLabel.setWrapText(true);
    HBox.setHgrow(messageLabel, Priority.ALWAYS);

    HBox entry = new HBox(10, timeLabel, messageLabel);
    entry.setAlignment(Pos.CENTER_LEFT);
    entry.setPadding(new Insets(3, 0, 3, 0));
    return entry;
  }

  /**
   * Creates the bottom command panel (Terminal).
   */
  private VBox createBottomPanel() {
    // Pass mainApp to the CommandProcessor
    CommandProcessor commandProcessor = new CommandProcessor(clientApi, mainApp);

    VBox bottomPanel = new VBox(0);
    bottomPanel.setPrefHeight(150);
    bottomPanel.setStyle("-fx-background-color: #252526; -fx-border-color: #444444;"
        + " -fx-border-width: 1 0 0 0;");

    // Console Output Area (TextArea)
    commandOutputArea = new TextArea();
    commandOutputArea.setEditable(false);
    commandOutputArea.setFocusTraversable(false);
    commandOutputArea.setWrapText(true);
    commandOutputArea.setText("Console initialized. Type 'help' for available commands.\n");
    String serverAddress = mainApp.getServerAddress();
    if (serverAddress != null && !serverAddress.isEmpty()) {
      commandOutputArea.appendText(
          "Connected to: " + serverAddress + ":" + mainApp.getServerPort() + "\n");
    } else {
      commandOutputArea.appendText("Server: Not connected\n");
    }
    commandOutputArea.setStyle(
        "-fx-control-inner-background: #1e1e1e; "
            + "-fx-font-family: 'Consolas';"
            + "-fx-text-fill: #ffffff;"
            + "-fx-font-size: 12px;");
    VBox.setVgrow(commandOutputArea, Priority.ALWAYS);

    // Input Bar (HBox)
    Label promptLabel = new Label(">");
    promptLabel.setStyle(
        "-fx-font-size: 14px;"
            + "-fx-font-weight: bold;"
            + "-fx-text-fill: #f0f0f0;");

    TextField inputField = new TextField();
    inputField.setPromptText("Enter command...");
    HBox.setHgrow(inputField, Priority.ALWAYS);
    inputField.setStyle("-fx-background-color: #333333; -fx-text-fill: #ffffff; "
        + "-fx-border-color: #555555; -fx-border-width: 0;");

    Button sendButton = new Button("Send");
    sendButton.setStyle("-fx-background-color: #007bff;"
        + "-fx-text-fill: white; "
        + "-fx-font-weight: bold;"
        + "-fx-padding: 5 10;"
        + "-fx-background-radius: 3;"
        + "-fx-cursor: hand;");

    HBox inputBar = new HBox(5, promptLabel, inputField, sendButton);
    inputBar.setAlignment(Pos.CENTER_LEFT);
    inputBar.setPadding(new Insets(5, 10, 5, 10));

    // Command execution
    sendButton.setOnAction(e -> executeCommand(commandProcessor, inputField, commandOutputArea));
    inputField.setOnAction(e -> executeCommand(commandProcessor, inputField, commandOutputArea));

    bottomPanel.getChildren().addAll(commandOutputArea, inputBar);
    return bottomPanel;
  }

  private void executeCommand(CommandProcessor commandProcessor, TextField inputField,
      TextArea commandOutputArea) {
    String command = inputField.getText().trim();
    if (!command.isEmpty()) {
      commandOutputArea.appendText("> " + command + "\n");
      commandProcessor.execute(command).thenAccept(response -> {
        Platform.runLater(() -> commandOutputArea.appendText(response + "\n"));
      });
      inputField.clear();
    }
  }

  /**
   * Getter for log content.
   */
  public VBox getLogContent() {
    return logContent;
  }

  // --- CENTER CONTENT CREATION ---
  private VBox createCenterContent() {
    // --- Controls Bar (Add Node, Refresh Data) ---
    Button addNodeBtn = new Button("+ Add Node");
    addNodeBtn.setOnAction(e -> controller.addNode());
    addNodeBtn.setStyle(
        "-fx-background-color: #34A853;"
            + "-fx-text-fill: white;"
            + " -fx-font-weight: bold;"
            + "-fx-padding: 8 15;"
            + "-fx-background-radius: 5;"
            + "-fx-cursor: hand;"
    );

    // --- Auto-Refresh Controls ---
    Label intervalLabel = new Label("Auto-Refresh:");
    intervalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

    ComboBox<String> intervalComboBox = new ComboBox<>();
    intervalComboBox.getItems().addAll("2 sec", "10 sec", "20 sec", "60 sec", "Manual");
    intervalComboBox.getSelectionModel().select("Manual");
    intervalComboBox.setStyle(
        "-fx-font-size: 14px;"
            + "-fx-padding: 5 10;"
            + "-fx-background-color: #ffffff; "
            + "-fx-border-color: #d0d0d0;"
            + "-fx-border-radius: 5;"
            + "-fx-background-radius: 5;");

    Button toggleRefreshBtn = new Button("Start Auto-Refresh");
    toggleRefreshBtn.setStyle(
        "-fx-background-color: #1a73e8;"
            + "-fx-text-fill: white; "
            + "-fx-font-weight: bold;"
            + "-fx-padding: 8 15;"
            + "-fx-background-radius: 5;"
            + "-fx-cursor: hand;");

    // --- Logic for Auto-Refresh ---
    final String startText = "Start Auto-Refresh";
    final String stopText = "Stop Auto-Refresh";

    toggleRefreshBtn.setOnAction(e -> {
      if (controller.getRefreshIntervalSeconds() > 0) {
        controller.setAutoRefreshInterval(0);
        toggleRefreshBtn.setText(startText);
        toggleRefreshBtn.setStyle(
            "-fx-background-color: #1a73e8;"
                + "-fx-text-fill: white; "
                + "-fx-font-weight: bold;"
                + "-fx-padding: 8 15;"
                + "-fx-background-radius: 5;"
                + "-fx-cursor: hand;");
        intervalComboBox.getSelectionModel().select("Manual");
      } else {
        String selected = intervalComboBox.getSelectionModel().getSelectedItem();
        if (selected == null || "Manual".equals(selected)) {
          controller.manualRefresh();
          return;
        }

        long seconds = Long.parseLong(selected.split(" ")[0]);
        controller.setAutoRefreshInterval(seconds);
        toggleRefreshBtn.setText(stopText);
        toggleRefreshBtn.setStyle("-fx-background-color: #EA4335;"
            + "-fx-text-fill: white; "
            + "-fx-font-weight: bold;"
            + "-fx-padding: 8 15;"
            + "-fx-background-radius: 5;"
            + "-fx-cursor: hand;");
      }
    });

    intervalComboBox.setOnAction(e -> {
      String selected = intervalComboBox.getSelectionModel().getSelectedItem();
      if (selected == null || "Manual".equals(selected)) {
        controller.setAutoRefreshInterval(0);
        toggleRefreshBtn.setText(startText);
        toggleRefreshBtn.setStyle("-fx-background-color: #1a73e8;"
            + "-fx-text-fill: white;"
            + " -fx-font-weight: bold;"
            + "-fx-padding: 8 15;"
            + "-fx-background-radius: 5;"
            + "-fx-cursor: hand;");
      } else {
        if (controller.getRefreshIntervalSeconds() > 0) {
          long seconds = Long.parseLong(selected.split(" ")[0]);
          controller.setAutoRefreshInterval(seconds);
        }
      }
    });

    HBox refreshControls = new HBox(10, intervalLabel, intervalComboBox, toggleRefreshBtn);
    refreshControls.setAlignment(Pos.CENTER_LEFT);

    HBox controlsBar = new HBox(30, addNodeBtn);
    controlsBar.setAlignment(Pos.CENTER_LEFT);

    lastUpdateLabel = new Label("Time and Date");
    lastUpdateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox headerRow = new HBox(controlsBar, spacer, lastUpdateLabel);
    headerRow.setPadding(new Insets(0, 0, 10, 0));
    headerRow.setAlignment(Pos.CENTER_LEFT);

    // --- Node Display Area ---
    nodesPane = new FlowPane();
    nodesPane.setVgap(20);
    nodesPane.setHgap(20);
    nodesPane.setAlignment(Pos.TOP_LEFT);
    nodesPane.setPadding(new Insets(20));

    ScrollPane scrollPane = new ScrollPane(nodesPane);
    scrollPane.setFitToWidth(true);
    scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

    VBox centerContainer = new VBox(15, headerRow, scrollPane);
    centerContainer.setPadding(new Insets(20));
    centerContainer.setStyle("-fx-background-color: #ffffff;");
    return centerContainer;
  }

  /**
   * Sets the user greeting label with the provided username.
   *
   * @param username The username to display in the greeting
   */
  public void setUserGreeting(String username) {
    userGreetingLabel.setText("Hello, " + username + "!");
  }

  /**
   * GETTER FOR ROOT PANE.
   */
  public BorderPane getRoot() {
    return root;
  }

  /**
   * Initialize network and perform initial data load. Should be called after the scene is
   * displayed.
   */
  public void initNetwork(core.ClientApi api) {
    if (!isInitialized) {
      controller.setApi(api);

      // Now it's safe to call manualRefresh
      controller.manualRefresh();
      controller.logActivity("System", "Dashboard initialized successfully");

      // Update the node count display
      updateNodeCount(0); // Initial count is 0, will be updated on refresh

      isInitialized = true;
    }
  }
}
