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

  private static final String SERVER_NAME = "GreenhouseServer";
  private static final String SERVER_VERSION = "1.0";

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

  private void handleHello(JsonNode msg) { 
    session.clientId = msg.path("clientId").asText(null); 
    Welcome welcome = new Welcome();
    welcome.id = msg.path("id").asText(null);
    welcome.server = SERVER_NAME;
    welcome.version = SERVER_VERSION;
    send(welcome);
  }

  private void handleGetTopology(JsonNode msg) {
    Topology t = new Topology();
    t.id = msg.path("id").asText(null);
    t.nodes = nodeManager.getAllNodes();
    send(t);
  }

  private void handleCreateNode(JsonNode msg) throws Exception {
    CreateNode cn = codec.fromJson(msg.toString(), CreateNode.class);
    String id = nodeManager.addNode(cn.node);
    engine.onNodeAdded(id);
    Ack a = new Ack();
    a.id = cn.id; a.status = "ok"; a.nodeId = id;
    send(a);
    // TODO: broadcast node_change added if needed
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

