package server;

import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Physical-like environment state for a greenhouse node. Values evolve over time and are influenced
 * by actuators.
 */
public class EnvironmentState {

  // --- Sensor Initial Values ---
  private double temperatureC = 22.0;
  private double humidityPct = 55.0;
  private int lightLux = 420;
  private double ph = 8.4;

  // --- Time and External Environment ---
  private double timeOfDayHours = 12.0;
  private static final double DAY_LENGTH_HOURS = 24.0;
  private static final double SECONDS_PER_HOUR = 3600.0;
  private static final double MIN_OUTSIDE_TEMP_C = 8.0;
  private static final double MAX_OUTSIDE_TEMP_C = 16.0;
  private static final int DAYTIME_LIGHT_LUX = 45000;

  // Day/Night Cycle Constants
  private static final double DAY_START_HOUR = 6.0;
  private static final double DAY_END_HOUR = 18.0;
  private static final double PEAK_TEMP_HOUR = 14.0; // 2 PM - peak temperature

  // --- Temperature Constants ---
  private static final double BASE_THERMAL_CONDUCTIVITY = 0.03;
  private static final double OPEN_WINDOW_HEAT_TRANSFER = 0.12;
  private static final double HALF_WINDOW_HEAT_TRANSFER = 0.05;
  private static final double FAN_HEAT_TRANSFER = 0.07;
  private static final double CO2_HEATING_RATE = 0.25;
  private static final double MAX_CO2_TEMP_INCREASE = 5.0;
  private static final double LIGHT_HEATING_FACTOR = 0.005;
  private static final double TEMPERATURE_NOISE_AMPLITUDE = 0.02;

  // --- Humidity Constants ---
  private static final double PUMP_HUMIDITY_GAIN = 0.35;
  private static final double NATURAL_HUMIDITY_DECAY = -0.08;
  private static final double FAN_HUMIDITY_LOSS = -0.20;
  private static final double OPEN_WINDOW_HUMIDITY_LOSS = -0.30;
  private static final double HALF_WINDOW_HUMIDITY_LOSS = -0.15;
  private static final double TEMP_EFFECT_BASE = 20.0;
  private static final double TEMP_EFFECT_FACTOR = 0.02;
  private static final double HUMIDITY_NOISE_AMPLITUDE = 0.15;
  private static final double MIN_HUMIDITY = 0.0;
  private static final double MAX_HUMIDITY = 100.0;

  // --- Light Constants ---
  private static final int LIGHT_MIN = 50;
  private static final int LIGHT_MAX = 50000;
  private static final double OPEN_WINDOW_LIGHT_FACTOR = 0.05;
  private static final double HALF_WINDOW_LIGHT_FACTOR = 0.03;
  private static final double CLOSED_WINDOW_LIGHT_FACTOR = 0.01;
  private static final double LIGHT_NOISE_AMPLITUDE = 5.0;

  // --- pH Constants ---
  private static final double NEUTRAL_PH = 7.0;
  private static final double ACIDIC_PH = 6.0;
  private static final double PUMP_PH_NEUTRALIZE_FACTOR = 0.05;
  private static final double CO2_ACIDIFY_FACTOR = 0.04;
  private static final double PH_NOISE_AMPLITUDE = 0.01;
  private static final double MIN_PH = 0.0;
  private static final double MAX_PH = 14.0;

  // --- Simple noise helper ---
  private double noise(double amplitude) {
    return (ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0 * amplitude;
  }

  // --- Dynamic calculation of outside parameters ---

  /**
   * Calculates the current outside temperature based on the time of day (sinusoidal model).
   */
  public double getCurrentOutsideTempC() {
    double amplitude = (MAX_OUTSIDE_TEMP_C - MIN_OUTSIDE_TEMP_C) / 2.0;
    double average = (MAX_OUTSIDE_TEMP_C + MIN_OUTSIDE_TEMP_C) / 2.0;
    return average + amplitude * Math.sin(
        2.0 * Math.PI * (timeOfDayHours - PEAK_TEMP_HOUR) / DAY_LENGTH_HOURS);
  }

  /**
   * Calculates the current outside light based on the time of day.
   */
  public double getCurrentOutsideLightLux() {
    if (timeOfDayHours > DAY_START_HOUR && timeOfDayHours < DAY_END_HOUR) {
      return DAYTIME_LIGHT_LUX * Math.sin(
          Math.PI * (timeOfDayHours - DAY_START_HOUR) / (DAY_END_HOUR - DAY_START_HOUR));
    } else {
      return LIGHT_MIN;
    }
  }

  /**
   * Update the environment by dt seconds, considering actuator states.
   *
   * @param dtSeconds seconds elapsed
   * @param fanOn     the fan state
   * @param pumpOn    the water pump state
   * @param co2On     the CO2 generator state
   * @param window    the window openness level
   */
  public void step(
      double dtSeconds, boolean fanOn, boolean pumpOn, boolean co2On, WindowLevel window) {
    // 1. Update Time
    updateTime(dtSeconds);

    // 2. Temperature dynamics
    updateTemperature(dtSeconds, fanOn, co2On, window);

    // 3. Humidity dynamics
    updateHumidity(dtSeconds, pumpOn, fanOn, window);

    // 4. Light dynamics
    updateLight(dtSeconds, window);

    // 5. pH dynamics
    updatePh(dtSeconds, pumpOn, co2On);
  }

  /**
   * Update the time of day.
   */
  public void updateTime(double dtSeconds) {
    LocalTime now = LocalTime.now();
    timeOfDayHours = now.getHour()
        + now.getMinute() / 60.0
        + now.getSecond() / 3600.0;
  }

  /**
   * Update temperature considering fan, CO2, and window state. Uses a thermal exchange model.
   */
  public void updateTemperature(
      double dtSeconds, boolean fanOn, boolean co2On, WindowLevel window) {
    double outsideTempC = getCurrentOutsideTempC();

    double windowHeatTransfer = 0.0;
    if (window == WindowLevel.OPEN) {
      windowHeatTransfer = OPEN_WINDOW_HEAT_TRANSFER;
    } else if (window == WindowLevel.HALF) {
      windowHeatTransfer = HALF_WINDOW_HEAT_TRANSFER;
    }

    double fanHeatTransfer = fanOn ? FAN_HEAT_TRANSFER : 0.0;
    double totalHeatTransfer = BASE_THERMAL_CONDUCTIVITY + windowHeatTransfer + fanHeatTransfer;

    double tempChange = (outsideTempC - temperatureC) * totalHeatTransfer;

    // CO2 heating effect
    if (co2On && temperatureC < outsideTempC + MAX_CO2_TEMP_INCREASE) {
      tempChange += CO2_HEATING_RATE;
    }

    // Light heating effect
    double lightHeating = (lightLux / (double) DAYTIME_LIGHT_LUX) * LIGHT_HEATING_FACTOR;
    tempChange += lightHeating;

    temperatureC += tempChange * dtSeconds + noise(TEMPERATURE_NOISE_AMPLITUDE);
  }

  /**
   * Update humidity considering pump, fan, window states, and a small temperature effect.
   */
  public void updateHumidity(double dtSeconds, boolean pumpOn, boolean fanOn, WindowLevel window) {
    double evap = pumpOn ? PUMP_HUMIDITY_GAIN : NATURAL_HUMIDITY_DECAY;

    double windowHumidityLoss = 0.0;
    if (window == WindowLevel.OPEN) {
      windowHumidityLoss = OPEN_WINDOW_HUMIDITY_LOSS;
    } else if (window == WindowLevel.HALF) {
      windowHumidityLoss = HALF_WINDOW_HUMIDITY_LOSS;
    }

    double fanHumidityLoss = fanOn ? FAN_HUMIDITY_LOSS : 0.0;
    double tempEffect = (temperatureC - TEMP_EFFECT_BASE) * TEMP_EFFECT_FACTOR;

    humidityPct += (evap + fanHumidityLoss + windowHumidityLoss + tempEffect) * dtSeconds + noise(
        HUMIDITY_NOISE_AMPLITUDE);
    humidityPct = Math.max(MIN_HUMIDITY, Math.min(MAX_HUMIDITY, humidityPct));
  }

  /**
   * Update light considering window state. Light level now moves toward the current outside light.
   */
  public void updateLight(double dtSeconds, WindowLevel window) {
    double outsideLight = getCurrentOutsideLightLux();
    double targetLight = outsideLight;
    double windowFactor = 0.0;

    switch (window) {
      case OPEN:
        windowFactor = OPEN_WINDOW_LIGHT_FACTOR;
        break;
      case HALF:
        windowFactor = HALF_WINDOW_LIGHT_FACTOR;
        break;
      case CLOSED:
        targetLight = LIGHT_MIN;
        windowFactor = CLOSED_WINDOW_LIGHT_FACTOR;
        break;
      default:
        windowFactor = 0.0;
    }

    double lightChange = (targetLight - lightLux) * windowFactor;
    lightLux += lightChange * dtSeconds + noise(LIGHT_NOISE_AMPLITUDE);
    lightLux = Math.max(LIGHT_MIN, Math.min(LIGHT_MAX, lightLux));
  }

  /**
   * Update the pH level of the environment considering pump and CO2 states.
   */
  public void updatePh(double dtSeconds, boolean pumpOn, boolean co2On) {
    double phTrend = 0.0;

    if (pumpOn) {
      phTrend += (NEUTRAL_PH - ph) * PUMP_PH_NEUTRALIZE_FACTOR;
    }

    if (co2On) {
      phTrend += (ACIDIC_PH - ph) * CO2_ACIDIFY_FACTOR;
    }

    ph += phTrend * dtSeconds + noise(PH_NOISE_AMPLITUDE);
    ph = Math.max(MIN_PH, Math.min(MAX_PH, ph));
  }

  /**
   * Window openness enum aligned with protocol. CLOSED/HALF/OPEN
   */
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

  public double getTimeOfDayHours() {
    return timeOfDayHours;
  }
}