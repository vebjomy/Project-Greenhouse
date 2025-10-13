package ui;

import controller.DashboardController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DashboardView {
  private final BorderPane root = new BorderPane();
  private final DashboardController controller;

  public DashboardView() {
    controller = new DashboardController(this);
    setupUI();
  }

  private void setupUI() {
    // --- TOP BAR ---
    Label title = new Label("Farm Dashboard");
    title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a73e8;");

    Button addNodeBtn = new Button("+ Add Node");
    addNodeBtn.setOnAction(e -> controller.addNode());
    addNodeBtn.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");

    Button refreshBtn = new Button("Refresh Data");
    refreshBtn.setOnAction(e -> controller.refreshData());
    refreshBtn.setStyle("-fx-background-color: #FABB05; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");

    HBox topControls = new HBox(15, addNodeBtn, refreshBtn);
    topControls.setAlignment(Pos.CENTER_RIGHT);

    HBox topBar = new HBox(50, title, topControls);
    topBar.setPadding(new Insets(20));
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

    root.setTop(topBar);

    // --- CENTER AREA for Nodes ---
    FlowPane nodesPane = new FlowPane();
    nodesPane.setPadding(new Insets(20));
    nodesPane.setVgap(20);
    nodesPane.setHgap(20);
    nodesPane.setAlignment(Pos.TOP_LEFT);

    // Make the FlowPane scrollable
    ScrollPane scrollPane = new ScrollPane(nodesPane);
    scrollPane.setFitToWidth(true);
    scrollPane.setStyle("-fx-background-color: transparent;");

    root.setCenter(scrollPane);


    // --- BOTTOM BAR ---
    Label lastUpdateLabel = new Label("Last update: -");
    lastUpdateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

    VBox bottomBar = new VBox(lastUpdateLabel);
    bottomBar.setPadding(new Insets(10, 20, 10, 20));
    bottomBar.setAlignment(Pos.CENTER_RIGHT);
    bottomBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
    root.setBottom(bottomBar);


    // --- INITIALIZE CONTROLLER ---
    controller.setUiComponents(nodesPane, lastUpdateLabel);
    controller.refreshData(); // Initial data load
  }

  public BorderPane getRoot() {
    return root;
  }
}
