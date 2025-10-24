package model;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 * HumiditySensor simulates a humidity sensor that provides humidity readings
 * and a visual representation using JavaFX.
 * @version 1.0
 */

public class HumiditySensor implements Sensor {
  private double currentHumidity; // 0% - 100%
  private static final DecimalFormat df = new DecimalFormat("0.0");


  /* * Constructor initializes the humidity sensor with a random value.
   */

  public HumiditySensor() {
    updateValue();
  }
  /* * Updates the humidity value with a new random reading.
   */

  public void updateValue() {
    // Simulate humidity: 0% - 100%
    this.currentHumidity = ThreadLocalRandom.current().nextDouble(0, 100);
  }

  /* * Returns the name of the sensor.
   * @return Sensor name
   */

  @Override
  public String getSensorName() {
    return "Humidity";
  }

  /* * Returns the current humidity reading as a formatted string.
   * @return Humidity reading
   */

  @Override
  public String getReading() {
    return df.format(currentHumidity) + " %";
  }

  /* * Returns the current humidity value as a numeric value.
   * @return Humidity value
   */

  @Override
  public double getNumericValue() {
    return currentHumidity;
  }

  /* * Provides a JavaFX Pane representing the humidity sensor visually.
   * @return Visual representation of the sensor
   */

  @Override
  public Pane getVisualRepresentation() {
    updateValue();

    double arcLength = 3.6 * currentHumidity;
    double startAngle = 90 - arcLength;
    int radius = 36;

    // --- Circular background ---
    Circle backgroundCircle = new Circle(40);
    backgroundCircle.setFill(Color.web("#f0f0f0"));

    // --- Colored arc representing humidity ---
    Arc humidityArc = new Arc(0, 0, radius, radius, startAngle, arcLength);
    humidityArc.setType(ArcType.OPEN);
    humidityArc.setStrokeWidth(8);
    humidityArc.setFill(null);

    // Color coding
    if (currentHumidity < 30) {
      humidityArc.setStroke(Color.web("#4FC3F7")); // Low humidity - blue
    } else if (currentHumidity > 70) {
      humidityArc.setStroke(Color.web("#E57373")); // High humidity - red
    } else {
      humidityArc.setStroke(Color.web("#81C784")); // Normal - green
    }

    // Ensure the arc is properly centered
    humidityArc.setStrokeLineCap(StrokeLineCap.ROUND);
    humidityArc.setLayoutX(0);
    humidityArc.setLayoutY(0);

    // --- Labels ---
    Label nameLabel = new Label(getSensorName());
    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
    nameLabel.setAlignment(Pos.CENTER);

    Label valueLabel = new Label(getReading());
    valueLabel.setFont(Font.font("System", FontWeight.NORMAL, 20));
    valueLabel.setAlignment(Pos.CENTER);

    // --- Stack layout ---
    StackPane circlePane = new StackPane();
    circlePane.getChildren().addAll(backgroundCircle, humidityArc, valueLabel);
    circlePane.setPadding(new Insets(10));
    circlePane.setPrefSize(80, 80);

    VBox layout = new VBox(5, nameLabel, circlePane);
    layout.setPadding(new Insets(10));
    layout.setAlignment(Pos.CENTER);

    return layout;
  }

  /* * Returns the current humidity value as a double.
   * @return Current humidity
   */

  @Override
  public double getCurrentValue() {
    return currentHumidity;
  }
}

