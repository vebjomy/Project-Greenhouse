package server;


import net.MessageCodec;
import dto.SensorUpdate;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Tracks active client connections and their subscriptions.
 * Thread-safe and designed for multi-client broadcasting.
 */
public class ClientRegistry {

  /** Represents one client session */
  public static final class Session {
    public final String sessionId;        // internal server id
    public volatile String clientId;      // from client's "hello"
    private final ClientSender sender;    // lambda to send line back to this client

    // event -> set of node IDs (or "*" for all)
    private final Map<String, Set<String>> subs = new ConcurrentHashMap<>();

    Session(String sessionId, ClientSender sender) {
      this.sessionId = sessionId;
      this.sender = sender;
    }

    /** Subscribe to given events for given nodes. */
    public void subscribe(Collection<String> events, Collection<String> nodes) {
      for (String ev : events) {
        subs.computeIfAbsent(ev, k -> new CopyOnWriteArraySet<>()).addAll(nodes);
      }
    }

    /** Unsubscribe from given events for given nodes. */
    public void unsubscribe(Collection<String> events, Collection<String> nodes) {
      for (String ev : events) {
        var set = subs.get(ev);
        if (set != null) set.removeAll(nodes);
      }
    }

    /** Check if this session wants a specific event for a node */
    public boolean interestedIn(String event, String nodeId) {
      var set = subs.get(event);
      if (set == null || set.isEmpty()) return false;
      return set.contains("*") || set.contains(nodeId);
    }

    /** Send a raw JSON line to this client. */
    public void send(String jsonLine) {
      sender.send(jsonLine);
    }
  }

  /** Simple functional interface to decouple handler IO */
  @FunctionalInterface
  public interface ClientSender {
    void send(String jsonLine);
  }

  private final Map<String, Session> sessions = new ConcurrentHashMap<>();
  private final MessageCodec codec = new MessageCodec();

  /** Register a new session */
  public Session addSession(ClientSender sender) {
    String sid = UUID.randomUUID().toString();
    Session s = new Session(sid, sender);
    sessions.put(sid, s);
    return s;
  }

  /** Remove an existing session */
  public void removeSession(Session s) {
    if (s != null) sessions.remove(s.sessionId);
  }

  /** Broadcast sensor_update to all interested sessions */
  public void broadcastSensorUpdate(SensorUpdate su) {
    try {
      String line = codec.toJsonLine(su);
      for (Session s : sessions.values()) {
        if (s.interestedIn("sensor_update", su.nodeId)) {
          s.send(line);
        }
      }
    } catch (Exception e) { e.printStackTrace(); }
  }

  /** Broadcast node_change to all interested sessions */
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

