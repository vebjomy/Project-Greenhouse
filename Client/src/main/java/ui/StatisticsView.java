package ui;

import controller.StatisticsController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Node;

import java.util.Collection;

/**
 * The View (UI) for the statistics page.
 * Displays charts and summary data about greenhouse nodes.
 */
public class StatisticsView {
  private final BorderPane view;
  private final StatisticsController controller;

  // --- UI Components ---
  private final Label totalNodesLabel = new Label("Total Nodes: -");
  private final Label avgTempLabel = new Label("Avg. Temperature: - °C");
  private final Label avgHumidityLabel = new Label("Avg. Humidity: - %");
  private final Label avgLightLabel = new Label("Avg. Light Level: - lx");
  private final Label avgPhLevelLabel = new Label("Avg. pH Level: -");

  // --- Charts ---
  private final BarChart<String, Number> tempBarChart;
  private final BarChart<String, Number> humidityBarChart;
  private final BarChart<String, Number> lightBarChart;
  private final BarChart<String, Number> phBarChart;

  public StatisticsView(Collection<Node> nodes) {
    // === Chart Axes ===
    CategoryAxis xAxisTemp = new CategoryAxis();
    xAxisTemp.setLabel("Node Name");
    NumberAxis yAxisTemp = new NumberAxis();
    yAxisTemp.setLabel("Temperature (°C)");
    tempBarChart = new BarChart<>(xAxisTemp, yAxisTemp);
    tempBarChart.setTitle("Current Temperature by Node");
    tempBarChart.setLegendVisible(false);
    tempBarChart.setPrefHeight(300);

    CategoryAxis xAxisHumidity = new CategoryAxis();
    xAxisHumidity.setLabel("Node Name");
    NumberAxis yAxisHumidity = new NumberAxis();
    yAxisHumidity.setLabel("Humidity (%)");
    humidityBarChart = new BarChart<>(xAxisHumidity, yAxisHumidity);
    humidityBarChart.setTitle("Current Humidity by Node");
    humidityBarChart.setLegendVisible(false);
    humidityBarChart.setPrefHeight(300);

    CategoryAxis xAxisLight = new CategoryAxis();
    xAxisLight.setLabel("Node Name");
    NumberAxis yAxisLight = new NumberAxis();
    yAxisLight.setLabel("Light Level (lx)");
    lightBarChart = new BarChart<>(xAxisLight, yAxisLight);
    lightBarChart.setTitle("Current Light Level by Node");
    lightBarChart.setLegendVisible(false);
    lightBarChart.setPrefHeight(300);

    CategoryAxis xAxisPh = new CategoryAxis();
    xAxisPh.setLabel("Node Name");
    NumberAxis yAxisPh = new NumberAxis();
    yAxisPh.setLabel("pH Level");
    phBarChart = new BarChart<>(xAxisPh, yAxisPh);
    phBarChart.setTitle("Current pH Level by Node");
    phBarChart.setLegendVisible(false);
    phBarChart.setPrefHeight(300);

    // === Controller ===
    controller = new StatisticsController(this, nodes);

    // === Layout ===
    view = createStatisticsLayout();

    // === Initial Update ===
    controller.updateStatistics();
  }

  private BorderPane createStatisticsLayout() {
    BorderPane layout = new BorderPane();
    layout.setPadding(new Insets(20));
    layout.setStyle("-fx-background-color: #f4f7fa;");

    Label titleLabel = new Label("System Statistics");
    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

    // === Summary Panel ===
    HBox summaryBox = new HBox(30, totalNodesLabel, avgTempLabel, avgHumidityLabel, avgLightLabel, avgPhLevelLabel);
    summaryBox.setAlignment(Pos.CENTER_LEFT);
    summaryBox.setPadding(new Insets(15));
    summaryBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d0d0d0; -fx-border-radius: 8; -fx-background-radius: 8;");

    String summaryStyle = "-fx-font-size: 16px; -fx-font-weight: 500;";
    totalNodesLabel.setStyle(summaryStyle);
    avgTempLabel.setStyle(summaryStyle);
    avgHumidityLabel.setStyle(summaryStyle);
    avgLightLabel.setStyle(summaryStyle);
    avgPhLevelLabel.setStyle(summaryStyle);

    // === Charts Container ===
    VBox chartsBox = new VBox(40);
    chartsBox.setPadding(new Insets(30, 0, 30, 0));
    chartsBox.getChildren().addAll(tempBarChart, humidityBarChart, lightBarChart, phBarChart);

    ScrollPane scrollPane = new ScrollPane(chartsBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setStyle("-fx-background-color: transparent;");

    VBox mainContent = new VBox(20, titleLabel, summaryBox, scrollPane);
    layout.setCenter(mainContent);

    return layout;
  }

  // === Getters for controller ===
  public Label getTotalNodesLabel() { return totalNodesLabel; }
  public Label getAvgTempLabel() { return avgTempLabel; }
  public Label getAvgHumidityLabel() { return avgHumidityLabel; }
  public Label getAvgLightLabel() { return avgLightLabel; }
  public Label getAvgPhLevelLabel() { return avgPhLevelLabel; }

  public BarChart<String, Number> getTempBarChart() { return tempBarChart; }
  public BarChart<String, Number> getHumidityBarChart() { return humidityBarChart; }
  public BarChart<String, Number> getLightBarChart() { return lightBarChart; }
  public BarChart<String, Number> getPhBarChart() { return phBarChart; }

  public BorderPane getView() { return view; }
}

