package core;

import dto.*;
import net.MessageTypes;
import net.NetworkClient;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * High-level client API for GUI: topology, subscriptions, commands, pull.
 * Works with real TCP server or local MockServer (development mode).
 */
public class ClientApi implements AutoCloseable {
  private final ClientState state = new ClientState();
  private final NetworkClient tcp = new NetworkClient();
  private final RequestManager requests = new RequestManager();


  public ClientApi(){
    tcp.setOnLine(this::handleLine);
    tcp.setOnError(err -> System.err.println("TCP error: " + err));
  }

  public CompletableFuture<Void> connect(String host, int port) {
    try {
      tcp.connect(host, port);
      System.out.println("Connecting to: " + host + ":" + port);

      // Send "hello" message
      String id = requests.newId();
      CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);

      Hello h = new Hello();
      h.id = id;
      h.clientId = "ui-" + UUID.randomUUID();
      h.user = "local";
      h.capabilities = List.of("topology", "commands", "subscribe");
      send(h);

      fut.thenRun(() -> System.out.println("Connected to GreenhouseServer ✔"));
      return fut;
    } catch (IOException e) {
      CompletableFuture<Void> f = new CompletableFuture<>();
      f.completeExceptionally(e);
      return f;
    }
  }




  // ---------- Listeners for GUI ----------
  public void onSensorUpdate(Consumer<ClientState.NodeState> l){
    state.onSensorUpdate(ns -> Platform.runLater(() -> l.accept(ns)));
  }
  public void onNodeChange(Consumer<ClientState.NodeState> l){
    state.onNodeChange(ns -> Platform.runLater(() -> l.accept(ns)));
  }

  public ClientState state(){ return state; }


  // ---------- Topology ----------

  public CompletableFuture<Topology> getTopology(){
    System.out.println("📤 [ClientApi] Requesting topology...");

    String id = requests.newId();
    System.out.println("   Request ID: " + id);

    var fut = requests.register(id).thenApply(js -> {
      System.out.println("📥 [ClientApi] Raw JSON response:");
      System.out.println(js.toPrettyString());

      try {
        Topology topology = tcp.codec().mapper().convertValue(js, Topology.class);
        System.out.println("   ✅ Parsed Topology object");
        System.out.println("   - Type: " + topology.type);
        System.out.println("   - Nodes field null? " + (topology.nodes == null));

        if (topology.nodes != null) {
          System.out.println("   - Nodes count: " + topology.nodes.size());
          for (int i = 0; i < topology.nodes.size(); i++) {
            var n = topology.nodes.get(i);
            System.out.println("   - Node[" + i + "]: " + n.name + " (id=" + n.id + ")");
          }
        } else {
          System.err.println("   ⚠️ WARNING: nodes field is NULL after parsing!");
        }

        return topology;
      } catch (Exception e) {
        System.err.println("   ❌ Failed to parse topology: " + e.getMessage());
        e.printStackTrace();
        throw e;
      }
    });

    var msg = new SimpleIdMessage(MessageTypes.GET_TOPOLOGY, id);
    send(msg);

    return fut.thenApply(topology -> {
      System.out.println("🔧 [ClientApi] Processing topology in state...");

      if (topology.nodes != null){
        System.out.println("   Updating state with " + topology.nodes.size() + " nodes");
        for (var n : topology.nodes){
          System.out.println("   - Patching node: " + n.id + " (" + n.name + ")");
          state.patchNode(n.id, n.name, n.location, n.ip, n.sensors, n.actuators);
        }
      } else {
        System.err.println("   ⚠️ Skipping state update - nodes is null");
      }

      return topology;
    });
  }

  // ---------- Subscriptions ----------
  public CompletableFuture<Void> subscribe(Collection<String> nodes, Collection<String> events){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    Subscribe s = new Subscribe();
    s.id = id; s.nodes = List.copyOf(nodes); s.events = List.copyOf(events);
    send(s);
    return fut;
  }

  public CompletableFuture<Void> unsubscribe(Collection<String> nodes, Collection<String> events){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    Unsubscribe s = new Unsubscribe();
    s.id = id; s.nodes = List.copyOf(nodes); s.events = List.copyOf(events);
    send(s);
    return fut;
  }

  // ---------- Node management ----------
  public CompletableFuture<String> createNode(Topology.Node node){
    System.out.println("📤 [ClientApi] createNode called");
    System.out.println("   Name: " + node.name);
    System.out.println("   Location: " + node.location);
    System.out.println("   IP: " + node.ip);
    System.out.println("   Sensors: " + node.sensors);
    System.out.println("   Actuators: " + node.actuators);

    String id = requests.newId();
    System.out.println("   Request ID: " + id);

    var fut = requests.register(id).thenApply(js -> {
      System.out.println("📥 [ClientApi] Server response received");
      System.out.println("   Response: " + js.toPrettyString());
      return js.has("nodeId") ? js.get("nodeId").asText() : null;
    });

    CreateNode msg = new CreateNode();
    msg.id = id;
    msg.node = node;

    try {
      String jsonMsg = tcp.codec().toJsonLine(msg);
      System.out.println("📡 [ClientApi] Sending JSON: " + jsonMsg);
    } catch (Exception e) {
      System.err.println("❌ [ClientApi] Failed to serialize: " + e.getMessage());
    }

    send(msg);
    return fut;
  }

  public CompletableFuture<Void> updateNode(String nodeId, Map<String,Object> patch){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    UpdateNode m = new UpdateNode();
    m.id = id; m.nodeId = nodeId; m.patch = patch;
    send(m);
    return fut;
  }

  public CompletableFuture<Void> deleteNode(String nodeId){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    DeleteNode m = new DeleteNode();
    m.id = id; m.nodeId = nodeId;
    send(m);
    return fut;
  }

  public CompletableFuture<Void> addComponent(String nodeId, String kind, String name){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    AddComponent m = new AddComponent();
    m.id = id; m.nodeId = nodeId; m.component = Map.of("kind", kind, "name", name);
    send(m);
    return fut;
  }

  public CompletableFuture<Void> removeComponent(String nodeId, String kind, String name){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    RemoveComponent m = new RemoveComponent();
    m.id = id; m.nodeId = nodeId; m.component = Map.of("kind", kind, "name", name);
    send(m);
    return fut;
  }

  public CompletableFuture<Void> setSampling(String nodeId, int intervalMs){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    SetSampling m = new SetSampling();
    m.id = id; m.nodeId = nodeId; m.intervalMs = intervalMs;
    send(m);
    return fut;
  }

  // ---------- Commands ----------
  public CompletableFuture<Void> sendCommand(String nodeId, String target, String action, Map<String,Object> params){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    Command c = new Command();
    c.id = id; c.nodeId = nodeId; c.target = target; c.action = action; c.params = params;
    send(c);

    return fut;
  }

  // ---------- Pull ----------
  public CompletableFuture<LastValues> getLastValues(String nodeId){
    String id = requests.newId();
    var fut = requests.register(id).thenApply(js -> tcp.codec().mapper().convertValue(js, LastValues.class));
    GetLastValues m = new GetLastValues();
    m.id = id; m.nodeId = nodeId;
    send(m);
    return fut;
  }

  public CompletableFuture<JsonNode> ping(){
    String id = requests.newId();
    CompletableFuture<JsonNode> fut = requests.register(id);
    Ping p = new Ping(); p.id = id;
    send(p);
    return fut;
  }


  // ---------- Incoming processing ----------
  private void handleLine(String line){
    try {
      var mapper = tcp.codec().mapper();
      JsonNode root = mapper.readTree(line);
      String type = root.path("type").asText("");
      String id = root.has("id") ? root.get("id").asText(null) : null;

      switch (type) {
        case MessageTypes.WELCOME -> {
          // complete "hello" future if present
          requests.complete(id, root);
        }
        case MessageTypes.ACK, MessageTypes.ERROR, MessageTypes.LAST_VALUES, MessageTypes.PONG -> {
          // complete pending request future (ACK/ERROR/LastValues/PONG have the id)
          requests.complete(id, root);
        }
        case MessageTypes.TOPOLOGY -> {
          Topology topo = mapper.convertValue(root, Topology.class);
          if (topo.nodes != null){
            for (var n : topo.nodes){
              state.patchNode(n.id, n.name, n.location, n.ip, n.sensors, n.actuators);
            }
          }
        }
        case MessageTypes.SENSOR_UPDATE -> {
          SensorUpdate su = mapper.convertValue(root, SensorUpdate.class);
          state.updateSensors(su.nodeId, su.data);
        }
        case MessageTypes.NODE_CHANGE -> {
          NodeChange nc = mapper.convertValue(root, NodeChange.class);
          if ("added".equalsIgnoreCase(nc.op) || "updated".equalsIgnoreCase(nc.op)) {
            var n = nc.node;
            state.patchNode(n.id, n.name, n.location, n.ip, n.sensors, n.actuators);
          } else if ("removed".equalsIgnoreCase(nc.op)) {
            state.removeNode(nc.nodeId);
          }
        }
        default -> System.out.println("Unknown message: " + line);
      }
    } catch (Exception e) { e.printStackTrace(); }
  }


  private void send(Object dto){
    try {
      String line = tcp.codec().toJsonLine(dto);
        tcp.sendLine(line);
    } catch (Exception e) { e.printStackTrace(); }
  }

  @Override public void close() throws IOException {
    tcp.close();
  }

  // --- tiny helper DTO for get_topology ---
  static class SimpleIdMessage {
    public String type; public String id;
    SimpleIdMessage(String type, String id){ this.type=type; this.id=id; }
  }
}


