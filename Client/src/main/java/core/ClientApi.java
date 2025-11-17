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

  /**
   * Constructs a new ClientApi instance, initializes the TCP client,
   * sets up line and error handlers for incoming server messages.
   */
  public ClientApi(){
    tcp.setOnLine(this::handleLine);
    tcp.setOnError(err -> System.err.println("TCP error: " + err));
  }

  /**
   * Connects to the server at the specified host and port, sends a hello message,
   * and returns a future that completes when the server responds.
   *
   * @param host the server hostname
   * @param port the server port
   * @return a CompletableFuture that completes when connected
   */
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

  /**
   * Registers a new user by sending a registration request to the server.
   *
   * @param req the registration request DTO
   * @return a CompletableFuture with the server's registration response
   */
  public CompletableFuture<RegisterResponse> sendRegisterMessage(RegisterRequest req) {
    String id = req.getId();
    var fut = requests.register(id).thenApply(js ->
            tcp.codec().mapper().convertValue(js, RegisterResponse.class)
    );
    send(req);
    return fut;
  }

  // ---------- Subscriptions ----------

  /**
   * Subscribes to updates for the specified nodes and events.
   *
   * @param nodes  the node IDs to subscribe to
   * @param events the event types to subscribe to
   * @return a CompletableFuture that completes when the subscription is acknowledged
   */
  public CompletableFuture<Void> subscribe(Collection<String> nodes, Collection<String> events){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    Subscribe s = new Subscribe();
    s.id = id; s.nodes = List.copyOf(nodes); s.events = List.copyOf(events);
    send(s);
    return fut;
  }

  /**
   * Unsubscribes from updates for the specified nodes and events.
   *
   * @param nodes  the node IDs to unsubscribe from
   * @param events the event types to unsubscribe from
   * @return a CompletableFuture that completes when the unsubscription is acknowledged
   */
  public CompletableFuture<Void> unsubscribe(Collection<String> nodes, Collection<String> events){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    Unsubscribe s = new Unsubscribe();
    s.id = id; s.nodes = List.copyOf(nodes); s.events = List.copyOf(events);
    send(s);
    return fut;
  }

  // ---------- Node management ----------

  /**
   * Creates a new node in the topology by sending a request to the server.
   *
   * @param node the node DTO to create
   * @return a CompletableFuture with the new node's ID
   */
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

  /**
   * Updates an existing node with the specified patch data.
   *
   * @param nodeId the ID of the node to update
   * @param patch  the patch data as a map
   * @return a CompletableFuture that completes when the update is acknowledged
   */
  public CompletableFuture<Void> updateNode(String nodeId, Map<String,Object> patch){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    UpdateNode m = new UpdateNode();
    m.id = id; m.nodeId = nodeId; m.patch = patch;
    send(m);
    return fut;
  }

  /**
   * Deletes a node from the topology.
   *
   * @param nodeId the ID of the node to delete
   * @return a CompletableFuture that completes when the deletion is acknowledged
   */
  public CompletableFuture<Void> deleteNode(String nodeId){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    DeleteNode m = new DeleteNode();
    m.id = id; m.nodeId = nodeId;
    send(m);
    return fut;
  }

  /**
   * Adds a component to a node.
   *
   * @param nodeId the node ID
   * @param kind   the component kind
   * @param name   the component name
   * @return a CompletableFuture that completes when the addition is acknowledged
   */
  public CompletableFuture<Void> addComponent(String nodeId, String kind, String name){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    AddComponent m = new AddComponent();
    m.id = id; m.nodeId = nodeId; m.component = Map.of("kind", kind, "name", name);
    send(m);
    return fut;
  }

  /**
   * Removes a component from a node.
   *
   * @param nodeId the node ID
   * @param kind   the component kind
   * @param name   the component name
   * @return a CompletableFuture that completes when the removal is acknowledged
   */
  public CompletableFuture<Void> removeComponent(String nodeId, String kind, String name){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    RemoveComponent m = new RemoveComponent();
    m.id = id; m.nodeId = nodeId; m.component = Map.of("kind", kind, "name", name);
    send(m);
    return fut;
  }

  /**
   * Sets the sampling interval for a node.
   *
   * @param nodeId     the node ID
   * @param intervalMs the sampling interval in milliseconds
   * @return a CompletableFuture that completes when the interval is set
   */
  public CompletableFuture<Void> setSampling(String nodeId, int intervalMs){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    SetSampling m = new SetSampling();
    m.id = id; m.nodeId = nodeId; m.intervalMs = intervalMs;
    send(m);
    return fut;
  }

  // ---------- Commands ----------

  /**
   * Sends a command to a node's target actuator or sensor.
   *
   * @param nodeId the node ID
   * @param target the target actuator or sensor
   * @param action the action to perform
   * @param params the command parameters
   * @return a CompletableFuture that completes when the command is acknowledged
   */
  public CompletableFuture<Void> sendCommand(String nodeId, String target, String action, Map<String,Object> params){
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);
    Command c = new Command();
    c.id = id; c.nodeId = nodeId; c.target = target; c.action = action; c.params = params;
    send(c);

    return fut;
  }

  // ---------- Pull ----------

  /**
   * Requests the last sensor values for a node.
   *
   * @param nodeId the node ID
   * @return a CompletableFuture with the last values DTO
   */
  public CompletableFuture<LastValues> getLastValues(String nodeId){
    String id = requests.newId();
    var fut = requests.register(id).thenApply(js -> tcp.codec().mapper().convertValue(js, LastValues.class));
    GetLastValues m = new GetLastValues();
    m.id = id; m.nodeId = nodeId;
    send(m);
    return fut;
  }

  /**
   * Sends a ping message to the server.
   *
   * @return a CompletableFuture with the server's response as a JsonNode
   */
  public CompletableFuture<JsonNode> ping(){
    String id = requests.newId();
    CompletableFuture<JsonNode> fut = requests.register(id);
    Ping p = new Ping(); p.id = id;
    send(p);
    return fut;
  }

  /**
   * Sends an authentication message to the server.
   *
   * @param auth the authentication DTO
   * @return a CompletableFuture with the authentication response
   */
  public CompletableFuture<AuthResponse> sendAuthMessage(Auth auth) {
    String id = auth.getId();
    var fut = requests.register(id).thenApply(js ->
            tcp.codec().mapper().convertValue(js, AuthResponse.class)
    );
    send(auth);
    return fut;
  }

  /**
   * Requests the list of users from the server.
   *
   * @return a CompletableFuture with the list of user data
   */
  public CompletableFuture<List<UsersListResponse.UserData>> getUsers() {
    String id = requests.newId();
    var fut = requests.register(id).thenApply(js -> {
      UsersListResponse response = tcp.codec().mapper().convertValue(js, UsersListResponse.class);
      return response.users;
    });

    var msg = new SimpleIdMessage("get_users", id);
    send(msg);
    return fut;
  }

  /**
   * Updates a user's information.
   *
   * @param userId   the user ID
   * @param username the new username
   * @param role     the new role
   * @return a CompletableFuture that completes when the update is acknowledged
   */
  public CompletableFuture<Void> updateUser(int userId, String username, String role) {
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);

    UpdateUserRequest req = new UpdateUserRequest();
    req.id = id;
    req.userId = userId;
    req.username = username;
    req.role = role;
    send(req);
    return fut;
  }

  /**
   * Deletes a user from the system.
   *
   * @param userId the user ID
   * @return a CompletableFuture that completes when the deletion is acknowledged
   */
  public CompletableFuture<Void> deleteUser(int userId) {
    String id = requests.newId();
    CompletableFuture<Void> fut = requests.register(id).thenApply(js -> null);

    DeleteUserRequest req = new DeleteUserRequest();
    req.id = id;
    req.userId = userId;
    send(req);
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
        case MessageTypes.REGISTER_RESPONSE -> {
          requests.complete(id, root);
        }
        case MessageTypes.AUTH_RESPONSE -> {
          requests.complete(id, root);
        }
        case MessageTypes.USERS_LIST -> {
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

  /**
   * Helper DTO for sending simple ID-based messages.
   */
  static class SimpleIdMessage {
    public String type; public String id;
    /**
     * Constructs a SimpleIdMessage with the specified type and ID.
     *
     * @param type the message type
     * @param id   the message ID
     */
    SimpleIdMessage(String type, String id){ this.type=type; this.id=id; }
  }
}


