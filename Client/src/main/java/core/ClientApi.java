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

  // Optional mock server for offline development
  private net.MockServer mock;
  private boolean usingMock;

  public ClientApi(){
    tcp.setOnLine(this::handleLine);
    tcp.setOnError(err -> System.err.println("TCP error: " + err));
  }

  // ---------- Connection ----------
  public void useMock(){
    usingMock = true;
    mock = new net.MockServer();
    mock.attachClient(this::handleLine);
  }

  public CompletableFuture<Void> connect(String host, int port) {
    try {
      usingMock = false;
      tcp.connect(host, port);
      String id = requests.newId();
      CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
      Hello h = new Hello();
      h.id = id;
      h.clientId = "ui-" + UUID.randomUUID();
      h.user = "local";
      h.capabilities = List.of("topology","commands","subscribe");
      send(h);
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
    String id = requests.newId();
    var fut = requests.register(id).thenApply(js -> tcp.codec().mapper().convertValue(js, Topology.class));
    var msg = new SimpleIdMessage(MessageTypes.GET_TOPOLOGY, id);
    send(msg);
    return fut.thenApply(topology -> {
      if (topology.nodes != null){
        for (var n : topology.nodes){
          state.patchNode(n.id, n.name, n.location, n.ip, n.sensors, n.actuators);
        }
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
    String id = requests.newId();
    var fut = requests.register(id).thenApply(js -> js.has("nodeId") ? js.get("nodeId").asText() : null);
    CreateNode msg = new CreateNode();
    msg.id = id; msg.node = node;
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
    if (usingMock && mock != null) mock.onClientCommand(c);
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

  // ---------- Heartbeat ----------
  public CompletableFuture<Void> ping(){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
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
        case MessageTypes.ACK, MessageTypes.ERROR, MessageTypes.LAST_VALUES -> {
          // complete pending request future (ACK/ERROR/LastValues have the id)
          requests.complete(id, root);
        }
        case MessageTypes.TOPOLOGY -> {
          Topology topo = mapper.convertValue(root, Topology.class);
          if (topo.nodes != null){
            for (var n : topo.nodes){
              state.patchNode(n.id, n.name, n.location, n.ip, n.sensors, n.actuators);
            }
          }
          // Complete the request future
          requests.complete(id, root);
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
      if (usingMock) System.out.print("> "+line); else tcp.sendLine(line);
    } catch (Exception e) { e.printStackTrace(); }
  }

  @Override public void close() throws IOException {
    if (mock != null) mock.shutdown();
    tcp.close();
  }

  // --- tiny helper DTO for get_topology ---
  static class SimpleIdMessage {
    public String type; public String id;
    SimpleIdMessage(String type, String id){ this.type=type; this.id=id; }
  }
}


