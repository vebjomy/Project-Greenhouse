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

  /**
   * Processes an incoming message line from the client.
   *
   * <p>This method parses the JSON message, determines its type, and routes it to
   * the appropriate handler method. It implements comprehensive error handling for:
   * <ul>
   *   <li>JSON parsing errors (malformed JSON)</li>
   *   <li>Unknown message types</li>
   *   <li>Runtime exceptions during message processing</li>
   * </ul>
   *
   * <p>In case of any error, an appropriate {@link dto.ErrorMsg} is sent back to
   * the client with a descriptive error code and message.
   *
   * <p><b>Supported Message Types:</b>
   * {@code hello}, {@code auth}, {@code register}, {@code get_users}, {@code update_user},
   * {@code delete_user}, {@code get_topology}, {@code create_node}, {@code update_node},
   * {@code delete_node}, {@code set_sampling}, {@code subscribe}, {@code unsubscribe},
   * {@code command}, {@code ping}
   *
   * @param line the raw JSON message line received from the client
   */
  private void process(String line) {
    try {
      JsonNode root = codec.mapper().readTree(line);
      String type = root.path("type").asText("");
      String requestId = root.path("id").asText(null);
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
        default -> {
          System.out.println("Unknown type: " + type);
        sendError(requestId, "UNSUPPORTED", "Unknown message type: " + type);
        }
      }
    } catch (com.fasterxml.jackson.core.JsonParseException e) {
      System.err.println("❌ JSON parse error: " + e.getMessage());
      sendError(null, "INVALID_JSON", "Malformed JSON: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("❌ Processing error: " + e.getMessage());
      e.printStackTrace();
      sendError(null, "INTERNAL", "Internal server error: " + e.getMessage());
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
    String requestId = msg.path("id").asText(null);
    try {
    System.out.println("📥 [Server] Received create_node request");
    CreateNode cn = codec.fromJson(msg.toString(), CreateNode.class);

      // Validation
      if (cn.node == null) {
        sendError(requestId, "INVALID_ARG", "Node object is required");
        return;
      }

      if (cn.node.name == null || cn.node.name.trim().isEmpty()) {
        sendError(requestId, "INVALID_ARG", "Node name is required");
        return;
      }


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

    } catch (Exception e) {
      System.err.println("❌ Error creating node: " + e.getMessage());
      e.printStackTrace();
      sendError(requestId, "INTERNAL", "Failed to create node: " + e.getMessage());
    }
  }

  /**
   * Handle update_node request.
   *
   * @param msg The JSON message.
   * @throws Exception If an error occurs.
   */
  private void handleUpdateNode(JsonNode msg) throws Exception {
    String requestId = msg.path("id").asText(null);

    try {
    UpdateNode m = codec.fromJson(msg.toString(), UpdateNode.class);

      // Validation
      if (m.nodeId == null || m.nodeId.trim().isEmpty()) {
        sendError(requestId, "INVALID_ARG", "Node ID is required");
        return;
      }

      if (nodeManager.getAllNodes().stream().noneMatch(n -> n.id.equals(m.nodeId))) {
        sendError(requestId, "NOT_FOUND", "Node not found: " + m.nodeId);
        return;
      }

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

    } catch (Exception e) {
      System.err.println("❌ Error updating node: " + e.getMessage());
      e.printStackTrace();
      sendError(requestId, "INTERNAL", "Failed to update node: " + e.getMessage());
    }
  }

  /**
   * Processes a delete_node request: deletes the node, notifies the engine, sends an ACK, and
   * broadcasts a node_change "deleted" event with the node id.
   *
   * @param msg JSON request containing the request id and target nodeId
   * @throws Exception if deserialization or downstream operations fail
   */
  private void handleDeleteNode(JsonNode msg) throws Exception {
    String requestId = msg.path("id").asText(null);

    try {
    DeleteNode m = codec.fromJson(msg.toString(), DeleteNode.class);

      // Validation
      if (m.nodeId == null || m.nodeId.trim().isEmpty()) {
        sendError(requestId, "INVALID_ARG", "Node ID is required");
        return;
      }

      if (nodeManager.getAllNodes().stream().noneMatch(n -> n.id.equals(m.nodeId))) {
        sendError(requestId, "NOT_FOUND", "Node not found: " + m.nodeId);
        return;
      }

      nodeManager.deleteNode(m.nodeId);
    engine.onNodeRemoved(m.nodeId);
    ack(msg, "ok");

    // broadcast node_change deleted
    NodeChange nc = new NodeChange();
    nc.op = "deleted";
    nc.node = new Topology.Node();
    nc.node.id = m.nodeId;
    registry.broadcast(nc);

    } catch (Exception e) {
      System.err.println("❌ Error deleting node: " + e.getMessage());
      e.printStackTrace();
      sendError(requestId, "INTERNAL", "Failed to delete node: " + e.getMessage());
    }
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
    String requestId = msg.path("id").asText(null);

    try {
    Command c = codec.fromJson(msg.toString(), Command.class);

      // Validation
      if (c.nodeId == null || c.target == null || c.action == null) {
        sendError(requestId, "INVALID_ARG", "Command requires nodeId, target, and action");
        return;
      }

      // Check if node exists
      if (nodeManager.getAllNodes().stream().noneMatch(n -> n.id.equals(c.nodeId))) {
        sendError(requestId, "NOT_FOUND", "Node not found: " + c.nodeId);
        return;
      }

    nodeManager.executeCommand(c);
    ack(msg, "ok");

    } catch (Exception e) {
      System.err.println("❌ Error executing command: " + e.getMessage());
      e.printStackTrace();
      sendError(requestId, "INTERNAL", "Failed to execute command: " + e.getMessage());
    }
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

  /**
   * Sends an error message to the client with the specified error code and message.
   *
   * <p>This method constructs and sends an {@link dto.ErrorMsg} to inform the client
   * about a processing error, validation failure, or other exceptional condition.
   * The error is logged on the server side for debugging purposes.
   *
   * <p><b>Supported Error Codes:</b>
   * <ul>
   *   <li>{@code INVALID_ARG} - Invalid or missing required parameters</li>
   *   <li>{@code NOT_FOUND} - Requested resource (e.g., node) does not exist</li>
   *   <li>{@code ALREADY_EXISTS} - Resource already exists (e.g., duplicate node)</li>
   *   <li>{@code UNSUPPORTED} - Unsupported operation or message type</li>
   *   <li>{@code FORBIDDEN} - Insufficient permissions for the operation</li>
   *   <li>{@code INTERNAL} - Internal server error during processing</li>
   *   <li>{@code INVALID_JSON} - Malformed JSON in client request</li>
   * </ul>
   *
   * @param requestId the request ID (correlation ID) from the original client message;
   *                  may be {@code null} if the request ID could not be extracted
   * @param code the error code string identifying the error category
   * @param message a human-readable error message describing the issue
   *
   * @see dto.ErrorMsg
   */
  private void sendError(String requestId, String code, String message) {
    try {
      dto.ErrorMsg error = new dto.ErrorMsg();
      error.id = requestId;
      error.code = code;
      error.message = message;
      send(error);
      System.err.println("❌ [Server] Error sent: " + code + " - " + message);
    } catch (Exception e) {
      System.err.println("❌ Failed to send error message: " + e.getMessage());
    }
  }
}
