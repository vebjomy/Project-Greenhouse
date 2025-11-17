package server;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dto.NodeChange;
import dto.SensorUpdate;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import net.MessageCodec;

/**
 * Tracks active client connections and their subscriptions. Thread-safe and designed for
 * multi-client broadcasting.
 */
public class ClientRegistry {

  /**
   * Broadcast node_change to all interested sessions.
   *
   * @param nc the node change to broadcast
   */
  public void broadcast(NodeChange nc) {
    try {
      String line = codec.toJsonLine(nc);
      for (Session s : sessions.values()) {
        // Node id may be inside payload; we route on event only
        if (s.interestedIn("node_change", "*")) {
          s.send(line);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Represents one client session. */
  public static final class Session {
    public final String sessionId; // internal server id
    public volatile String clientId; // from client's "hello"
    private final ClientSender sender; // lambda to send line back to this client

    // event -> set of node IDs (or "*" for all)
    private final Map<String, Set<String>> subs = new ConcurrentHashMap<>();

    Session(String sessionId, ClientSender sender) {
      this.sessionId = sessionId;
      this.sender = sender;
    }

    /**
     * Subscribe to given events for given nodes.
     *
     * @param events the event types
     * @param nodes the node IDs
     */
    public void subscribe(Collection<String> events, Collection<String> nodes) {
      for (String ev : events) {
        subs.computeIfAbsent(ev, k -> new CopyOnWriteArraySet<>()).addAll(nodes);
      }
    }

    /**
     * Unsubscribe from given events for given nodes.
     *
     * @param events the event types
     * @param nodes the node IDs
     */
    public void unsubscribe(Collection<String> events, Collection<String> nodes) {
      for (String ev : events) {
        var set = subs.get(ev);
        if (set != null) {
          set.removeAll(nodes);
        }
      }
    }

    /**
     * Check if this session wants a specific event for a node.
     *
     * @param event the event type
     * @param nodeId the node ID
     * @return true if interested
     */
    public boolean interestedIn(String event, String nodeId) {
      var set = subs.get(event);
      if (set == null || set.isEmpty()) {
        return false;
      }
      return set.contains("*") || set.contains(nodeId);
    }

    /** Send a raw JSON line to this client. */
    public void send(String jsonLine) {
      sender.send(jsonLine);
    }
  }

  /** Simple functional interface to decouple handler IO. */
  @FunctionalInterface
  public interface ClientSender {
    /**
     * Send a raw JSON line to the client.
     *
     * @param jsonLine the JSON line to send
     */
    void send(String jsonLine);
  }

  private final Map<String, Session> sessions = new ConcurrentHashMap<>();
  private final MessageCodec codec = new MessageCodec();

  /**
   * Register a new session.
   *
   * @param sender the sender lambda
   * @return the created session
   */
  public Session addSession(ClientSender sender) {
    String sid = UUID.randomUUID().toString();
    Session s = new Session(sid, sender);
    sessions.put(sid, s);
    return s;
  }

  /**
   * Remove an existing session.
   *
   * @param s the session to remove
   */
  public void removeSession(Session s) {
    if (s != null) {
      sessions.remove(s.sessionId);
    }
  }

  /**
   * Broadcast sensor_update to all interested sessions.
   *
   * @param su the sensor update to broadcast
   */
  public void broadcastSensorUpdate(SensorUpdate su) {
    try {
      String line = codec.toJsonLine(su);
      for (Session s : sessions.values()) {
        if (s.interestedIn("sensor_update", su.nodeId)) {
          s.send(line);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Broadcast node_change to all interested sessions.
   *
   * @param nodeChangeJson the node change JSON to broadcast
   */
  public void broadcastNodeChange(ObjectNode nodeChangeJson) {
    String line = nodeChangeJson.toString() + "\n";
    for (Session s : sessions.values()) {
      // Node id may be inside payload; we route on event only
      if (s.interestedIn("node_change", "*")) {
        s.send(line);
      }
    }
  }
}
