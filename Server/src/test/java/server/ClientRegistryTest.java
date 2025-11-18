package server;

import dto.NodeChange;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ClientRegistry.
 * <p>
 * This test class verifies session management and subscription logic for client connections.
 * It uses a mock sender to capture messages sent to clients.
 * <ul>
 *   <li>(addAndRemoveSessionTest): Tests adding and removing client sessions.</li>
 *   <li>(subscribeAndInterestedInTest): Tests event/node subscriptions and interest checks.</li>
 *   <li>(unsubscribeTest): Tests unsubscribing from events/nodes.</li>
 *   <li>(broadcastNodeChangeTest): Tests broadcasting node changes to interested sessions.</li>
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
        // No direct way to check, but should not throw
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
    void broadcastNodeChangeTest() {
        ClientRegistry.Session session = registry.addSession(sentMessages::add);
        session.subscribe(List.of("node_change"), List.of("*"));
        NodeChange nc = new NodeChange();
        nc.op = "added";
        registry.broadcast(nc);
        assertFalse(sentMessages.isEmpty());
    }
}
