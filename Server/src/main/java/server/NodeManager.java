package server;

import dto.Command;
import dto.Topology;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory nodes + runtimes. Applies patches and executes commands.
 */
public class NodeManager {
  private final Map<String, Topology.Node> nodes = new ConcurrentHashMap<>();
  private final Map<String, NodeRuntime> runtimes = new ConcurrentHashMap<>();

  public NodeManager() {
    // Seed one demo node
    Topology.Node n = new Topology.Node();
    n.id = "node-1";
    n.name = "Demo Greenhouse";
    n.location = "Central";
    n.ip = "127.0.0.1";
    n.sensors = new ArrayList<>(List.of("temperature","humidity","light","ph"));
    n.actuators = new ArrayList<>(List.of("fan","water_pump","co2","window"));
    nodes.put(n.id, n);
    runtimes.put(n.id, new NodeRuntime(n.id));
  }

  public synchronized String addNode(Topology.Node node) {
    String id = "node-" + (nodes.size() + 1);
    node.id = id;
    if (node.sensors == null) node.sensors = new ArrayList<>();
    if (node.actuators == null) node.actuators = new ArrayList<>();
    nodes.put(id, node);
    runtimes.put(id, new NodeRuntime(id));
    return id;
  }

  public synchronized void updateNode(String nodeId, Map<String,Object> patch) {
    var n = nodes.get(nodeId);
    if (n == null) return;
    if (patch.containsKey("name")) n.name = Objects.toString(patch.get("name"), n.name);
    if (patch.containsKey("location")) n.location = Objects.toString(patch.get("location"), n.location);
    if (patch.containsKey("ip")) n.ip = Objects.toString(patch.get("ip"), n.ip);
    // Optionally patch sensors/actuators arrays
  }

  public synchronized void deleteNode(String nodeId) {
    nodes.remove(nodeId);
    runtimes.remove(nodeId);
  }

  public synchronized void addComponent(String nodeId, Map<String,Object> component) {
    var n = nodes.get(nodeId);
    if (n == null) return;
    String kind = Objects.toString(component.get("kind"), "");
    String name = Objects.toString(component.get("name"), "");
    if ("sensor".equals(kind)) {
      if (!n.sensors.contains(name)) n.sensors.add(name);
    } else if ("actuator".equals(kind)) {
      if (!n.actuators.contains(name)) n.actuators.add(name);
    }
  }

  public synchronized void removeComponent(String nodeId, Map<String,Object> component) {
    var n = nodes.get(nodeId);
    if (n == null) return;
    String kind = Objects.toString(component.get("kind"), "");
    String name = Objects.toString(component.get("name"), "");
    if ("sensor".equals(kind)) {
      n.sensors.remove(name);
    } else if ("actuator".equals(kind)) {
      n.actuators.remove(name);
    }
  }

  public synchronized void setSampling(String nodeId, int intervalMs) {
    var rt = runtimes.get(nodeId);
    if (rt != null) rt.intervalMs = Math.max(200, intervalMs);
  }

  public List<Topology.Node> getAllNodes() { return new ArrayList<>(nodes.values()); }

  public NodeRuntime getRuntime(String nodeId) { return runtimes.get(nodeId); }

  /** Advance the environment for a node by dt seconds. */
  public void advance(String nodeId, double dtSeconds) {
    var rt = runtimes.get(nodeId);
    if (rt == null) return;
    rt.env.step(dtSeconds, rt.fanOn.get(), rt.pumpOn.get(), rt.co2On.get(), rt.window);
  }

  /** Return a snapshot map for building SensorUpdate.data */
  public Map<String,Object> snapshot(String nodeId) {
    var rt = runtimes.get(nodeId);
    if (rt == null) return Map.of();
    var env = rt.env;
    // Sensor readings
    Map<String,Object> data = new LinkedHashMap<>();
    data.put("temperature", env.temperatureC);
    data.put("humidity", env.humidityPct);
    data.put("light", env.lightLux);
    data.put("ph", env.ph);
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
    if (rt == null) return;

    switch (cmd.target) {
      case "fan" -> rt.fanOn.set(asBool(cmd.params.get("on")));
      case "water_pump" -> rt.pumpOn.set(asBool(cmd.params.get("on")));
      case "co2" -> rt.co2On.set(asBool(cmd.params.get("on")));
      case "window" -> {
        var level = String.valueOf(cmd.params.get("level"));
        try {
          rt.window = EnvironmentState.WindowLevel.valueOf(level);
        } catch (Exception ignored) { /* invalid level -> ignore */ }
      }
      default -> System.out.println("Unknown actuator: " + cmd.target);
    }
  }

  private boolean asBool(Object v) {
    return (v instanceof Boolean b && b) || "true".equalsIgnoreCase(String.valueOf(v));
  }
}


