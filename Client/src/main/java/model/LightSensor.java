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
 * LightSensor simulates a light sensor that provides light intensity readings
 * and a visual representation using JavaFX.
 * @version 1.0
 */

public class LightSensor implements Sensor {
  private double currentLight;
  private final LinkedList<Double> history = new LinkedList<>();
  private static final int MAX_HISTORY_SIZE = 20;
  private static final DecimalFormat df = new DecimalFormat("0.0");

  /* * Constructor initializes the light sensor with random values.
   */

  public LightSensor() {

    for (int i = 0; i < MAX_HISTORY_SIZE; i++) {
      updateValue();
    }
  }

  /* * Updates the light value with a new random reading.
   */

  public void updateValue() {
    this.currentLight = ThreadLocalRandom.current().nextDouble(0.0, 1000.0);
    history.addLast(this.currentLight);
    if (history.size() > MAX_HISTORY_SIZE) {
      history.removeFirst();
    }
  }

  /* * Returns the name of the sensor.
   * This method is overridden from the Sensor interface.
   * @return Sensor name
   */

  @Override
  public String getSensorName() {
    return "Light";
  }

  @Override
  public String getReading() {
    return df.format(currentLight) + " lx";
  }

  @Override
  public double getNumericValue() {
    return currentLight;
  }

  @Override
  public Pane getVisualRepresentation() {
    updateValue();

    // --- icon ---
    SVGPath lightIcon = new SVGPath();
    lightIcon.setContent("M1 11H4V13H1V11M19.1 3.5L17 5.6L18.4 7L20.5 4.9L19.1 3.5M11" +
        " 1H13V4H11V1M4.9 3.5L3.5 4.9L5.6 7L7 5.6L4.9 3.5M10 22C10 22.6 10.4 23 11" +
        " 23H13C13.6 23 14 22.6 14 22V21H10V22M12 6C8.7 6 6 8.7 6 12C6 14.2 7.2 16.2 9 " +
        "17.2V19C9 19.6 9.4 20 10 20H14C14.6 20 15 19.6 15 19V17.2C16.8 16.2 18 14.2 18" +
        " 12C18 8.7 15.3 6 12 6M13" +
        " 15.9V17H11V15.9C9.3 15.5 8 13.9 8 12C8 9.8 9.8 8 12 8S16 9.8 16 12C16 13.9" +
        " 14.7 15.4 13 15.9M20 11H23V13H20V11Z");
    lightIcon.setScaleX(1.2);
    lightIcon.setScaleY(1.2);

    // --- Labels ---
    Label nameLabel = new Label(getSensorName());
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
    Label valueLabel = new Label(getReading());
    valueLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));

    // --- color  ---
    Color statusColor;
    if (currentLight < 200) {
      statusColor = Color.web("#4FC3F7");
    } else if (currentLight > 700) {
      statusColor = Color.web("#FFD54F");
    } else {
      statusColor = Color.web("#81C784");
    }
    lightIcon.setFill(statusColor);
    valueLabel.setTextFill(statusColor);


    VBox titleBox = new VBox(-2, nameLabel, valueLabel);
    HBox topPane = new HBox(10, lightIcon, titleBox);
    topPane.setPadding(new Insets(0, 0, 10, 0));


    Canvas chartCanvas = new Canvas(198, 60);
    drawChart(chartCanvas, statusColor);


    VBox layout = new VBox(5, topPane, chartCanvas);
    layout.setPadding(new Insets(10));

    return layout;
  }

  /* * Draws the light intensity history chart on the provided canvas.
   * @param canvas Canvas to draw the chart on
   * @param lineColor Color of the chart line
   */

  private void drawChart(Canvas canvas, Color lineColor) {
    GraphicsContext gc = canvas.getGraphicsContext2D();
    double width = canvas.getWidth();
    double height = canvas.getHeight();

    gc.clearRect(0, 0, width, height);

    gc.setStroke(Color.web("#f0f0f0"));
    gc.setLineWidth(1);
    for(int i = 0; i < 5; i++){
      gc.strokeLine(0, (height/4)*i, width, (height/4)*i);
    }

    gc.setStroke(lineColor);
    gc.setLineWidth(2.0);

    double minLux = 0.0;
    double maxLux = 1000.0;
    double range = maxLux - minLux;
    double xStep = width / (MAX_HISTORY_SIZE - 1);

    for (int i = 0; i < history.size() - 1; i++) {
      double y1 = height - ((history.get(i) - minLux) / range * height);
      double y2 = height - ((history.get(i + 1) - minLux) / range * height);
      gc.strokeLine(i * xStep, y1, (i + 1) * xStep, y2);
    }
  }

  /* * Returns the current light value as a double.
   * @return Current light intensity
   */

  @Override
  public double getCurrentValue() {
    return currentLight;
  }
}

