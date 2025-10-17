package model;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class TemperatureSensor implements Sensor {
  private double currentTemperature;
  private final LinkedList<Double> history = new LinkedList<>();
  private static final int MAX_HISTORY_SIZE = 20; // Number of points for the graph
  private static final DecimalFormat df = new DecimalFormat("0.0");

  public TemperatureSensor() {
    // Initialize with some historical data
    for (int i = 0; i < MAX_HISTORY_SIZE; i++) {
      updateValue();
    }
  }

  public void updateValue() {
    // Simulate a temperature reading between -10.0 and 40.0
    this.currentTemperature = ThreadLocalRandom.current().nextDouble(-10.0, 40.0);
    history.addLast(this.currentTemperature);
    if (history.size() > MAX_HISTORY_SIZE) {
      history.removeFirst();
    }
  }

  @Override
  public String getSensorName() {
    return "Temperature";
  }

  @Override
  public String getReading() {
    return df.format(currentTemperature) + " °C";
  }

  @Override
  public double getNumericValue() {
    return currentTemperature;
  }

  @Override
  public Pane getVisualRepresentation() {
    updateValue(); // Get a new value each time it's drawn

    // --- Icon ---
    SVGPath thermometerIcon = new SVGPath();
    thermometerIcon.setContent("M19 8C20.11 8 21 8.9 21 10V16.76C21.61 17.31 22 18.11 22 19C22" +
        " 20.66 20.66 22 19 22C17.34 22 16 20.66 16 19C16 18.11 16.39 17.31 17 16.76V10C17" +
        " 8.9 17.9 8 19 8M19 9C18.45 9 18 9.45 18 10V11H20V10C20 9.45 19.55 9 19 9M5" +
        " 20V12H2L12 3L16.4 6.96C15.54 7.69 15 8.78 15 10V16C14.37 16.83 14 17.87 14 19L14.1 20H5Z");
    thermometerIcon.setScaleX(1.2);
    thermometerIcon.setScaleY(1.2);

    // --- Labels ---
    Label nameLabel = new Label(getSensorName());
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
    Label valueLabel = new Label(getReading());
    valueLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));

    // --- Color coding ---
    Color statusColor;
    if (currentTemperature < 5) {
      statusColor = Color.web("#4FC3F7"); // Light Blue
    } else if (currentTemperature > 28) {
      statusColor = Color.web("#E57373"); // Red
    } else {
      statusColor = Color.web("#81C784"); // Green
    }
    thermometerIcon.setFill(statusColor);
    valueLabel.setTextFill(statusColor);

    // --- Top section layout ---
    VBox titleBox = new VBox(-2, nameLabel, valueLabel);
    HBox topPane = new HBox(10, thermometerIcon, titleBox);
    topPane.setPadding(new Insets(0, 0, 10, 0));


    // --- Chart ---
    Canvas chartCanvas = new Canvas(198, 60);
    drawChart(chartCanvas, statusColor);

    // --- Final Layout ---
    VBox layout = new VBox(5, topPane, chartCanvas);
    layout.setPadding(new Insets(10));

    return layout;
  }

  private void drawChart(Canvas canvas, Color lineColor) {
    GraphicsContext gc = canvas.getGraphicsContext2D();

    double width = canvas.getWidth();
    double height = canvas.getHeight();

    // Clear canvas
    gc.clearRect(0, 0, width, height);

    // Draw background grid
    gc.setStroke(Color.web("#d1cbcb"));
    gc.setLineWidth(1);
    for(int i = 0; i < 5; i++){
      gc.strokeLine(0, (height/4)*i, width, (height/4)*i);
    }

    // --- Draw line chart ---
    gc.setStroke(lineColor);
    gc.setLineWidth(2.0);

    double minTemp = -15.0; // Chart min value
    double maxTemp = 45.0;  // Chart max value
    double range = maxTemp - minTemp;

    double xStep = width / (MAX_HISTORY_SIZE - 1);

    for (int i = 0; i < history.size() - 1; i++) {
      double y1 = height - ((history.get(i) - minTemp) / range * height);
      double y2 = height - ((history.get(i + 1) - minTemp) / range * height);
      gc.strokeLine(i * xStep, y1, (i + 1) * xStep, y2);
    }
  }

  @Override
  public double getCurrentValue() {
    return currentTemperature;
  }
}

