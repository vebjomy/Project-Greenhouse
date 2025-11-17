package server;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Physical-like environment state for a greenhouse node. Values evolve over time and are influenced
 * by actuators.
 */
public class EnvironmentState {
  // Sensors
  private double temperatureC = 22.0; // °C
  private double humidityPct = 55.0; // % RH
  private int lightLux = 420; // lux
  private double ph = 8.4; // pH

  // External environment assumptions (can be extended or randomized)
  public double outsideTempC = 12.0; // °C
  public int daytimeLightLux = 10000; // lux when "daylight" (demo)

  // CO2 heating effect parameters
  private static final double CO2_HEATING_RATE = 0.25; // °C per second when CO2 is on
  private static final double MAX_CO2_TEMP_INCREASE = 5.0; // Maximum temperature increase from CO2

  // Light parameters
  private static final double LIGHT_INCREASE_OPEN = 25.0; // lux/sec when window open
  private static final double LIGHT_INCREASE_HALF = 12.0; // lux/sec when window half open
  private static final double LIGHT_DECREASE_CLOSED = 8.0; // lux/sec when window closed
  private static final int LIGHT_MIN = 50;
  private static final int LIGHT_MAX = 20000;

  // --- Simple noise helper ---
  private double noise(double amplitude) {
    return (ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0 * amplitude;
  }

  /**
   * Update the environment by dt seconds, considering actuator states. The model is deliberately
   * simple and stable for demo purposes.
   *
   * @param dtSeconds seconds elapsed
   * @param fanOn the fan state
   * @param pumpOn the water pump state
   * @param co2On the CO2 generator state
   * @param window the window openness level
   */
  public void step(
      double dtSeconds, boolean fanOn, boolean pumpOn, boolean co2On, WindowLevel window) {
    // --- Temperature dynamics ---
    updateTemperature(dtSeconds, fanOn, co2On, window);

    // --- Humidity dynamics ---
    updateHumidity(dtSeconds, pumpOn, fanOn, window);

    // --- Light dynamics ---
    updateLight(dtSeconds, window);

    // --- pH dynamics ---
    updatePh(dtSeconds, pumpOn, co2On);
  }

  /**
   * Update temperature considering fan, CO2, and window state.
   *
   * @param dtSeconds seconds elapsed
   * @param fanOn the fan state
   * @param co2On the CO2 state
   * @param window the window openness level
   */
  public void updateTemperature(
      double dtSeconds, boolean fanOn, boolean co2On, WindowLevel window) {
    double towardOutside =
        (window == WindowLevel.OPEN) ? 0.15 : (window == WindowLevel.HALF ? 0.08 : 0.03);
    double fanCooling = fanOn ? 0.10 : 0.0; // fan pushes toward outside temp too

    // CO2 heating effect - increases temperature when CO2 is on
    double co2Heating = co2On ? CO2_HEATING_RATE : 0.0;

    double targetTemp =
        outsideTempC + (temperatureC - outsideTempC) * (1.0 - (towardOutside + fanCooling));

    // Apply CO2 heating effect
    if (co2On && temperatureC < outsideTempC + MAX_CO2_TEMP_INCREASE) {
      targetTemp += co2Heating * dtSeconds;
    }

    temperatureC += (targetTemp - temperatureC) * 0.10 * dtSeconds + noise(0.02);
  }

  /**
   * Update humidity considering pump, fan and window states.
   *
   * @param dtSeconds seconds elapsed
   * @param pumpOn the pump state
   * @param fanOn the fan state
   * @param window the window openness level
   */
  public void updateHumidity(double dtSeconds, boolean pumpOn, boolean fanOn, WindowLevel window) {
    double evap = pumpOn ? +0.35 : -0.08; // pump increases humidity, otherwise it slowly drops
    double ventLoss =
        (fanOn ? -0.20 : 0.0)
            + (window == WindowLevel.OPEN ? -0.30 : (window == WindowLevel.HALF ? -0.15 : 0.0));
    humidityPct += (evap + ventLoss) * dtSeconds + noise(0.15);
    humidityPct = Math.max(0.0, Math.min(100.0, humidityPct));
  }

  /**
   * Update light considering window state.
   *
   * @param dtSeconds seconds elapsed
   * @param window the window openness level
   */
  public void updateLight(double dtSeconds, WindowLevel window) {
    // Light depends on window state: closed = decreasing, open = increasing
    double lightChange = 0.0;

    switch (window) {
      case OPEN:
        // Moderate increase when window fully open
        lightChange = LIGHT_INCREASE_OPEN;
        break;
      case HALF:
        // Small increase when half open
        lightChange = LIGHT_INCREASE_HALF;
        break;
      case CLOSED:
        // Slow decrease when closed (no external light)
        lightChange = -LIGHT_DECREASE_CLOSED;
        break;
      default:
        lightChange = 0.0;
    }

    // Apply the light change with some noise
    lightLux += lightChange * dtSeconds + noise(5);

    // Ensure light stays within reasonable bounds
    lightLux = Math.max(LIGHT_MIN, Math.min(LIGHT_MAX, lightLux));
  }

  /**
   * Update the pH level of the environment considering pump and CO2 states. Very simplified: pump
   * slightly increases pH toward 7, CO2 slightly lowers toward 6.0
   *
   * @param dtSeconds seconds elapsed
   * @param pumpOn the pump state
   * @param co2On the CO2 state
   */
  public void updatePh(double dtSeconds, boolean pumpOn, boolean co2On) {
    double phTrend = 0.0;
    if (pumpOn) {
      phTrend += (7.0 - ph) * 0.05;
    }
    if (co2On) {
      phTrend += (6.0 - ph) * 0.04;
    }
    ph += phTrend * dtSeconds + noise(0.01);
    ph = Math.max(0.0, Math.min(14.0, ph));
  }

  /** Window openness enum aligned with protocol. CLOSED/HALF/OPEN */
  public enum WindowLevel {
    CLOSED,
    HALF,
    OPEN
  }

  // --- Getters ---
  public double getTemperatureC() {
    return temperatureC;
  }

  public double getHumidityPct() {
    return humidityPct;
  }

  public int getLightLux() {
    return lightLux;
  }

  public double getPh() {
    return ph;
  }
}
