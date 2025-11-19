package server;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the class NodeRuntime.
 *
 * <p>The following is tested:</p>
 *
 * <b>Positive tests:</b>
 * <ul>
 *   <li>(initializationTest): NodeRuntime initializes with correct defaults.</li>
 *   <li>(actuatorStateChangeTest): Actuator states and interval can be changed.</li>
 *   <li>(environmentStepTest): Stepping environment changes temperature.</li>
 * </ul>
 *
 * <b>Negative tests:</b>
 * <ul>
 *   <li>(invalidWindowAssignmentTest): Passing null window throws exception.</li>
 *   <li>(intervalBelowMinimumTest): Interval can be set below minimum by direct assignment.</li>
 *   <li>(actuatorToggleEdgeCaseTest): Actuator toggling edge cases are handled.</li>
 * </ul>
 */
class NodeRuntimeTest {
    @Test
    void initializationTest() {
        NodeRuntime rt = new NodeRuntime("node-xyz");
        assertEquals("node-xyz", rt.nodeId);
        assertNotNull(rt.env);
        assertFalse(rt.fanOn.get());
        assertFalse(rt.pumpOn.get());
        assertFalse(rt.co2On.get());
        assertEquals(EnvironmentState.WindowLevel.CLOSED, rt.window);
        assertEquals(1000, rt.intervalMs);
    }

    @Test
    void actuatorStateChangeTest() {
        NodeRuntime rt = new NodeRuntime("node-abc");
        rt.fanOn.set(true);
        rt.pumpOn.set(true);
        rt.co2On.set(true);
        rt.window = EnvironmentState.WindowLevel.OPEN;
        rt.intervalMs = 500;
        assertTrue(rt.fanOn.get());
        assertTrue(rt.pumpOn.get());
        assertTrue(rt.co2On.get());
        assertEquals(EnvironmentState.WindowLevel.OPEN, rt.window);
        assertEquals(500, rt.intervalMs);
    }

    @Test
    void environmentStepTest() {
        NodeRuntime rt = new NodeRuntime("node-step");
        double tempBefore = rt.env.getTemperatureC();
        rt.env.step(1.0, true, true, true, EnvironmentState.WindowLevel.OPEN);
        double tempAfter = rt.env.getTemperatureC();
        assertNotEquals(tempBefore, tempAfter);
    }

    @Test
    void invalidWindowAssignmentTest() {
        NodeRuntime rt = new NodeRuntime("node-neg");
        rt.window = null;
        assertThrows(NullPointerException.class, () -> {
            rt.env.step(1.0, false, false, false, rt.window);
        });
    }

    @Test
    void intervalBelowMinimumTest() {
        NodeRuntime rt = new NodeRuntime("node-interval");
        rt.intervalMs = 50;
        assertTrue(rt.intervalMs < 200);
    }

    @Test
    void actuatorToggleEdgeCaseTest() {
        NodeRuntime rt = new NodeRuntime("node-toggle");
        rt.fanOn.set(false);
        rt.fanOn.set(false);
        assertFalse(rt.fanOn.get());
        rt.fanOn.set(true);
        assertTrue(rt.fanOn.get());
    }
}
