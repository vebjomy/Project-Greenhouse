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


/**
 * PHSensor simulates a pH sensor that provides pH level readings.
 * It maintains a history of readings and provides a visual representation
 * of the current pH level along with a trend chart.
 * @version 1.0
 */

public class PHSensor implements Sensor {
  private double currentPH;
  private final LinkedList<Double> history = new LinkedList<>();
  private static final int MAX_HISTORY_SIZE = 20;
  //
  private static final DecimalFormat df = new DecimalFormat("0.00");

  //
  private static final double MIN_PH = 0.0;
  private static final double MAX_PH = 14.0;
  private static final double OPTIMAL_LOW = 6.5;
  private static final double OPTIMAL_HIGH = 8.5;

  public PHSensor() {
    //
    for (int i = 0; i < MAX_HISTORY_SIZE; i++) {
      //
      this.currentPH = ThreadLocalRandom.current().nextDouble(6.0, 9.0);
      history.addLast(this.currentPH);
    }
  }

  /**
   * Simulates updating the pH value with a random fluctuation.
   * The new value is constrained within the valid pH range (0.0 to 14.0).
   */

  public void updateValue() {
    double nextPH = currentPH + ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
    this.currentPH = Math.max(MIN_PH, Math.min(MAX_PH, nextPH));

    history.addLast(this.currentPH);
    if (history.size() > MAX_HISTORY_SIZE) {
      history.removeFirst();
    }
  }

  /**
   * Returns the name of the sensor.
   * @return the sensor name "pH"
   */

  @Override
  public String getSensorName() {
    return "pH";
  }

  @Override
  public String getReading() {
    return df.format(currentPH) + " Ph";
  }

  @Override
  public double getNumericValue() {
    return currentPH;
  }

  @Override
  public Pane getVisualRepresentation() {
    updateValue();

    // --- icon ---
    SVGPath phIcon = new SVGPath();
    phIcon.setContent("M12 2C6.5 2 2 6.5 2 12C2 17.5 12 22 12 22S22 17.5 22 12C22 6.5 17.5 2 12 2M12 4C16.4 4 20 7.6 20 12C20 14.8 17.4 17.6 12 20C6.6 17.6 4 14.8 4 12C4 7.6 7.6 4 12 4Z");
    phIcon.setScaleX(1.2);
    phIcon.setScaleY(1.2);

    // --- Labels ---
    Label nameLabel = new Label(getSensorName());
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
    Label valueLabel = new Label(getReading());
    valueLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));

    // --- color status ---
    Color statusColor;
    if (currentPH < OPTIMAL_LOW) {
      statusColor = Color.web("#4FC3F7");
    } else if (currentPH > OPTIMAL_HIGH) {

      statusColor = Color.web("#FF7043");
    } else {

      statusColor = Color.web("#81C784");
    }

    phIcon.setFill(statusColor);
    valueLabel.setTextFill(statusColor);


    VBox titleBox = new VBox(-2, nameLabel, valueLabel);
    HBox topPane = new HBox(10, phIcon, titleBox);
    topPane.setPadding(new Insets(0, 0, 10, 0));


    Canvas chartCanvas = new Canvas(198, 60);
    drawChart(chartCanvas, statusColor);


    VBox layout = new VBox(5, topPane, chartCanvas);
    layout.setPadding(new Insets(10));

    return layout;
  }

  /**
   * Draws the pH trend chart on the provided canvas.
   * @param canvas the canvas to draw on
   * @param lineColor the color of the trend line
   */

  private void drawChart(Canvas canvas, Color lineColor) {
    GraphicsContext gc = canvas.getGraphicsContext2D();
    double width = canvas.getWidth();
    double height = canvas.getHeight();

    gc.clearRect(0, 0, width, height);


    gc.setStroke(Color.web("#f0f0f0"));
    gc.setLineWidth(1);

    int numLines = 8;
    for(int i = 0; i < numLines; i++){
      gc.strokeLine(0, (height / (numLines - 1)) * i, width, (height / (numLines - 1)) * i);
    }

    double y_neutral = height - ((7.0 - MIN_PH) / (MAX_PH - MIN_PH) * height);
    gc.setStroke(Color.web("#CCCCCC"));
    gc.setLineWidth(1.5);
    gc.strokeLine(0, y_neutral, width, y_neutral);
    gc.setStroke(lineColor);
    gc.setLineWidth(2.0);

    double range = MAX_PH - MIN_PH;
    double xStep = width / (MAX_HISTORY_SIZE - 1);

    for (int i = 0; i < history.size() - 1; i++) {

      double y1 = height - ((history.get(i) - MIN_PH) / range * height);
      double y2 = height - ((history.get(i + 1) - MIN_PH) / range * height);

      y1 = Math.max(0, Math.min(height, y1));
      y2 = Math.max(0, Math.min(height, y2));

      gc.strokeLine(i * xStep, y1, (i + 1) * xStep, y2);
    }
  }

  /**
   * Returns the current pH value.
   * @return the current pH value
   */

  @Override
  public double getCurrentValue() {
    return currentPH;
  }
}
