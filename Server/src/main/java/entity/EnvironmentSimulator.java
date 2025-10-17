package entity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Simulates the environment in a greenhouse. Information about the environment can be retrieved by
 * sensors. The environment can be influenced by actuators.
 *
 * <p>A 24-hour cycle is represented by a 120-second cycle in the simulator. Each second in the
 * simulator thus represents 12 minutes in real life.
 *
 * <p>The temperature is represented in degrees Celsius. It follows a simple model where the
 * temperature increases during the day and decreases at night.
 *
 * <p>Humidity is represented by a percentage. It follows a faster cycle than temperature and light
 * level.
 */
public class EnvironmentSimulator {
  private int temperature; // Current temperature in Celsius
  private int humidity;
  private int lightLevel;
  private int cycleSecond = 0; // Current second in the 120-second cycle
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public EnvironmentSimulator() {
    this.temperature = 22; // Default temperature in Celsius
    this.humidity = 50; // Default humidity in percentage
    this.lightLevel = 70; // Default light level in percentage
  }

  private void startCycle() {
    scheduler.scheduleAtFixedRate(
        () -> {
          cycleSecond = (cycleSecond + 1) % 120;
          updateEnvironment(cycleSecond);
        },
        0,
        1,
        TimeUnit.SECONDS);
  }

  private void updateEnvironment(int second) {
    // Example: temperature rises for first 60 seconds, falls for next 60
    if (second < 60) {
      setTemperature(getTemperature() + second / 6);
      setLightLevel(getLightLevel() + second / 2);
    } else {
      setTemperature(getTemperature() - (second - 60) / 6);
      setLightLevel(getLightLevel() - (second - 60) / 2);
    }
    // Humidity could follow a similar or different pattern
    setHumidity(50 + (int) (10 * Math.sin(Math.toRadians(second * 3))));
  }

  /**
   * Set the temperature of the environment.
   *
   * @param temperature the new temperature in Celsius
   */
  public void setTemperature(int temperature) {
    if (temperature < -273) {
      throw new IllegalArgumentException(
          "Error: Physically impossible. Temperature must be > -273");
    }
    this.temperature = temperature;
  }

  /**
   * Set the humidity of the environment.
   *
   * @param humidity the new humidity in percentage
   */
  public void setHumidity(int humidity) {
    this.humidity = humidity;
  }

  /**
   * Set the light level of the environment.
   *
   * @param lightLevel the new light level in percentage
   */
  public void setLightLevel(int lightLevel) {
    this.lightLevel = lightLevel;
  }

  /**
   * Get the current temperature of the environment.
   *
   * @return the current temperature in Celsius.
   */
  public int getTemperature() {
    return temperature;
  }

  /**
   * Get the current humidity of the environment.
   *
   * @return the current humidity in percentage.
   */
  public int getHumidity() {
    return humidity;
  }

  /**
   * Get the current light level of the environment.
   *
   * @return the current light level in percentage.
   */
  public int getLightLevel() {
    return lightLevel;
  }

  /**
   * Set the cycle second to a specified value. The second must be a value between 0 and 120
   *
   * @param second The second to set the clock to.
   */
  public void setCycleSecond(int second) {
    if (second > 120 || second < 0) {
      throw new IllegalArgumentException("Invalid cycle second");
    }
    this.cycleSecond = second;
  }
}
