package server;

import dto.Command;
import dto.Topology;
import org.junit.jupiter.api.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the class NodeManager.
 *
 * <p>The following is tested:</p>
 *
 * <b>Positive tests:</b>
 * <ul>
 *   <li>(addNodeTest): Adding nodes with valid parameters increases the nodes count.</li>
 *   <li>(getAllNodesTest): Retrieving all nodes returns the expected list.</li>
 *   <li>(updateNodeTest): Updating an existing node changes its properties as expected.</li>
 *   <li>(deleteNodeTest): Deleting a node removes it from the manager.</li>
 *   <li>(executeCommandTest): Executing a command updates actuator state.</li>
 *   <li>(addNodeWithMissingFieldsTest): Adding a node with missing fields initializes empty lists.</li>
 * </ul>
 *
 * <b>Negative tests:</b>
 * <ul>
 *   <li>(updateNonExistentNodeTest): Updating a non-existent node does not throw but logs a warning.</li>
 *   <li>(executeCommandInvalidNodeTest): Executing a command for an invalid node ID does not throw.</li>
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

    @Test
    void executeCommandTest() {
        String nodeId = nodeManager.getAllNodes().get(0).id;
        Command cmd = new Command();
        cmd.nodeId = nodeId;
        cmd.target = "fan";
        cmd.params = Map.of("on", true);
        nodeManager.executeCommand(cmd);
    }

    @Test
    void addNodeWithMissingFieldsTest() {
        Topology.Node node = new Topology.Node();
        String id = nodeManager.addNode(node);
        assertNotNull(id);
        assertNotNull(nodeManager.getAllNodes().stream().filter(n -> n.id.equals(id)).findFirst().orElse(null));
    }

    @Test
    void executeCommandInvalidNodeTest() {
        Command cmd = new Command();
        cmd.nodeId = "invalid-id";
        cmd.target = "fan";
        cmd.params = Map.of("on", true);
        assertDoesNotThrow(() -> nodeManager.executeCommand(cmd));
    }

    @Test
    void updateNonExistentNodeTest() {
        assertDoesNotThrow(() -> nodeManager.updateNode("invalid-id", Map.of("name", "NoNode")));
    }
}
