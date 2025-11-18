package controller;

import java.util.Collection;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import model.Node;
import ui.StatisticsView;


/**
 * Controller for the StatisticsView. It calculates and populates the
 * statistics based on the current state of the nodes and sensors.
 *
 * @version 2.0 - Updated to work with new Node model
 */
public class StatisticsController {
  private final StatisticsView view;
  private final Collection<Node> nodes;

  /**
   * Constructor for StatisticsController.
   *
   * @param view  The StatisticsView instance to control.
   * @param nodes The collection of nodes containing sensors to analyze.
   */
  public StatisticsController(StatisticsView view, Collection<Node> nodes) {
    this.view = view;
    this.nodes = nodes;
  }

  /**
   * Calculates all statistics and updates the UI components.
   * This includes average values for each sensor type and updating bar charts.
   */
  public void updateStatistics() {
    if (nodes == null || nodes.isEmpty()) {
      return; // Nothing to show
    }

    // --- Update Summary Labels ---
    view.getTotalNodesLabel().setText("Total Nodes: " + nodes.size());

    double avgTemp = calculateAverageTemperature();
    view.getAvgTempLabel().setText(String.format("Avg. Temperature: %.1f °C", avgTemp));

    double avgHumidity = calculateAverageHumidity();
    view.getAvgHumidityLabel().setText(String.format("Avg. Humidity: %.1f %%", avgHumidity));

    double avgLight = calculateAverageLight();
    view.getAvgLightLabel().setText(String.format("Avg. Light Level: %.1f lx", avgLight));

    double avgPh = calculateAveragePh();
    view.getAvgPhLevelLabel().setText(String.format("Avg. pH Level: %.2f", avgPh));

    // --- Update ALL Bar Charts ---
    updateAllCharts();
    applyBarColors();
  }

  /**
   * Applies distinct colors to each bar chart for better visual distinction.
   */
  private void applyBarColors() {
    setChartBarColor(view.getTempBarChart(), "#ff6f61");
    setChartBarColor(view.getHumidityBarChart(), "#4db8ff");
    setChartBarColor(view.getLightBarChart(), "#ffd54f");
    setChartBarColor(view.getPhBarChart(), "#81c784");
  }

  /**
   * Sets the color of the bars in a given BarChart.
   *
   * @param chart The BarChart to style.
   * @param color The color to apply to the bars (in hex format).
   */
  private void setChartBarColor(BarChart<String, Number> chart, String color) {
    chart.lookupAll(".chart-bar").forEach(bar ->
            bar.setStyle("-fx-bar-fill: " + color + ";")
    );
  }

  /**
   * Calculates the average temperature across all nodes.
   */
  private double calculateAverageTemperature() {
    return nodes.stream()
            .map(Node::getTemperature)
            .filter(temp -> temp != null)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
  }

  /**
   * Calculates the average humidity across all nodes.
   */
  private double calculateAverageHumidity() {
    return nodes.stream()
            .map(Node::getHumidity)
            .filter(humidity -> humidity != null)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
  }

  /**
   * Calculates the average light level across all nodes.
   */
  private double calculateAverageLight() {
    return nodes.stream()
            .map(Node::getLight)
            .filter(light -> light != null)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
  }

  /**
   * Calculates the average pH across all nodes.
   */
  private double calculateAveragePh() {
    return nodes.stream()
            .map(Node::getPh)
            .filter(ph -> ph != null)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
  }

  /**
   * Updates all bar charts with current node data.
   */
  private void updateAllCharts() {
    updateTemperatureChart();
    updateHumidityChart();
    updateLightChart();
    updatePhChart();
  }

  /**
   * Updates the temperature bar chart.
   */
  private void updateTemperatureChart() {
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Temperature");

    for (Node node : nodes) {
      Double temp = node.getTemperature();
      if (temp != null) {
        series.getData().add(new XYChart.Data<>(node.getName(), temp));
      }
    }

    view.getTempBarChart().getData().clear();
    view.getTempBarChart().getData().add(series);
  }

  /**
   * Updates the humidity bar chart.
   */
  private void updateHumidityChart() {
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Humidity");

    for (Node node : nodes) {
      Double humidity = node.getHumidity();
      if (humidity != null) {
        series.getData().add(new XYChart.Data<>(node.getName(), humidity));
      }
    }

    view.getHumidityBarChart().getData().clear();
    view.getHumidityBarChart().getData().add(series);
  }

  /**
   * Updates the light level bar chart.
   */
  private void updateLightChart() {
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Light");

    for (Node node : nodes) {
      Double light = node.getLight();
      if (light != null) {
        series.getData().add(new XYChart.Data<>(node.getName(), light));
      }
    }

    view.getLightBarChart().getData().clear();
    view.getLightBarChart().getData().add(series);
  }

  /**
   * Updates the pH bar chart.
   */
  private void updatePhChart() {
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("pH Level");

    for (Node node : nodes) {
      Double ph = node.getPh();
      if (ph != null) {
        series.getData().add(new XYChart.Data<>(node.getName(), ph));
      }
    }

    view.getPhBarChart().getData().clear();
    view.getPhBarChart().getData().add(series);
  }
}