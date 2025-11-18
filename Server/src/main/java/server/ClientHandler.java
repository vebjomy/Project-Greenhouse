package server;

import com.fasterxml.jackson.databind.JsonNode;
import dto.Ack;
import dto.Auth;
import dto.AuthResponse;
import dto.Command;
import dto.CreateNode;
import dto.DeleteNode;
import dto.DeleteUserRequest;
import dto.GetUsersRequest;
import dto.NodeChange;
import dto.Pong;
import dto.RegisterRequest;
import dto.RegisterResponse;
import dto.SetSampling;
import dto.Subscribe;
import dto.Topology;
import dto.Unsubscribe;
import dto.UpdateNode;
import dto.UpdateUserRequest;
import dto.UsersListResponse;
import dto.Welcome;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import net.MessageCodec;

/**
 * Single client connection: subscriptions + requests + acks.
 */
public class ClientHandler implements Runnable {

  private final Socket socket;
  private final NodeManager nodeManager;
  private final ClientRegistry registry;
  private final SensorEngine engine;
  private final UserService userService;
  private final MessageCodec codec = new MessageCodec();

  private PrintWriter out;
  private ClientRegistry.Session session;

  /**
   * Creates a new {@code ClientHandler} for a single client connection and wires required services.
   * Initializes references for node management, client session registry, sensor engine callbacks,
   * and user management. No I/O is performed here.
   *
   * @param socket      the client socket used for reading and writing messages
   * @param nodeManager manager for node CRUD and operations
   * @param registry    registry that tracks sessions and broadcasts server events
   * @param engine      sensor engine notified on topology changes
   * @param userService user management service for auth and user operations
   */
  public ClientHandler(
      Socket socket,
      NodeManager nodeManager,
      ClientRegistry registry,
      SensorEngine engine,
      UserService userService) {
    this.socket = socket;
    this.nodeManager = nodeManager;
    this.registry = registry;
    this.engine = engine;
    this.userService = userService;
  }

  @Override
  public void run() {
    try (BufferedReader in =
        new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        PrintWriter writer =
            new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)) {
      this.out = writer;

      session =
          registry.addSession(
              json -> {
                out.write(json);
                out.flush();
              });

      Welcome welcome = new Welcome();
      welcome.server = "GreenhouseServer";
      welcome.version = "1.0";
      send(welcome);

      String line;
      while ((line = in.readLine()) != null) {
        process(line);
      }

    } catch (Exception e) {
      System.err.println("Client error: " + e.getMessage());
    } finally {
      registry.removeSession(session);
      try {
        socket.close();
      } catch (IOException ignored) {
        System.out.println(" Failed to close socket");
      }
    }
  }

  private void process(String line) {
    try {
      JsonNode root = codec.mapper().readTree(line);
      String type = root.path("type").asText("");
      switch (type) {
        case "hello" -> handleHello(root);
        case "auth" -> handleAuth(root);
        case "register" -> handleRegister(root);
        case "get_users" -> handleGetUsers(root);
        case "update_user" -> handleUpdateUser(root);
        case "delete_user" -> handleDeleteUser(root);
        case "get_topology" -> handleGetTopology(root);
        case "create_node" -> handleCreateNode(root);
        case "update_node" -> handleUpdateNode(root);
        case "delete_node" -> handleDeleteNode(root);
        case "set_sampling" -> handleSetSampling(root);
        case "subscribe" -> handleSubscribe(root);
        case "unsubscribe" -> handleUnsubscribe(root);
        case "command" -> handleCommand(root);
        case "ping" -> handlePing(root);
        default -> System.out.println("Unknown type: " + type);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handleHello(JsonNode msg) {
    session.clientId = msg.path("clientId").asText(null);
    ack(msg, "ok");
  }

  // Handle get_topology request and send topology response
  private void handleGetTopology(JsonNode msg) {
    System.out.println("📥 [Server] Received get_topology request");
    String requestId = msg.path("id").asText(null);
    System.out.println("   Request ID: " + requestId);

    Topology t = new Topology();
    t.id = requestId;
    t.nodes = nodeManager.getAllNodes();

    System.out.println("   📊 Topology created:");
    System.out.println("   - Request ID: " + t.id);
    System.out.println("   - Nodes count: " + (t.nodes != null ? t.nodes.size() : "NULL"));

    if (t.nodes != null && !t.nodes.isEmpty()) {
      System.out.println("   - Node details:");
      for (var n : t.nodes) {
        System.out.println("     • " + n.id + ": " + n.name + " (" + n.location + ") - " + n.ip);
      }
    } else {
      System.err.println("   ⚠️ WARNING: Node list is empty or null!");
    }

    try {
      String json = codec.toJsonLine(t);
      System.out.println("   📤 Sending topology JSON:");
      System.out.println(json);
    } catch (Exception e) {
      System.err.println("   ❌ Failed to serialize: " + e.getMessage());
    }

    send(t);

    System.out.println("   ✅ Topology sent (no separate ACK)");
  }

  private void handleAuth(JsonNode msg) throws Exception {
    System.out.println("📥 [Server] Received auth request");
    Auth auth = codec.fromJson(msg.toString(), Auth.class);

    System.out.println("   Username: " + auth.getUsername());

    boolean isValid = userService.validateUser(auth.getUsername(), auth.getPassword());

    AuthResponse response = new AuthResponse();
    response.id = auth.getId();
    response.success = isValid;

    if (isValid) {
      response.userId = userService.getUserId(auth.getUsername());
      response.role = userService.getUserRole(auth.getUsername());
      response.message = "Authentication successful";
      System.out.println("   ✅ Authentication successful - Role: " + response.role);
    } else {
      response.message = "Invalid username or password";
      System.out.println("   ❌ Authentication failed");
    }

    send(response);
  }

  private void handleRegister(JsonNode msg) throws Exception {
    System.out.println("📥 [Server] Received register request");
    RegisterRequest req = codec.fromJson(msg.toString(), RegisterRequest.class);

    System.out.println("   Username: " + req.getUsername());
    System.out.println("   Role: " + req.getRole());

    RegisterResponse response = new RegisterResponse();
    response.id = req.getId();

    // Check if user already exists
    if (userService.getUserId(req.getUsername()) != -1) {
      response.success = false;
      response.message = "Username already exists";
      System.out.println("   ❌ Registration failed - User exists");
    } else {
      // Register new user
      int userId = userService.registerUser(req.getUsername(), req.getPassword(), req.getRole());
      response.success = true;
      response.userId = userId;
      response.message = "Registration successful";
      System.out.println("   ✅ Registration successful - UserID: " + userId);
    }

    send(response);
  }

  private void handleGetUsers(JsonNode msg) throws Exception {
    System.out.println("📥 [Server] Received get_users request");
    GetUsersRequest req = codec.fromJson(msg.toString(), GetUsersRequest.class);

    UsersListResponse response = new UsersListResponse();
    response.id = req.id;
    response.success = true;
    response.users = userService.getAllUsers();

    System.out.println("   ✅ Returning " + response.users.size() + " users");
    send(response);
  }

  private void handleUpdateUser(JsonNode msg) throws Exception {
    System.out.println("📥 [Server] Received update_user request");
    UpdateUserRequest req = codec.fromJson(msg.toString(), UpdateUserRequest.class);

    boolean success = userService.updateUser(req.userId, req.username, req.role);

    Ack ack = new Ack();
    ack.id = req.id;
    ack.status = success ? "ok" : "error";

    System.out.println("   " + (success ? "✅" : "❌") + " User update: " + req.username);
    send(ack);
  }

  private void handleDeleteUser(JsonNode msg) throws Exception {
    System.out.println("📥 [Server] Received delete_user request");
    DeleteUserRequest req = codec.fromJson(msg.toString(), DeleteUserRequest.class);

    boolean success = userService.deleteUser(req.userId);

    Ack ack = new Ack();
    ack.id = req.id;
    ack.status = success ? "ok" : "error";

    System.out.println("   " + (success ? "✅" : "❌") + " User deleted: ID " + req.userId);
    send(ack);
  }

  /**
   * Handle create_node request.
   *
   * @param msg The JSON message.
   * @throws Exception If an error occurs.
   */
  private void handleCreateNode(JsonNode msg) throws Exception {
    System.out.println("📥 [Server] Received create_node request");
    System.out.println("   Raw JSON: " + msg.toPrettyString());

    CreateNode cn = codec.fromJson(msg.toString(), CreateNode.class);

    System.out.println("   Parsed node:");
    System.out.println("   - Name: " + cn.node.name);
    System.out.println("   - Location: " + cn.node.location);
    System.out.println("   - IP: " + cn.node.ip);
    System.out.println("   - Sensors: " + cn.node.sensors);
    System.out.println("   - Actuators: " + cn.node.actuators);

    String id = nodeManager.addNode(cn.node);
    System.out.println("   ✅ Node added with ID: " + id);

    engine.onNodeAdded(id);

    Ack a = new Ack();
    a.id = cn.id;
    a.status = "ok";
    a.nodeId = id;

    System.out.println("   📤 Sending ACK with nodeId: " + id);
    send(a);

    // Verify node was added
    System.out.println("   📊 Total nodes now: " + nodeManager.getAllNodes().size());

    // broadcast node_change added
    NodeChange nc = new NodeChange();
    nc.op = "added";
    nc.node = cn.node;
    nc.node.id = id;
    registry.broadcast(nc);
  }

  /**
   * Handle update_node request.
   *
   * @param msg The JSON message.
   * @throws Exception If an error occurs.
   */
  private void handleUpdateNode(JsonNode msg) throws Exception {
    UpdateNode m = codec.fromJson(msg.toString(), UpdateNode.class);
    nodeManager.updateNode(m.nodeId, m.patch);
    ack(msg, "ok");
    // broadcast node_change updated
    NodeChange nc = new NodeChange();
    nc.op = "updated";
    nc.node =
        nodeManager.getAllNodes().stream()
            .filter(n -> n.id.equals(m.nodeId))
            .findFirst()
            .orElse(null);
    registry.broadcast(nc);
  }

  /**
   * Processes a delete_node request: deletes the node, notifies the engine, sends an ACK, and
   * broadcasts a node_change "deleted" event with the node id.
   *
   * @param msg JSON request containing the request id and target nodeId
   * @throws Exception if deserialization or downstream operations fail
   */
  private void handleDeleteNode(JsonNode msg) throws Exception {
    DeleteNode m = codec.fromJson(msg.toString(), DeleteNode.class);
    nodeManager.deleteNode(m.nodeId);
    engine.onNodeRemoved(m.nodeId);
    ack(msg, "ok");
    // broadcast node_change deleted
    NodeChange nc = new NodeChange();
    nc.op = "deleted";
    nc.node = new Topology.Node();
    nc.node.id = m.nodeId;
    registry.broadcast(nc);
  }


  private void handleSetSampling(JsonNode msg) throws Exception {
    SetSampling m = codec.fromJson(msg.toString(), SetSampling.class);
    nodeManager.setSampling(m.nodeId, m.intervalMs);
    engine.rescheduleNode(m.nodeId);
    ack(msg, "ok");
  }

  private void handleSubscribe(JsonNode msg) throws Exception {
    Subscribe s = codec.fromJson(msg.toString(), Subscribe.class);
    session.subscribe(s.events, s.nodes);
    ack(msg, "ok");
  }

  private void handleUnsubscribe(JsonNode msg) throws Exception {
    Unsubscribe s = codec.fromJson(msg.toString(), Unsubscribe.class);
    session.unsubscribe(s.events, s.nodes);
    ack(msg, "ok");
  }

  private void handleCommand(JsonNode msg) throws Exception {
    Command c = codec.fromJson(msg.toString(), Command.class);
    nodeManager.executeCommand(c);
    ack(msg, "ok");
  }

  private void handlePing(JsonNode msg) {
    Pong p = new Pong();
    p.id = msg.path("id").asText(null);
    send(p);
  }

  private void ack(JsonNode msg, String status) {
    try {
      Ack a = new Ack();
      a.id = msg.path("id").asText(null);
      a.status = status;
      send(a);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void send(Object obj) {
    try {
      out.write(codec.toJsonLine(obj));
      out.flush();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
