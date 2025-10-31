package server;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Physical-like environment state for a greenhouse node.
 * Values evolve over time and are influenced by actuators.
 */
public class EnvironmentState {
  // --- Sensors (public for simplicity; could be getters) ---
  public double temperatureC = 22.0;  // °C
  public double humidityPct = 55.0;   // % RH
  public int lightLux = 420;          // lux
  public double ph = 6.4;             // pH

  // External environment assumptions (can be extended or randomized)
  public double outsideTempC = 12.0;  // °C
  public int daytimeLightLux = 10000; // lux when "daylight" (demo)

  // --- Simple noise helper ---
  private double noise(double amplitude) {
    return (ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0 * amplitude;
  }

  /**
   * Update the environment by dt seconds, considering actuator states.
   * The model is deliberately simple and stable for demo purposes.
   */
  public void step(double dtSeconds, boolean fanOn, boolean pumpOn, boolean co2On, WindowLevel window) {
    // --- Temperature dynamics ---
    double towardOutside = (window == WindowLevel.OPEN) ? 0.15 : (window == WindowLevel.HALF ? 0.08 : 0.03);
    double fanCooling = fanOn ? 0.10 : 0.0;  // fan pushes toward outside temp too
    double targetTemp = outsideTempC + (temperatureC - outsideTempC) * (1.0 - (towardOutside + fanCooling));
    temperatureC += (targetTemp - temperatureC) * 0.10 * dtSeconds + noise(0.02);

    // --- Humidity dynamics ---
    double evap = pumpOn ? +0.35 : -0.08;   // pump increases humidity, otherwise it slowly drops
    double ventLoss = (fanOn ? -0.20 : 0.0) + (window == WindowLevel.OPEN ? -0.30 : (window == WindowLevel.HALF ? -0.15 : 0.0));
    humidityPct += (evap + ventLoss) * dtSeconds + noise(0.15);
    humidityPct = Math.max(0.0, Math.min(100.0, humidityPct));

    // --- Light dynamics ---
    // For demo: light drifts slightly; you could model time-of-day instead
    lightLux += (int) Math.round(noise(5));
    lightLux = Math.max(0, Math.min(200_000, lightLux));

    // --- pH dynamics ---
    // Very simplified: pump slightly increases pH toward 7, CO2 slightly lowers toward 6.0
    double phTrend = 0.0;
    if (pumpOn) phTrend += (7.0 - ph) * 0.05;
    if (co2On)  phTrend += (6.0 - ph) * 0.04;
    ph += phTrend * dtSeconds + noise(0.01);
    ph = Math.max(0.0, Math.min(14.0, ph));
  }

  /** Window openness enum aligned with protocol CLOSED/HALF/OPEN */
  public enum WindowLevel { CLOSED, HALF, OPEN }
}

