package ui;

import controller.DashboardController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;


public class DashboardView {
  private final BorderPane root = new BorderPane();
  private final DashboardController controller;
  private FlowPane nodesPane;
  private Label lastUpdateLabel;
  private Label userGreetingLabel;
  private VBox dashboardContent;
  private BorderPane usersContent;
  private BorderPane statisticsContent;

  public DashboardView() {
    controller = new DashboardController(this);
    setupUI();
  }

  private void setupUI() {
    // --- Overall Styling ---
    root.setStyle("-fx-background-color: #ffffff;"); // White background for the main area

    root.getStylesheets().add(getClass().getResource("/client.css").toExternalForm());
    // --- 1. TOP BAR (Title + Greeting + Info) ---
    root.setTop(createTopBar());

    // --- 2. LEFT SIDEBAR (Navigation) ---
    root.setLeft(createLeftSidebar());

    // --- 3. CENTER CONTENT (Controls + Nodes) ---
    VBox centerContainer = createCenterContent();
    root.setCenter(centerContainer);

    // --- 4. RIGHT PANEL (Status/Log) ---
    root.setRight(createRightPanel());

    dashboardContent = createCenterContent();
    root.setCenter(dashboardContent);

    // --- INITIALIZE CONTROLLER ---
    // Pass components the controller needs to update
    controller.setUiComponents(nodesPane, lastUpdateLabel);
    controller.refreshData(); // Initial data load
  }

  // --- TOP BAR CREATION ---
  private HBox createTopBar() {
    Label titledash = new Label("Green House\nFarm Management System");
    titledash.getStyleClass().add("title-dash");

    // Greeting and additional information
    userGreetingLabel = new Label("Hello, John Doe!");
    Label additionalInfo = new Label("System Status: Operational (4 Nodes)");
    userGreetingLabel.setStyle("-fx-font-size: 21px; -fx-font-weight: 500; -fx-text-fill: #333333;");
    additionalInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #00796b;"); // Teal color for status
    VBox userInfo = new VBox(5, userGreetingLabel, additionalInfo);
    userInfo.setAlignment(Pos.CENTER_LEFT);

    HBox topBar = new HBox(20, titledash, userInfo);
    topBar.setPadding(new Insets(20));
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
    return topBar;
  }

  // --- MODIFIED LEFT SIDEBAR ---
  private VBox createLeftSidebar() {
    VBox sidebar = new VBox(10);
    sidebar.setPadding(new Insets(15, 0, 15, 0));
    sidebar.setPrefWidth(200);
    sidebar.setAlignment(Pos.TOP_CENTER);
    sidebar.setStyle("-fx-background-color: #2c3e50;");

    String buttonStyle = "-fx-min-width: 180px; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15; -fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-border-width: 0;";
    String buttonHoverStyle = "-fx-background-color: #34495e; -fx-cursor: hand;";
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
        usersContent = new UsersView().getView();
      }
      root.setCenter(usersContent);
      setActiveButton(usersBtn, navButtons, buttonStyle, buttonActiveStyle);
    });
    statsBtn.setOnAction(e -> {
      // Create the view, passing the nodes from the main controller
      statisticsContent = new StatisticsView(controller.getNodes()).getView();
      root.setCenter(statisticsContent);
      setActiveButton(statsBtn, navButtons, buttonStyle, buttonActiveStyle);
    });
    for (Button btn : navButtons) {
      btn.setStyle(buttonStyle);
      btn.setOnMouseEntered(e -> { if (!btn.getStyle().contains(buttonActiveStyle)) btn.setStyle(buttonStyle + buttonHoverStyle); });
      btn.setOnMouseExited(e -> { if (!btn.getStyle().contains(buttonActiveStyle)) btn.setStyle(buttonStyle); });
    }
//    logoutBtn.setOnAction(e -> mainApp.showLoginScreen());

    // --- Set Dashboard as the active button by default ---
    setActiveButton(dashboardBtn, navButtons, buttonStyle, buttonActiveStyle);

    sidebar.getChildren().addAll(dashboardBtn, usersBtn, statsBtn, logoutBtn);
    return sidebar;
  }
  // --- Helper to change the active button ---
  private void setActiveButton(Button activeButton, Button[] allButtons, String baseStyle, String activeStyle) {
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
    rightPanel.setStyle("-fx-background-color: #f0f4f8; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 0 1;");

    Label header = new Label("Activity Log");
    header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

    VBox logContent = new VBox(5);
    logContent.getChildren().addAll(
        createLogEntry("10:05", "Node 3", "Low Humidity Warning"),
        createLogEntry("10:00", "System", "Data refresh complete"),
        createLogEntry("09:45", "Node 1", "Offline for 5 minutes"),
        createLogEntry("09:30", "System", "Scheduled check OK")
    );

    ScrollPane scrollLog = new ScrollPane(logContent);
    scrollLog.setFitToWidth(true);
    scrollLog.setPrefHeight(VBox.USE_COMPUTED_SIZE);
    scrollLog.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

    // create a delete and update button for the log
    Button clearLogBtn = new Button("Clear Log");
    clearLogBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
    clearLogBtn.setOnAction(e -> logContent.getChildren().clear());

    Button updateLogBtn = new Button("Update Log");
    updateLogBtn.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
    updateLogBtn.setOnAction(e -> logContent.getChildren().add(createLogEntry("Now", "System",
        "Log updated manually")));

    Button saveLogBtn = new Button("Save Log");
    saveLogBtn.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
    saveLogBtn.setOnAction(e -> logContent.getChildren().add(createLogEntry("Now", "System",
        "Save log ")));



    rightPanel.getChildren().addAll(header, scrollLog,clearLogBtn, updateLogBtn, saveLogBtn);
    rightPanel.setAlignment(Pos.TOP_LEFT);

    return rightPanel;
  }

  private HBox createLogEntry(String time, String source, String message) {
    Label timeLabel = new Label(time);
    timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999; -fx-font-weight: bold;");

    Label messageLabel = new Label(source + ": " + message);
    messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

    HBox entry = new HBox(10, timeLabel, messageLabel);
    entry.setAlignment(Pos.CENTER_LEFT);
    entry.setPadding(new Insets(3, 0, 3, 0));
    return entry;
  }
  // --- CENTER CONTENT CREATION ---
  private VBox createCenterContent() {
    // --- Controls Bar (Add Node, Refresh Data) ---
    Button addNodeBtn = new Button("+ Add Node");
    addNodeBtn.setOnAction(e -> controller.addNode());
    addNodeBtn.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");

    // --- New Auto-Refresh Controls ---
    Label intervalLabel = new Label("Auto-Refresh:");
    intervalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

    // ComboBox for interval selection with custom styling
    javafx.scene.control.ComboBox<String> intervalComboBox = new javafx.scene.control.ComboBox<>();
    intervalComboBox.getItems().addAll("2 sec", "10 sec", "20 sec", "60 sec" , "Manual");
    intervalComboBox.getSelectionModel().select("Manual"); // Default selection
    intervalComboBox.setStyle("-fx-font-size: 14px; -fx-padding: 5 10; -fx-background-color: #ffffff; " +
        "-fx-border-color: #d0d0d0; -fx-border-radius: 5; -fx-background-radius: 5;");

    Button toggleRefreshBtn = new Button("Start Auto-Refresh");
    toggleRefreshBtn.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");

    // --- Logic for Auto-Refresh ---
    final String startText = "Start Auto-Refresh";
    final String stopText = "Stop Auto-Refresh";

    toggleRefreshBtn.setOnAction(e -> {
      if (controller.getRefreshIntervalSeconds() > 0) {
        // Currently refreshing, so stop it
        controller.setAutoRefreshInterval(0);
        toggleRefreshBtn.setText(startText);
        toggleRefreshBtn.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        intervalComboBox.getSelectionModel().select("Manual"); // Visually reset to manual
      } else {
        // Not refreshing, so start it
        String selected = intervalComboBox.getSelectionModel().getSelectedItem();
        if (selected == null || "Manual".equals(selected)) {
          // If "Manual" is selected, just perform a single refresh
          controller.refreshData();
          return;
        }

        long seconds = Long.parseLong(selected.split(" ")[0]);
        controller.setAutoRefreshInterval(seconds);
        toggleRefreshBtn.setText(stopText);
        toggleRefreshBtn.setStyle("-fx-background-color: #EA4335; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
      }
    });

    intervalComboBox.setOnAction(e -> {
      String selected = intervalComboBox.getSelectionModel().getSelectedItem();
      if (selected == null || "Manual".equals(selected)) {
        // If manual is selected, stop the auto-refresh and reset button text
        controller.setAutoRefreshInterval(0);
        toggleRefreshBtn.setText(startText);
        toggleRefreshBtn.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
      } else {
        // If a time is selected, the user probably wants to start it right away
        // The button will handle starting/stopping
        if (controller.getRefreshIntervalSeconds() > 0) {
          // If it was already running, change the interval immediately
          long seconds = Long.parseLong(selected.split(" ")[0]);
          controller.setAutoRefreshInterval(seconds);
        }
      }
    });

    HBox refreshControls = new HBox(10, intervalLabel, intervalComboBox, toggleRefreshBtn);
    refreshControls.setAlignment(Pos.CENTER_LEFT);

    // Combine Add Node button and Refresh Controls
    HBox controlsBar = new HBox(30, addNodeBtn, refreshControls);
    controlsBar.setAlignment(Pos.CENTER_LEFT);

    lastUpdateLabel = new Label("Last update: -");
    lastUpdateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

    // Combine controls and status on one line
    HBox headerRow = new HBox(30, controlsBar, lastUpdateLabel);
    headerRow.setPadding(new Insets(0, 0, 10, 0));
    headerRow.setAlignment(Pos.CENTER_LEFT);

    // --- Node Display Area ---
    nodesPane = new FlowPane();
    nodesPane.setVgap(20);
    nodesPane.setHgap(20);
    nodesPane.setAlignment(Pos.TOP_LEFT);

    // Make the FlowPane scrollable
    ScrollPane scrollPane = new ScrollPane(nodesPane);
    scrollPane.setFitToWidth(true);
    scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

    // --- Final Center Container ---
    VBox centerContainer = new VBox(15, headerRow, scrollPane);
    centerContainer.setPadding(new Insets(20));
    centerContainer.setStyle("-fx-background-color: #ffffff;");

    return centerContainer;
  }
  public BorderPane getRoot() {
    return root;
  }
}
