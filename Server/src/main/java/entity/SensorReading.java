package entity;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Typed sensor reading DTO suitable for database insertion.
 * Includes environment ID and timestamp.
 *
 * <p>Fields correspond to database column names for easy mapping.
 */
public class SensorReading {
  private final String environmentId;
  private final Instant timestamp;
  private final double temperature;
  private final double humidity;
  private final double lightLevel;
  private final double co2Level;
  private final double soilPh;
  private final double soilMoisture;

  /**
   * Constructor for SensorReading.
   *
   * @param environmentId ID of the environment where the reading was taken
   * @param timestamp     time of the reading
   * @param temperature   temperature value
   * @param humidity      humidity value
   * @param lightLevel    light level value
   * @param co2Level      CO2 level value
   * @param soilPh        soil pH value
   * @param soilMoisture  soil moisture value
   */
  public SensorReading(String environmentId,
                       Instant timestamp,
                       double temperature,
                       double humidity,
                       double lightLevel,
                       double co2Level,
                       double soilPh,
                       double soilMoisture) {
    this.environmentId = environmentId;
    this.timestamp = timestamp;
    this.temperature = temperature;
    this.humidity = humidity;
    this.lightLevel = lightLevel;
    this.co2Level = co2Level;
    this.soilPh = soilPh;
    this.soilMoisture = soilMoisture;
  }

  // getters
  public String getEnvironmentId() {
    return environmentId;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public double getTemperature() {
    return temperature;
  }

  public double getHumidity() {
    return humidity;
  }

  public double getLightLevel() {
    return lightLevel;
  }

  public double getCo2Level() {
    return co2Level;
  }

  public double getSoilPh() {
    return soilPh;
  }

  public double getSoilMoisture() {
    return soilMoisture;
  }

  /**
   * Convert to a Map with String and Object where keys match DB column names.
   * Useful for prepared statements or generic DB mappers.
   */
  public Map<String, Object> toMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("environment_id", environmentId);
    map.put("timestamp", timestamp);
    map.put("temperature", temperature);
    map.put("humidity", humidity);
    map.put("light_level", lightLevel);
    map.put("co2_level", co2Level);
    map.put("soil_ph", soilPh);
    map.put("soil_moisture", soilMoisture);
    return map;
  }


  @Override
  public String toString() {
    return "SensorReading{"
            + "environmentId='" + environmentId + '\''
            + ", timestamp=" + timestamp
            + ", temperature=" + temperature
            + ", humidity=" + humidity
            + ", lightLevel=" + lightLevel
            + ", co2Level=" + co2Level
            + ", soilPH=" + soilPh
            + ", soilMoisture=" + soilMoisture
            + '}';
  }
}