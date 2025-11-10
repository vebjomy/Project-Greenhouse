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
  public void testSoilMoisture_increasesAndCapsAt100() throws Exception {
    EnvironmentSimulator env = new EnvironmentSimulator();

    // Stop the simulator's scheduler to avoid concurrent updates during the test
    java.lang.reflect.Field schedField = EnvironmentSimulator.class.getDeclaredField("scheduler");
    schedField.setAccessible(true);
    java.util.concurrent.ScheduledExecutorService sched =
            (java.util.concurrent.ScheduledExecutorService) schedField.get(env);
    sched.shutdownNow();
    try {
      sched.awaitTermination(100, java.util.concurrent.TimeUnit.MILLISECONDS);
    } catch (InterruptedException ignored) {}

    // Invoke the private updateSoilMoisture method repeatedly via reflection
    java.lang.reflect.Method updateMethod =
            EnvironmentSimulator.class.getDeclaredMethod("updateSoilMoisture");
    updateMethod.setAccessible(true);
    for (int i = 0; i < 20; i++) {
      updateMethod.invoke(env);
    }

    int moisture = env.getSoilMoisture();
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
