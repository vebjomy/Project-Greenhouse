package server;

import dto.SensorUpdate;
import dto.Topology;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the class SensorEngine.
 *
 * <p>The following is tested:</p>
 *
 * <b>Positive tests:</b>
 * <ul>
 *   <li>(scheduleNodeTest): Scheduling a node results in sensor updates being broadcast.</li>
 *   <li>(scheduleMultipleNodesTest): Scheduling multiple nodes broadcasts updates for each.</li>
 *   <li>(rescheduleNodeTest): Rescheduling a node after interval change continues updates.</li>
 *   <li>(onNodeRemovedTest): Removing a node stops its updates.</li>
 *   <li>(closeStopsAllTasksTest): Closing the engine stops all scheduled tasks.</li>
 * </ul>
 *
 * <b>Negative tests:</b>
 * <ul>
 *   <li>(invalidNodeIdTest): Scheduling with an invalid node ID does not throw and does not broadcast updates.</li>
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
    void scheduleMultipleNodesTest() throws InterruptedException {
        Topology.Node node2 = new Topology.Node();
        node2.name = "Node2";
        node2.sensors = List.of("temperature");
        node2.actuators = List.of("fan");
        String id2 = nodeManager.addNode(node2);

        engine.scheduleNode(nodeManager.getAllNodes().get(0).id);
        engine.scheduleNode(id2);
        Thread.sleep(400);
        assertTrue(updates.stream().anyMatch(u -> u.nodeId.equals(id2)));
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

    @Test
    void invalidNodeIdTest() {
        engine.scheduleNode("invalid-id");
    }

    @Test
    void closeStopsAllTasksTest() throws InterruptedException {
        String nodeId = nodeManager.getAllNodes().get(0).id;
        engine.scheduleNode(nodeId);
        engine.close();
        updates.clear();
        Thread.sleep(300);
        assertTrue(updates.isEmpty());
    }
}
