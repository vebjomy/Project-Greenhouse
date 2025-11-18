package server;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Physical-like environment state for a greenhouse node. Values evolve over time and are influenced
 * by actuators.
 */
public class EnvironmentState {
  // --- Sensors ---
  private double temperatureC = 22.0; // °C
  private double humidityPct = 55.0; // % RH
  private int lightLux = 420; // lux
  private double ph = 8.4; // pH

  // --- External environment and Time ---
  // Simple time simulation for day/night cycle
  private double timeOfDayHours = 12.0; // Starts at noon
  private static final double DAY_LENGTH_HOURS = 24.0;
  private static final double MIN_OUTSIDE_TEMP_C = 8.0; // °C
  private static final double MAX_OUTSIDE_TEMP_C = 16.0; // °C
  public int daytimeLightLux = 45000; // max lux when "daylight"

  // --- CO2 heating effect parameters ---
  private static final double CO2_HEATING_RATE = 0.25; // Constant heat added by CO2 (simplified)
  private static final double MAX_CO2_TEMP_INCREASE = 5.0; // Maximum temperature increase from CO2

  // --- Light parameters ---
  private static final int LIGHT_MIN = 50;
  private static final int LIGHT_MAX = 50000;

  // --- Simple noise helper ---
  private double noise(double amplitude) {
    return (ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0 * amplitude;
  }

  // --- Dynamic calculation of outside parameters ---

  /** Calculates the current outside temperature based on the time of day (sinusoidal model). */
  public double getCurrentOutsideTempC() {
    double amplitude = (MAX_OUTSIDE_TEMP_C - MIN_OUTSIDE_TEMP_C) / 2.0;
    double average = (MAX_OUTSIDE_TEMP_C + MIN_OUTSIDE_TEMP_C) / 2.0;
    // Simple sinusoid peaking at 14:00 (2 PM)
    return average + amplitude * Math.sin(2.0 * Math.PI * (timeOfDayHours - 14.0) / DAY_LENGTH_HOURS);
  }

  /** Calculates the current outside light based on the time of day. */
  public double getCurrentOutsideLightLux() {
    if (timeOfDayHours > 6.0 && timeOfDayHours < 18.0) {
      // Simplified peak at noon (12:00) during a 12-hour day cycle
      return daytimeLightLux * Math.sin(Math.PI * (timeOfDayHours - 6.0) / 12.0);
    } else {
      return LIGHT_MIN; // Minimal light at night
    }
  }

  /**
   * Update the environment by dt seconds, considering actuator states.
   *
   * @param dtSeconds seconds elapsed
   * @param fanOn the fan state
   * @param pumpOn the water pump state
   * @param co2On the CO2 generator state
   * @param window the window openness level
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

  /** Update the time of day. */
  public void updateTime(double dtSeconds) {
    timeOfDayHours = (timeOfDayHours + dtSeconds / 3600.0) % DAY_LENGTH_HOURS;
  }

  /**
   * Update temperature considering fan, CO2, and window state. Uses a thermal exchange model.
   */
  public void updateTemperature(
      double dtSeconds, boolean fanOn, boolean co2On, WindowLevel window) {
    double outsideTempC = getCurrentOutsideTempC();

    // Base heat exchange (e.g., insulation) - moves toward outsideTempC
    double thermalConductivity = 0.03;
    double windowHeatTransfer = 0.0;

    // Increased heat loss/gain when the window is open
    if (window == WindowLevel.OPEN) {
      windowHeatTransfer = 0.12;
    } else if (window == WindowLevel.HALF) {
      windowHeatTransfer = 0.05;
    }
    // Fan also increases air circulation and heat exchange
    double fanHeatTransfer = fanOn ? 0.07 : 0.0;

    // Total heat exchange coefficient (how fast it moves toward outsideTempC)
    double totalHeatTransfer = thermalConductivity + windowHeatTransfer + fanHeatTransfer;

    // Apply heat exchange: (outsideTempC - temperatureC) * totalHeatTransfer
    // If inside is warmer, tempChange is negative (cooling); if colder, tempChange is positive (warming)
    double tempChange = (outsideTempC - temperatureC) * totalHeatTransfer;

    // CO2 heating effect - constant addition of heat
    if (co2On && temperatureC < outsideTempC + MAX_CO2_TEMP_INCREASE) {
      tempChange += CO2_HEATING_RATE;
    }

    // Light heating: Small effect from high light levels (simplified sun heating)
    double lightHeating = (lightLux / (double) daytimeLightLux) * 0.005;
    tempChange += lightHeating;

    temperatureC += tempChange * dtSeconds + noise(0.02);
  }

  /**
   * Update humidity considering pump, fan, window states, and a small temperature effect.
   */
  public void updateHumidity(double dtSeconds, boolean pumpOn, boolean fanOn, WindowLevel window) {
    // pump increases humidity (evaporation), otherwise it slowly drops
    double evap = pumpOn ? +0.35 : -0.08;

    // Fan and window cause loss of humidity (ventilation)
    double ventLoss =
        (fanOn ? -0.20 : 0.0)
            + (window == WindowLevel.OPEN ? -0.30 : (window == WindowLevel.HALF ? -0.15 : 0.0));

    // Condensation/Evaporation effect: higher temp increases humidity (more evaporation)
    double tempEffect = (temperatureC - 20.0) * 0.02; // Change factor: 0 at 20°C

    humidityPct += (evap + ventLoss + tempEffect) * dtSeconds + noise(0.15);
    humidityPct = Math.max(0.0, Math.min(100.0, humidityPct));
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
        // High rate of change towards outside light
        windowFactor = 0.05;
        break;
      case HALF:
        // Medium rate of change towards outside light
        windowFactor = 0.03;
        break;
      case CLOSED:
        // Slow change towards minimum light level (LIGHT_MIN)
        targetLight = LIGHT_MIN;
        windowFactor = 0.01;
        break;
      default:
        windowFactor = 0.0;
    }

    // Change is proportional to the difference: (targetLight - currentLight) * windowFactor
    double lightChange = (targetLight - lightLux) * windowFactor;

    lightLux += lightChange * dtSeconds + noise(5);

    lightLux = Math.max(LIGHT_MIN, Math.min(LIGHT_MAX, lightLux));
  }

  /**
   * Update the pH level of the environment considering pump and CO2 states.
   */
  public void updatePh(double dtSeconds, boolean pumpOn, boolean co2On) {
    double phTrend = 0.0;
    // Pump tends to neutralize (move toward 7.0)
    if (pumpOn) {
      phTrend += (7.0 - ph) * 0.05;
    }
    // CO2 tends to acidify (move toward 6.0)
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

  public double getTimeOfDayHours() {
    return timeOfDayHours;
  }
}