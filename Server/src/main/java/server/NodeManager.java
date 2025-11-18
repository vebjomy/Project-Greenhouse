package server;

import dto.Command;
import dto.SensorUpdate;
import dto.Topology;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory nodes + runtimes. Applies patches and executes commands. */
public class NodeManager {
  private final Map<String, Topology.Node> nodes = new ConcurrentHashMap<>();
  private final Map<String, NodeRuntime> runtimes = new ConcurrentHashMap<>();

  /** Constructor initializes with a demo node. */
  public NodeManager() {
    // Seed one demo node
    Topology.Node n = new Topology.Node();
    n.id = "node-1";
    n.name = "Demo Greenhouse";
    n.location = "Central";
    n.ip = "127.0.0.1";
    n.sensors = new ArrayList<>(List.of("temperature", "humidity", "light", "ph"));
    n.actuators = new ArrayList<>(List.of("fan", "water_pump", "co2", "window"));
    nodes.put(n.id, n);
    runtimes.put(n.id, new NodeRuntime(n.id));
  }

  /**
   * Add a new node and return its assigned ID.
   *
   * @param node the node to add
   * @return the assigned node ID
   */
  public synchronized String addNode(Topology.Node node) {
    System.out.println("🔧 [NodeManager] addNode called");
    System.out.println("   Input node: " + node.name);
    System.out.println("   Current nodes count: " + nodes.size());

    String id = "node-" + (nodes.size() + 1);
    node.id = id;

    // Initialize sensors/actuators lists if null
    if (node.sensors == null) {
      node.sensors = new ArrayList<>();
      System.out.println("   ⚠️ Initialized empty sensors list");
    }
    if (node.actuators == null) {
      node.actuators = new ArrayList<>();
      System.out.println("   ⚠️ Initialized empty actuators list");
    }

    System.out.println("   Assigned ID: " + id);
    System.out.println("   Node sensors: " + node.sensors);
    System.out.println("   Node actuators: " + node.actuators);

    // Add to map
    nodes.put(id, node);
    System.out.println("   ✅ Node added to map");
    System.out.println("   New nodes count: " + nodes.size());

    // Create runtime
    runtimes.put(id, new NodeRuntime(id));
    System.out.println("   ✅ Runtime created");

    // Verification
    if (nodes.containsKey(id)) {
      System.out.println("   ✅ Verification: Node IS in map");
      System.out.println("   ✅ Node details: " + nodes.get(id).name);
    } else {
      System.err.println("   ❌ ERROR: Node NOT in map!");
    }

    return id;
  }

  /**
   * Returns a list containing all nodes.
   *
   * @return a list containing all nodes
   */
  public List<Topology.Node> getAllNodes() {
    System.out.println("🔧 [NodeManager] getAllNodes called");
    System.out.println("   Nodes in map: " + nodes.size());

    List<Topology.Node> result = new ArrayList<>(nodes.values());

    System.out.println("   Returning list with: " + result.size() + " nodes");
    if (!result.isEmpty()) {
      System.out.println("   First node: " + result.get(0).name);
    }

    return result;
  }

  /**
   * Update an existing node with a patch map.
   * Supports updating: name, location, ip, sensors (full list), actuators (full list).
   *
   * @param nodeId the ID of the node to update
   * @param patch the patch map with fields to update
   */
  public synchronized void updateNode(String nodeId, Map<String, Object> patch) {
    var n = nodes.get(nodeId);
    if (n == null) {
      System.err.println("⚠️ Cannot update node: " + nodeId + " not found");
      return;
    }

    System.out.println("🔧 [NodeManager] Updating node: " + nodeId);

    // Update basic properties
    if (patch.containsKey("name")) {
      n.name = Objects.toString(patch.get("name"), n.name);
      System.out.println("   Updated name: " + n.name);
    }
    if (patch.containsKey("location")) {
      n.location = Objects.toString(patch.get("location"), n.location);
      System.out.println("   Updated location: " + n.location);
    }
    if (patch.containsKey("ip")) {
      n.ip = Objects.toString(patch.get("ip"), n.ip);
      System.out.println("   Updated IP: " + n.ip);
    }

    // Patch sensors array (replaces entire list)
    if (patch.containsKey("sensors")) {
      Object sensorsObj = patch.get("sensors");
      if (sensorsObj instanceof List<?>) {
        n.sensors = new ArrayList<>();
        for (Object item : (List<?>) sensorsObj) {
          n.sensors.add(Objects.toString(item, ""));
        }
        System.out.println("   Updated sensors: " + n.sensors);
      }
    }

    // Patch actuators array (replaces entire list)
    if (patch.containsKey("actuators")) {
      Object actuatorsObj = patch.get("actuators");
      if (actuatorsObj instanceof List<?>) {
        n.actuators = new ArrayList<>();
        for (Object item : (List<?>) actuatorsObj) {
          n.actuators.add(Objects.toString(item, ""));
        }
        System.out.println("   Updated actuators: " + n.actuators);
      }
    }

    System.out.println("   ✅ Node updated successfully");
  }

  /**
   * Delete a node by its ID.
   *
   * @param nodeId the ID of the node to delete
   */
  public synchronized void deleteNode(String nodeId) {
    nodes.remove(nodeId);
    runtimes.remove(nodeId);
  }


  /**
   * Set the sampling interval for a node's runtime.
   *
   * @param nodeId the ID of the node
   * @param intervalMs the sampling interval in milliseconds
   */
  public synchronized void setSampling(String nodeId, int intervalMs) {
    var rt = runtimes.get(nodeId);
    if (rt != null) {
      rt.intervalMs = Math.max(200, intervalMs);
    }
  }

  /** Get the runtime for a node by its ID. */
  public NodeRuntime getRuntime(String nodeId) {
    return runtimes.get(nodeId);
  }

  /** Advance the environment for a node by dt seconds. */
  public void advance(String nodeId, double dtSeconds) {
    var rt = runtimes.get(nodeId);
    if (rt == null) {
      return;
    }
    rt.env.step(dtSeconds, rt.fanOn.get(), rt.pumpOn.get(), rt.co2On.get(), rt.window);
  }

  /** Return a snapshot map for building SensorUpdate.data. */
  public Map<String, Object> snapshot(String nodeId) {
    var rt = runtimes.get(nodeId);
    if (rt == null) {
      return Map.of();
    }
    var env = rt.env;
    // Sensor readings
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("temperature", env.getTemperatureC());
    data.put("humidity", env.getHumidityPct());
    data.put("light", env.getLightLux());
    data.put("ph", env.getPh());
    // Actuator states
    data.put("fan", rt.fanOn.get() ? "ON" : "OFF");
    data.put("water_pump", rt.pumpOn.get() ? "ON" : "OFF");
    data.put("co2", rt.co2On.get() ? "ON" : "OFF");
    data.put("window", rt.window.toString()); // "OPEN", "CLOSED", "PARTIAL"
    return data;
  }

  /** Apply actuator command according to protocol. */
  public void executeCommand(Command cmd) {
    var rt = runtimes.get(cmd.nodeId);
    if (rt == null) {
      return;
    }

    switch (cmd.target) {
      case "fan" -> rt.fanOn.set(asBool(cmd.params.get("on")));
      case "water_pump" -> rt.pumpOn.set(asBool(cmd.params.get("on")));
      case "co2" -> rt.co2On.set(asBool(cmd.params.get("on")));
      case "window" -> {
        var level = String.valueOf(cmd.params.get("level"));
        try {
          rt.window = EnvironmentState.WindowLevel.valueOf(level);
        } catch (Exception ignored) {
          /* invalid level -> ignore */
        }
      }
      default -> System.out.println("Unknown actuator: " + cmd.target);
    }
    SensorUpdate immediate = new SensorUpdate();
    immediate.nodeId = cmd.nodeId;
    immediate.timestamp = System.currentTimeMillis();
    immediate.data = snapshot(cmd.nodeId);
  }

  /**
   * Convert an object to boolean. Handles Boolean and String "true"/"false".
   *
   * @param v the object to convert
   * @return the boolean value
   */
  private boolean asBool(Object v) {
    return (v instanceof Boolean b && b) || "true".equalsIgnoreCase(String.valueOf(v));
  }
}
