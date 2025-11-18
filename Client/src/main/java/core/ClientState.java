package core;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Stores the current state of all greenhouse nodes, including sensor values.
 * Provides listener subscriptions for real-time sensor updates.
 */
public class ClientState {

  public static class NodeState {
    public final String nodeId;
    public String name;
    public String location;
    public String ip;
    public final Set<String> sensors = ConcurrentHashMap.newKeySet();
    public final Set<String> actuators = ConcurrentHashMap.newKeySet();
    public final Map<String, Object> last = new ConcurrentHashMap<>();
    public NodeState(String id){ this.nodeId = id; }
    public final Map<String, Double> sensorValues = new ConcurrentHashMap<>();
    public final List<Consumer<NodeState>> sensorListeners = new CopyOnWriteArrayList<>();
    public final StringProperty fanStatus = new SimpleStringProperty("N/A");
    public final StringProperty pumpStatus = new SimpleStringProperty("N/A");
    public final StringProperty co2Status = new SimpleStringProperty("N/A");
    public final StringProperty windowStatus = new SimpleStringProperty("N/A");
  }

  private final Map<String, NodeState> nodes = new ConcurrentHashMap<>();

  // listeners
  private final List<Consumer<NodeState>> sensorListeners = Collections.synchronizedList(new ArrayList<>());
  private final List<Consumer<NodeState>> nodeChangeListeners = Collections.synchronizedList(new ArrayList<>());

  public NodeState upsertNode(String id){
    return nodes.computeIfAbsent(id, NodeState::new);
  }

  /**
   * Updates node configuration (name, location, IP, sensors, actuators).
   * This triggers nodeChange listeners to update the UI.
   *
   * @param id node ID
   * @param name new name (can be null to keep current)
   * @param location new location (can be null to keep current)
   * @param ip new IP address (can be null to keep current)
   * @param sensors new sensors list (can be null to keep current)
   * @param actuators new actuators list (can be null to keep current)
   */
  public void patchNode(String id, String name, String location, String ip,
                        Collection<String> sensors, Collection<String> actuators){
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

    //Notify listeners to update UI
    if (changed) {
      System.out.println("✅ [ClientState] Broadcasting node change for: " + id);
      notifyNodeChange(n);
    }
  }

  public void removeNode(String id){
    NodeState n = nodes.remove(id);
    if (n != null) notifyNodeChange(n);
  }

  public void updateSensors(String nodeId, Map<String, Object> data) {
    NodeState ns = nodes.get(nodeId);
    if (ns == null) return;

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

  public void applyLastValues(String nodeId, Map<String,Object> data){
    NodeState n = upsertNode(nodeId);
    n.last.clear();
    n.last.putAll(data);
    notifySensor(n);
  }

  // listeners
  public void onSensorUpdate(Consumer<NodeState> l){ sensorListeners.add(l); }
  public void onNodeChange(Consumer<NodeState> l){ nodeChangeListeners.add(l); }

  private void notifySensor(NodeState ns){
    synchronized (sensorListeners){ for (var l: sensorListeners) l.accept(ns); }
  }
  private void notifyNodeChange(NodeState ns){
    synchronized (nodeChangeListeners){ for (var l: nodeChangeListeners) l.accept(ns); }
  }

  public Collection<NodeState> allNodes(){ return nodes.values(); }
  public NodeState get(String id){ return nodes.get(id); }
}



