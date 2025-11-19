package server;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the class EnvironmentState.
 *
 * <p>The following is tested:</p>
 *
 * <b>Positive tests:</b>
 * <ul>
 *   <li>(initialValuesTest): Initial sensor values are correct.</li>
 *   <li>(stepWithActuatorsTest): Stepping with actuators changes values within physical bounds.</li>
 *   <li>(windowLevelEffectTest): Window level affects temperature as expected.</li>
 *   <li>(phBoundsTest): pH remains within valid bounds after repeated steps.</li>
 * </ul>
 *
 * <b>Negative tests:</b>
 * <ul>
 *   <li>(nullWindowLevelThrowsTest): Passing null as window level throws NullPointerException.</li>
 *   <li>(extremeDtSecondsDoesNotThrowTest): Extreme dtSeconds does not throw and clamped values stay in bounds.</li>
 *   <li>(invalidEnumCastThrowsTest): Invalid enum cast throws ClassCastException.</li>
 * </ul>
 */
class EnvironmentStateTest {
    private EnvironmentState env;

    @BeforeEach
    void setUp() {
        env = new EnvironmentState();
    }

    @Test
    void initialValuesTest() {
        assertEquals(22.0, env.getTemperatureC(), 0.5);
        assertEquals(55.0, env.getHumidityPct(), 1.0);
        assertEquals(420, env.getLightLux(), 50);
        assertEquals(8.4, env.getPh(), 0.2);
    }

    @Test
    void stepWithActuatorsTest() {
        env.step(1.0, true, true, true, EnvironmentState.WindowLevel.OPEN);
        // Values should change, but remain within physical bounds
        assertTrue(env.getTemperatureC() > 8.0 && env.getTemperatureC() < 40.0);
        assertTrue(env.getHumidityPct() >= 0.0 && env.getHumidityPct() <= 100.0);
        assertTrue(env.getLightLux() >= 50 && env.getLightLux() <= 50000);
        assertTrue(env.getPh() >= 0.0 && env.getPh() <= 14.0);
    }

    @Test
    void windowLevelEffectTest() {
        double tempClosed = env.getTemperatureC();
        env.step(1.0, false, false, false, EnvironmentState.WindowLevel.OPEN);
        double tempOpen = env.getTemperatureC();
        assertNotEquals(tempClosed, tempOpen);
    }

    @Test
    void phBoundsTest() {
        for (int i = 0; i < 100; i++) {
            env.step(1.0, false, true, true, EnvironmentState.WindowLevel.CLOSED);
        }
        assertTrue(env.getPh() >= 0.0 && env.getPh() <= 14.0);
    }

    @Test
    void nullWindowLevelThrowsTest() {
        assertThrows(NullPointerException.class, () -> {
            env.step(1.0, false, false, false, null);
        });
    }

    @Test
    void extremeDtSecondsDoesNotThrowTest() {
        assertDoesNotThrow(() -> {
            env.step(1e6, false, false, false, EnvironmentState.WindowLevel.CLOSED);
        });
        // Only check values that are clamped in EnvironmentState
        assertTrue(env.getHumidityPct() >= 0.0 && env.getHumidityPct() <= 100.0);
        assertTrue(env.getLightLux() >= 50 && env.getLightLux() <= 50000);
        assertTrue(env.getPh() >= 0.0 && env.getPh() <= 14.0);
    }

    @Test
    void invalidEnumCastThrowsTest() {
        assertThrows(ClassCastException.class, () -> {
            Object invalid = "NOT_A_WINDOW_LEVEL";
            env.step(1.0, false, false, false, (EnvironmentState.WindowLevel) invalid);
        });
    }
}
