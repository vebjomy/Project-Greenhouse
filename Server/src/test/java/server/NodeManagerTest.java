package server;

import dto.Command;
import dto.Topology;
import org.junit.jupiter.api.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NodeManager.
 * <p>
 * This test class verifies node addition, update, deletion, and snapshot logic.
 * <ul>
 *   <li>(addNodeTest): Tests adding a new node.</li>
 *   <li>(getAllNodesTest): Tests retrieval of all nodes.</li>
 *   <li>(updateNodeTest): Tests updating node properties.</li>
 *   <li>(deleteNodeTest): Tests deleting a node.</li>
 *   <li>(snapshotTest): Tests snapshot data for a node.</li>
 * </ul>
 */
class NodeManagerTest {
    private NodeManager nodeManager;

    @BeforeEach
    void setUp() {
        nodeManager = new NodeManager();
    }

    @Test
    void addNodeTest() {
        Topology.Node node = new Topology.Node();
        node.name = "Test Node";
        node.location = "Lab";
        node.ip = "192.168.1.10";
        node.sensors = List.of("temperature");
        node.actuators = List.of("fan");
        String id = nodeManager.addNode(node);
        assertNotNull(id);
        assertTrue(nodeManager.getAllNodes().stream().anyMatch(n -> n.id.equals(id)));
    }

    @Test
    void getAllNodesTest() {
        List<Topology.Node> nodes = nodeManager.getAllNodes();
        assertFalse(nodes.isEmpty());
    }

    @Test
    void updateNodeTest() {
        Topology.Node node = new Topology.Node();
        node.name = "Old Name";
        node.location = "Old Location";
        node.ip = "10.0.0.1";
        node.sensors = List.of("humidity");
        node.actuators = List.of("pump");
        String id = nodeManager.addNode(node);

        Map<String, Object> patch = new HashMap<>();
        patch.put("name", "New Name");
        patch.put("location", "New Location");
        patch.put("ip", "10.0.0.2");
        patch.put("sensors", List.of("light"));
        patch.put("actuators", List.of("window"));
        nodeManager.updateNode(id, patch);

        Topology.Node updated = nodeManager.getAllNodes().stream().filter(n -> n.id.equals(id)).findFirst().orElse(null);
        assertNotNull(updated);
        assertEquals("New Name", updated.name);
        assertEquals("New Location", updated.location);
        assertEquals("10.0.0.2", updated.ip);
        assertEquals(List.of("light"), updated.sensors);
        assertEquals(List.of("window"), updated.actuators);
    }

    @Test
    void deleteNodeTest() {
        Topology.Node node = new Topology.Node();
        node.name = "ToDelete";
        node.location = "Room";
        node.ip = "10.0.0.3";
        node.sensors = List.of("ph");
        node.actuators = List.of("co2");
        String id = nodeManager.addNode(node);
        nodeManager.deleteNode(id);
        assertFalse(nodeManager.getAllNodes().stream().anyMatch(n -> n.id.equals(id)));
    }

    @Test
    void snapshotTest() {
        List<Topology.Node> nodes = nodeManager.getAllNodes();
        assertFalse(nodes.isEmpty());
        Topology.Node node = nodes.get(0);
        Map<String, Object> snapshot = nodeManager.snapshot(node.id);
        assertTrue(snapshot.containsKey("temperature"));
        assertTrue(snapshot.containsKey("humidity"));
        assertTrue(snapshot.containsKey("light"));
        assertTrue(snapshot.containsKey("ph"));
        assertTrue(snapshot.containsKey("fan"));
        assertTrue(snapshot.containsKey("water_pump"));
        assertTrue(snapshot.containsKey("co2"));
        assertTrue(snapshot.containsKey("window"));
    }
}
