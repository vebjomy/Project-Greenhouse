package entity;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * SensorNode2: creates typed SensorReading objects from EnvironmentSimulator instances.
 */
public class SensorNode2 {
  private Map<String, EnvironmentSimulator> environments; // Map of environment simulators

  /**
   * Constructor for SensorNode2.
   *
   * @param environments Map of environment simulators keyed by environment ID
   */
  public SensorNode2(Map<String, EnvironmentSimulator> environments) {
    this.environments = environments;
  }

  public Map<String, EnvironmentSimulator> getEnvironments() {
    return environments;
  }

  public void setEnvironments(Map<String, EnvironmentSimulator> environments) {
    this.environments = environments;
  }

  /**
   * Build a typed SensorReading for the given simulator.
   * Include a timestamp and an environment id for DB storage.
   */
  public SensorReading getSensorReading(EnvironmentSimulator env, String environmentId) {
    Objects.requireNonNull(env, "env cannot be null");
    Instant now = Instant.now();

    // convert/scale as needed; using double to preserve decimals if simulator returns them later
    double temperature = env.getTemperature();
    double humidity = env.getHumidity();
    double lightLevel = env.getLightLevel();
    double co2Level = env.getCo2Level();
    double soilPH = env.getSoilPH();
    double soilMoisture = env.getSoilMoisture();

    return new SensorReading(environmentId, now,
            temperature, humidity, lightLevel, co2Level, soilPH, soilMoisture);
  }

  /**
   * Convenience: fetch by environment key from internal collection.
   */
  public SensorReading getSensorReadingForId(String environmentId) {
    EnvironmentSimulator env = environments.get(environmentId);
    return getSensorReading(env, environmentId);
  }
}