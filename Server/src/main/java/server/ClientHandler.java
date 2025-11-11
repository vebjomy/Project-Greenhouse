package server;


import net.MessageCodec;
import dto.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * Single client connection: subscriptions + requests + acks.
 */
public class ClientHandler implements Runnable {
  private final Socket socket;
  private final NodeManager nodeManager;
  private final ClientRegistry registry;
  private final SensorEngine engine;
  private final MessageCodec codec = new MessageCodec();

  private PrintWriter out;
  private ClientRegistry.Session session;

  public ClientHandler(Socket socket, NodeManager nodeManager, ClientRegistry registry, SensorEngine engine) {
    this.socket = socket;
    this.nodeManager = nodeManager;
    this.registry = registry;
    this.engine = engine;
  }

  @Override
  public void run() {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
         PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)) {
      this.out = writer;

      session = registry.addSession(json -> { out.write(json); out.flush(); });

      Welcome welcome = new Welcome();
      welcome.server = "GreenhouseServer";
      welcome.version = "1.0";
      send(welcome);

      String line;
      while ((line = in.readLine()) != null) process(line);

    } catch (Exception e) {
      System.err.println("Client error: " + e.getMessage());
    } finally {
      registry.removeSession(session);
      try { socket.close(); } catch (IOException ignored) {}
    }
  }

  private void process(String line) {
    try {
      JsonNode root = codec.mapper().readTree(line);
      String type = root.path("type").asText("");
      switch (type) {
        case "hello" -> handleHello(root);
        case "get_topology" -> handleGetTopology(root);
        case "create_node" -> handleCreateNode(root);
        case "update_node" -> handleUpdateNode(root);
        case "delete_node" -> handleDeleteNode(root);
        case "add_component" -> handleAddComponent(root);
        case "remove_component" -> handleRemoveComponent(root);
        case "set_sampling" -> handleSetSampling(root);
        case "subscribe" -> handleSubscribe(root);
        case "unsubscribe" -> handleUnsubscribe(root);
        case "command" -> handleCommand(root);
        case "ping" -> handlePing(root);
        default -> System.out.println("Unknown type: " + type);
      }
    } catch (Exception e) { e.printStackTrace(); }
  }

  private void handleHello(JsonNode msg) { session.clientId = msg.path("clientId").asText(null); ack(msg, "ok"); }


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
        System.out.println("     • " + n.id + ": " + n.name +
                " (" + n.location + ") - " + n.ip);
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

  // Handle create_node request and send ack with new node ID
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
  }

  private void handleUpdateNode(JsonNode msg) throws Exception {
    UpdateNode m = codec.fromJson(msg.toString(), UpdateNode.class);
    nodeManager.updateNode(m.nodeId, m.patch);
    ack(msg, "ok");
    // TODO: broadcast node_change updated
  }

  private void handleDeleteNode(JsonNode msg) throws Exception {
    DeleteNode m = codec.fromJson(msg.toString(), DeleteNode.class);
    nodeManager.deleteNode(m.nodeId);
    engine.onNodeRemoved(m.nodeId);
    ack(msg, "ok");
    // TODO: broadcast node_change removed
  }

  private void handleAddComponent(JsonNode msg) throws Exception {
    AddComponent m = codec.fromJson(msg.toString(), AddComponent.class);
    nodeManager.addComponent(m.nodeId, m.component);
    ack(msg, "ok");
  }

  private void handleRemoveComponent(JsonNode msg) throws Exception {
    RemoveComponent m = codec.fromJson(msg.toString(), RemoveComponent.class);
    nodeManager.removeComponent(m.nodeId, m.component);
    ack(msg, "ok");
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

  private void handlePing(JsonNode msg) { Pong p = new Pong(); p.id = msg.path("id").asText(null); send(p); }

  private void ack(JsonNode msg, String status) {
    try { Ack a = new Ack(); a.id = msg.path("id").asText(null); a.status = status; send(a); }
    catch (Exception e) { e.printStackTrace(); }
  }

  private void send(Object obj) {
    try { out.write(codec.toJsonLine(obj)); out.flush(); }
    catch (Exception e) { e.printStackTrace(); }
  }
}

