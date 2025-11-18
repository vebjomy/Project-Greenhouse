package core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Stores the current state of all greenhouse nodes, including sensor values. Provides listener
 * subscriptions for real-time sensor updates.
 */
public class ClientState {

  /**
   * Represents the state of a single greenhouse node.
   */
  public static class NodeState {

    public final String nodeId;
    public String name;
    public String location;
    public String ip;
    public final Set<String> sensors = ConcurrentHashMap.newKeySet();
    public final Set<String> actuators = ConcurrentHashMap.newKeySet();
    public final Map<String, Object> last = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param id node ID
     */
    public NodeState(String id) {
      this.nodeId = id;
    }

    public final Map<String, Double> sensorValues = new ConcurrentHashMap<>();
    public final List<Consumer<NodeState>> sensorListeners = new CopyOnWriteArrayList<>();
    public final StringProperty fanStatus = new SimpleStringProperty("N/A");
    public final StringProperty pumpStatus = new SimpleStringProperty("N/A");
    public final StringProperty co2Status = new SimpleStringProperty("N/A");
    public final StringProperty windowStatus = new SimpleStringProperty("N/A");
  }

  private final Map<String, NodeState> nodes = new ConcurrentHashMap<>();

  // listeners
  private final List<Consumer<NodeState>> sensorListeners = Collections.synchronizedList(
      new ArrayList<>());
  private final List<Consumer<NodeState>> nodeChangeListeners = Collections.synchronizedList(
      new ArrayList<>());

  /**
   * Gets or creates a NodeState for the given node ID.
   *
   * @param id node ID
   * @return NodeState instance
   */
  public NodeState upsertNode(String id) {
    return nodes.computeIfAbsent(id, NodeState::new);
  }

  /**
   * Updates node configuration (name, location, IP, sensors, actuators). This triggers nodeChange
   * listeners to update the UI.
   *
   * @param id        node ID
   * @param name      new name (can be null to keep current)
   * @param location  new location (can be null to keep current)
   * @param ip        new IP address (can be null to keep current)
   * @param sensors   new sensors list (can be null to keep current)
   * @param actuators new actuators list (can be null to keep current)
   */
  public void patchNode(String id, String name, String location, String ip,
      Collection<String> sensors, Collection<String> actuators) {
    NodeState n = upsertNode(id);

    boolean changed = false;

    if (name != null && !name.equals(n.name)) {
      n.name = name;
      changed = true;
      System.out.println("🔧 [ClientState] Updated name: " + name);
    }

    if (location != null && !location.equals(n.location)) {
      n.location = location;
      changed = true;
      System.out.println("🔧 [ClientState] Updated location: " + location);
    }

    if (ip != null && !ip.equals(n.ip)) {
      n.ip = ip;
      changed = true;
      System.out.println("🔧 [ClientState] Updated IP: " + ip);
    }

    if (sensors != null) {
      n.sensors.clear();
      n.sensors.addAll(sensors);
      changed = true;
      System.out.println("🔧 [ClientState] Updated sensors: " + sensors);
    }

    if (actuators != null) {
      n.actuators.clear();
      n.actuators.addAll(actuators);
      changed = true;
      System.out.println("🔧 [ClientState] Updated actuators: " + actuators);
    }

    // Notify listeners to update UI
    if (changed) {
      System.out.println("✅ [ClientState] Broadcasting node change for: " + id);
      notifyNodeChange(n);
    }
  }

  /**
   * Removes a node from the state and notifies listeners.
   *
   * @param id node ID
   */
  public void removeNode(String id) {
    NodeState n = nodes.remove(id);
    if (n != null) {
      notifyNodeChange(n);
    }
  }

  /**
   * Updates sensor values for a given node. This triggers sensor update listeners to update the
   * UI.
   *
   * @param nodeId the ID of the node
   * @param data   a map of sensor names to their new values
   */
  public void updateSensors(String nodeId, Map<String, Object> data) {
    NodeState ns = nodes.get(nodeId);
    if (ns == null) {
      return;
    }

    // Update sensor values on the JavaFX Application Thread
    Platform.runLater(() -> {
      data.forEach((key, value) -> {
        switch (key) {
          case "fan" -> ns.fanStatus.set(String.valueOf(value));
          case "water_pump" -> ns.pumpStatus.set(String.valueOf(value));
          case "co2" -> ns.co2Status.set(String.valueOf(value));
          case "window" -> ns.windowStatus.set(String.valueOf(value));
          default -> {
            // Other sensors as double values
            if (value instanceof Number num) {
              ns.sensorValues.put(key, num.doubleValue());
            }
          }
        }
      });

      // Notify both local and global listeners
      ns.sensorListeners.forEach(l -> l.accept(ns));
      notifySensor(ns);
    });
  }

  /**
   * Applies the last known sensor values for a given node.
   *
   * @param nodeId the ID of the node
   * @param data   a map of sensor names to their last known values
   */
  public void applyLastValues(String nodeId, Map<String, Object> data) {
    NodeState n = upsertNode(nodeId);
    n.last.clear();
    n.last.putAll(data);
    notifySensor(n);
  }

  // listeners

  /**
   * Registers a listener for sensor updates.
   *
   * @param l the listener to register
   */
  public void onSensorUpdate(Consumer<NodeState> l) {
    sensorListeners.add(l);
  }

  /**
   * Registers a listener for node changes.
   *
   * @param l the listener to register
   */
  public void onNodeChange(Consumer<NodeState> l) {
    nodeChangeListeners.add(l);
  }

  /**
   * Notifies all registered sensor update listeners.
   *
   * @param ns the NodeState with updated sensor values
   */
  private void notifySensor(NodeState ns) {
    synchronized (sensorListeners) {
      for (var l : sensorListeners) {
        l.accept(ns);
      }
    }
  }

  /**
   * Notifies all registered node change listeners.
   *
   * @param ns the NodeState that has changed
   */
  private void notifyNodeChange(NodeState ns) {
    synchronized (nodeChangeListeners) {
      for (var l : nodeChangeListeners) {
        l.accept(ns);
      }
    }
  }

  /**
   * Returns a live collection view of all current NodeState instances.
   *
   * @return a collection view of all known NodeState objects
   */
  public Collection<NodeState> allNodes() {
    return nodes.values();
  }

  /**
   * Gets a NodeState by ID.
   *
   * @param id node ID
   * @return NodeState instance or null if not found
   */
  public NodeState get(String id) {
    return nodes.get(id);
  }
}



