package server;

import dto.SensorUpdate;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SensorEngine.
 * <p>
 * This test class verifies node scheduling, broadcasting sensor updates, and node removal.
 * <ul>
 *   <li>(scheduleNodeTest): Tests scheduling a node and receiving sensor updates.</li>
 *   <li>(rescheduleNodeTest): Tests rescheduling a node after interval change.</li>
 *   <li>(onNodeRemovedTest): Tests node removal stops updates.</li>
 * </ul>
 */
class SensorEngineTest {
    private NodeManager nodeManager;
    private List<SensorUpdate> updates;
    private SensorEngine engine;

    @BeforeEach
    void setUp() {
        nodeManager = new NodeManager();
        updates = Collections.synchronizedList(new ArrayList<>());
        engine = new SensorEngine(nodeManager, updates::add);
    }

    @AfterEach
    void tearDown() {
        engine.close();
    }

    @Test
    void scheduleNodeTest() throws InterruptedException {
        String nodeId = nodeManager.getAllNodes().get(0).id;
        engine.scheduleNode(nodeId);
        Thread.sleep(300); // Wait for a few ticks
        assertFalse(updates.isEmpty());
        assertEquals(nodeId, updates.get(0).nodeId);
    }

    @Test
    void rescheduleNodeTest() throws InterruptedException {
        String nodeId = nodeManager.getAllNodes().get(0).id;
        engine.scheduleNode(nodeId);
        nodeManager.setSampling(nodeId, 200);
        engine.rescheduleNode(nodeId);
        Thread.sleep(300);
        assertTrue(updates.size() > 0);
    }

    @Test
    void onNodeRemovedTest() throws InterruptedException {
        String nodeId = nodeManager.getAllNodes().get(0).id;
        engine.scheduleNode(nodeId);
        engine.onNodeRemoved(nodeId);
        updates.clear();
        Thread.sleep(300);
        assertTrue(updates.isEmpty());
    }
}
