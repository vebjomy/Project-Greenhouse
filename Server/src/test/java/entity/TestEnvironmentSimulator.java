package entity;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

/**
 * Test class for EnvironmentSimulator.
 * Used to verify the behavior of the environment simulation in various scenarios.
 * <p>
 * The following is tested:
 * <ul>
 *   <li>Temperature fluctuations over a 24-hour cycle.
 * </ul>
 */
public class TestEnvironmentSimulator {
  @Test
  public void testTemperatureFluctuations() {
    EnvironmentSimulator envSim = new EnvironmentSimulator();

    // Simulate a full day in 1-hour increments
    for (int hour = 0; hour < 24; hour++) {
      envSim.setCycleSecond(hour * 5); // Each hour corresponds to 5 cycle seconds
      int temp = envSim.getTemperature();

      // Check expected temperature ranges
      if (hour >= 6 && hour <= 18) { // Daytime
        assert (temp >= 20 && temp <= 30);
      } else { // Nighttime
        assert (temp >= 15 && temp <= 25);
      }
    }
  }

  @Test
  public void testTemperatureTransition_increaseThenDecrease() {
    EnvironmentSimulator env = new EnvironmentSimulator();
    env.setTemperature(20); // known baseline

    // Daytime (second < 60) increases by second/6
    env.updateTemperatureState(30); // +30/6 = +5
    assertEquals(25, env.getTemperature());

    // Nighttime (second >= 60) decreases by (second-60)/6 from current value
    env.updateTemperatureState(90); // -(90-60)/6 = -5 -> back to 20
    assertEquals(20, env.getTemperature());
  }

  @Test
  public void testHumiditySinusoidal_valuesAtKeyPoints() {
    EnvironmentSimulator env = new EnvironmentSimulator();

    env.updateHumidityState(0);   // sin(0) = 0 -> 50
    assertEquals(50, env.getHumidity());

    env.updateHumidityState(30);  // sin(90°) = 1 -> 60
    assertEquals(60, env.getHumidity());

    env.updateHumidityState(60);  // sin(180°) = 0 -> 50
    assertEquals(50, env.getHumidity());

    env.updateHumidityState(90);  // sin(270°) = -1 -> 40
    assertEquals(40, env.getHumidity());
  }

  @Test
  public void testCo2DefaultBehaviour_atKeyPoints() {
    EnvironmentSimulator env = new EnvironmentSimulator();

    env.updateCo2Level(0);   // 400 + 100*sin(0) = 400
    assertEquals(400, getCo2(env));

    env.updateCo2Level(30);  // 400 + 100*sin(90°) = 500
    assertEquals(500, getCo2(env));

    env.updateCo2Level(90);  // 400 - 100*sin(90°) = 300 (night branch)
    assertEquals(300, getCo2(env));
  }

  // helper reads CO2 via reflection because field is private
  private int getCo2(EnvironmentSimulator env) {
    try {
      java.lang.reflect.Field f = EnvironmentSimulator.class.getDeclaredField("co2Level");
      f.setAccessible(true);
      return f.getInt(env);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testSoilMoisture_increasesAndCapsAt100() {
    EnvironmentSimulator env = new EnvironmentSimulator();

    // default soilMoisture is 40; pumpOn defaults to true
    for (int i = 0; i < 20; i++) {
      env.updateSoilMoisture();
    }
    // should not exceed 100
    int moisture = getSoilMoisture(env);
    assertTrue("soil moisture should be <= 100", moisture <= 100);
    assertEquals(100, moisture);
  }

  // helper reads soilMoisture via reflection because field is private
  private int getSoilMoisture(EnvironmentSimulator env) {
    try {
      java.lang.reflect.Field f = EnvironmentSimulator.class.getDeclaredField("soilMoisture");
      f.setAccessible(true);
      return f.getInt(env);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetTemperature_invalidThrows() {
    EnvironmentSimulator env = new EnvironmentSimulator();
    env.setTemperature(-274); // physically impossible -> exception
  }

  @Test
  public void testSetCycleSecond_bounds() {
    EnvironmentSimulator env = new EnvironmentSimulator();

    // valid bounds
    env.setCycleSecond(0);
    env.setCycleSecond(120);

    // invalid should throw
    try {
      env.setCycleSecond(121);
      fail("Expected IllegalArgumentException for cycle second > 120");
    } catch (IllegalArgumentException ignored) {
    }

    try {
      env.setCycleSecond(-1);
      fail("Expected IllegalArgumentException for cycle second < 0");
    } catch (IllegalArgumentException ignored) {
    }
  }
}
