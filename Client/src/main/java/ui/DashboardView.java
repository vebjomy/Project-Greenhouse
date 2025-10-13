package ui;

import controller.DashboardController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;


public class DashboardView {
  private final BorderPane root = new BorderPane();
  private final DashboardController controller;
  private FlowPane nodesPane;
  private Label lastUpdateLabel;
  private Label userGreetingLabel;

  public DashboardView() {
    // Assuming DashboardController is implemented and handles the logic
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

  // --- LEFT SIDEBAR CREATION ---
  private VBox createLeftSidebar() {
    VBox sidebar = new VBox(10);
    sidebar.setPadding(new Insets(15, 0, 15, 0));
    sidebar.setPrefWidth(200);
    sidebar.setAlignment(Pos.TOP_CENTER);
    sidebar.setStyle("-fx-background-color: #2c3e50;"); // Darker background

    String buttonStyle = "-fx-min-width: 180px; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15; -fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-border-width: 0;";
    String buttonHoverStyle = "-fx-background-color: #34495e; -fx-cursor: hand;";
    String buttonActiveStyle = "-fx-background-color: #1a73e8; -fx-text-fill: white;";

    Button homeBtn = new Button("  Home");
    Button dashboardBtn = new Button("  Dashboard");
    Button usersBtn = new Button("  Users");
    Button statsBtn = new Button("  Statistics");

    Button logoutBtn = new Button("  Log out");

    // Add margin to push logout to the bottom visually
    VBox.setMargin(logoutBtn, new Insets(550, 0, 0, 0));

    // Apply initial style and hover effects
    Button[] navButtons = {homeBtn, dashboardBtn, usersBtn, statsBtn, logoutBtn};
    for (Button btn : navButtons) {
      btn.setStyle(buttonStyle);
      btn.setOnMouseEntered(e -> btn.setStyle(buttonStyle + buttonHoverStyle));
      btn.setOnMouseExited(e -> btn.setStyle(buttonStyle));
      // Highlight Dashboard as active
      if (btn == dashboardBtn) {
        btn.setStyle(buttonStyle + buttonActiveStyle);
        btn.setOnMouseExited(e -> btn.setStyle(buttonStyle + buttonActiveStyle)); // Keep active style on exit
      }
    }

    // Example action for logout (can be customized)
    logoutBtn.setOnAction(e -> System.out.println("User logged out."));

    sidebar.getChildren().addAll(homeBtn, dashboardBtn, usersBtn, statsBtn, logoutBtn);
    return sidebar;
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

    rightPanel.getChildren().addAll(header, scrollLog);
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

    Button refreshBtn = new Button("Refresh Data");
    refreshBtn.setOnAction(e -> controller.refreshData());
    refreshBtn.setStyle("-fx-background-color: #FABB05; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");

    lastUpdateLabel = new Label("Last update: -");
    lastUpdateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

    HBox controlsBar = new HBox(15, addNodeBtn, refreshBtn);
    controlsBar.setAlignment(Pos.CENTER_LEFT);

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
