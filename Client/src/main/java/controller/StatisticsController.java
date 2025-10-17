package controller;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import model.*;
import ui.StatisticsView;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Controller for the StatisticsView. It calculates and populates the
 * statistics based on the current state of the nodes and sensors.
 */
public class StatisticsController {
  private final StatisticsView view;
  private final List<Node> nodes;

  public StatisticsController(StatisticsView view, List<Node> nodes) {
    this.view = view;
    this.nodes = nodes;
  }

  /**
   * Calculates all statistics and updates the UI components.
   */
  public void updateStatistics() {
    if (nodes == null || nodes.isEmpty()) {
      return; // Nothing to show
    }

    // --- Update Summary Labels ---
    view.getTotalNodesLabel().setText("Total Nodes: " + nodes.size());

    double avgTemp = calculateAverageForSensor(TemperatureSensor.class);
    view.getAvgTempLabel().setText(String.format("Avg. Temperature: %.1f °C", avgTemp));

    double avgHumidity = calculateAverageForSensor(HumiditySensor.class);
    view.getAvgHumidityLabel().setText(String.format("Avg. Humidity: %.1f %%", avgHumidity));

    double avgLight = calculateAverageForSensor(LightSensor.class);
    view.getAvgLightLabel().setText(String.format("Avg. Light Level: %.1f lx", avgLight));

    double avgPh = calculateAverageForSensor(PHSensor.class);
    view.getAvgPhLevelLabel().setText(String.format("Avg. pH Level: %.2f", avgPh));

    // --- Update ALL Bar Charts ---
    updateAllCharts();

    applyBarColors();
  }

  private void applyBarColors() {
    setChartBarColor(view.getTempBarChart(), "#ff6f61");
    setChartBarColor(view.getHumidityBarChart(), "#4db8ff");
    setChartBarColor(view.getLightBarChart(), "#ffd54f");
    setChartBarColor(view.getPhBarChart(), "#81c784");
  }

  private void setChartBarColor(BarChart<String, Number> chart, String color) {
    chart.lookupAll(".chart-bar").forEach(bar ->
        bar.setStyle("-fx-bar-fill: " + color + ";")
    );

  }

  /**
   * A generic method to calculate the average value for a specific sensor type
   * across all nodes.
   *
   * @param sensorClass The class of the sensor type to average.
   * @param <T> The type of the sensor, must extend Sensor.
   * @return The calculated average, or 0.0 if no such sensors are found.
   */
  private <T extends Sensor> double calculateAverageForSensor(Class<T> sensorClass) {
    OptionalDouble average = nodes.stream()
        .flatMap(node -> node.getSensors().stream()) // Get all sensors from all nodes
        .filter(sensorClass::isInstance)             // Filter for the specific sensor type
        .mapToDouble(Sensor::getCurrentValue) // Get the current value of each sensor
        .average();                                   // Calculate the average

    return average.orElse(0.0);
  }

  // ------------------------------------------------------------------
  // ------------------------------------------------------------------

  /**
   * Calls the generic chart update method for all sensor types.
   */
  private void updateAllCharts() {
    // 1.
    updateChartForSensor(TemperatureSensor.class, view.getTempBarChart());

    // 2.
    updateChartForSensor(HumiditySensor.class, view.getHumidityBarChart());

    // 3.
    updateChartForSensor(LightSensor.class, view.getLightBarChart());

    // 4. pH
    updateChartForSensor(PHSensor.class, view.getPhBarChart());
  }


  /**
   * A generic method to update a BarChart for a specific sensor type.
   *
   * @param sensorClass The class of the sensor type to display.
   * @param chart The BarChart to update.
   * @param <T> The type of the sensor, must extend Sensor.
   */
  private <T extends Sensor> void updateChartForSensor(Class<T> sensorClass, BarChart<String, Number> chart) {


    XYChart.Series<String, Number> series = new XYChart.Series<>();

    for (Node node : nodes) {

      node.getSensors().stream()
          .filter(sensorClass::isInstance)
          .findFirst()
          .ifPresent(sensor -> {

            series.getData().add(new XYChart.Data<>(node.getName(), sensor.getCurrentValue()));
          });
    }


    chart.getData().clear();
    chart.getData().add(series);
  }


}