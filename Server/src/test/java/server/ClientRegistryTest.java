package server;

import dto.NodeChange;
import dto.SensorUpdate;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the class ClientRegistry.
 *
 * <p>The following is tested:</p>
 *
 * <b>Positive tests:</b>
 * <ul>
 *   <li>(addAndRemoveSessionTest): Adding and removing client sessions works as expected.</li>
 *   <li>(subscribeAndInterestedInTest): Subscribing to events/nodes and interest checks work.</li>
 *   <li>(unsubscribeTest): Unsubscribing from events/nodes updates interest correctly.</li>
 *   <li>(multipleSessionsDifferentSubscriptionsTest): Multiple sessions with different subscriptions receive correct updates.</li>
 *   <li>(unsubscribeAllTest): Unsubscribing from all events/nodes stops updates.</li>
 *   <li>(broadcastSensorUpdateTest): Broadcasting sensor updates reaches interested sessions.</li>
 *   <li>(broadcastNodeChangeTest): Broadcasting node changes reaches interested sessions.</li>
 * </ul>
 *
 * <b>Negative tests:</b>
 * <ul>
 *   <li>(subscribeAndInterestedInTest): Interest checks for unsubscribed events/nodes return false.</li>
 *   <li>(unsubscribeTest): Unsubscribed nodes/events do not receive updates.</li>
 *   <li>(unsubscribeAllTest): No updates are sent to sessions after unsubscribing.</li>
 * </ul>
 */
class ClientRegistryTest {
    private ClientRegistry registry;
    private List<String> sentMessages;

    @BeforeEach
    void setUp() {
        registry = new ClientRegistry();
        sentMessages = new ArrayList<>();
    }

    @Test
    void addAndRemoveSessionTest() {
        ClientRegistry.Session session = registry.addSession(sentMessages::add);
        assertNotNull(session);
        registry.removeSession(session);
    }

    @Test
    void subscribeAndInterestedInTest() {
        ClientRegistry.Session session = registry.addSession(sentMessages::add);
        session.subscribe(List.of("node_change"), List.of("node1", "*"));
        assertTrue(session.interestedIn("node_change", "node1"));
        assertTrue(session.interestedIn("node_change", "*"));
        assertFalse(session.interestedIn("sensor_update", "node1"));
    }

    @Test
    void unsubscribeTest() {
        ClientRegistry.Session session = registry.addSession(sentMessages::add);
        session.subscribe(List.of("node_change"), List.of("node1", "node2"));
        session.unsubscribe(List.of("node_change"), List.of("node1"));
        assertFalse(session.interestedIn("node_change", "node1"));
        assertTrue(session.interestedIn("node_change", "node2"));
    }

    @Test
    void multipleSessionsDifferentSubscriptionsTest() {
        ClientRegistry.Session s1 = registry.addSession(sentMessages::add);
        ClientRegistry.Session s2 = registry.addSession(sentMessages::add);
        s1.subscribe(List.of("sensor_update"), List.of("node-1"));
        s2.subscribe(List.of("sensor_update"), List.of("node-2"));
        SensorUpdate su = new SensorUpdate();
        su.nodeId = "node-2";
        su.timestamp = System.currentTimeMillis();
        su.data = Map.of("temperature", 22);
        registry.broadcastSensorUpdate(su);
        assertTrue(sentMessages.size() >= 1);
    }

    @Test
    void unsubscribeAllTest() {
        ClientRegistry.Session session = registry.addSession(sentMessages::add);
        session.subscribe(List.of("sensor_update"), List.of("node-1"));
        session.unsubscribe(List.of("sensor_update"), List.of("node-1"));
        SensorUpdate su = new SensorUpdate();
        su.nodeId = "node-1";
        su.timestamp = System.currentTimeMillis();
        su.data = Map.of("temperature", 20);
        registry.broadcastSensorUpdate(su);
        assertTrue(sentMessages.isEmpty());
    }

    @Test
    void broadcastSensorUpdateTest() {
        ClientRegistry.Session session = registry.addSession(sentMessages::add);
        session.subscribe(List.of("sensor_update"), List.of("node-1"));
        SensorUpdate su = new SensorUpdate();
        su.nodeId = "node-1";
        su.timestamp = System.currentTimeMillis();
        su.data = Map.of("temperature", 25);
        registry.broadcastSensorUpdate(su);
        assertFalse(sentMessages.isEmpty());
    }

    @Test
    void broadcastNodeChangeTest() {
        ClientRegistry.Session session = registry.addSession(sentMessages::add);
        session.subscribe(List.of("node_change"), List.of("*"));
        NodeChange nc = new NodeChange();
        nc.op = "added";
        registry.broadcast(nc);
        assertFalse(sentMessages.isEmpty());
    }
}
