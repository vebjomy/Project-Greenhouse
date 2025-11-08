package core;

import App.MainApp;
import dto.Topology;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Parses user commands from terminal and executes corresponding actions through ClientApi.
 */
public class CommandProcessor {
  private final ClientApi api;
  private final MainApp mainApp;

  public CommandProcessor(ClientApi api, MainApp mainApp) {
    this.api = api;
    this.mainApp = mainApp;
  }

  public CompletableFuture<String> execute(String input) {
    if (input == null || input.isBlank()) {
      return CompletableFuture.completedFuture("Empty command.");
    }

    String[] parts = input.trim().split("\\s+");
    String cmd = parts[0].toLowerCase(Locale.ROOT);

    try {
      return switch (cmd) {
        case "help" -> CompletableFuture.completedFuture(getHelpText());
        case "connect" -> handleConnect(parts);
        case "ping" -> handlePing();
        case "topology", "get_topology" -> handleGetTopology();
        case "create_node" -> handleCreateNode(parts);
        case "status" -> handleStatus();
        case "exit", "quit" -> handleExit();
        default -> CompletableFuture.completedFuture("Unknown command: " + cmd +
            "\nType 'help' for available commands.");
      };
    } catch (Exception e) {
      return CompletableFuture.completedFuture("❌ Command error: " + e.getMessage());
    }
  }

  private CompletableFuture<String> handleConnect(String[] parts) {
    // Use default settings from MainApp
    String host;
    int port;

    // But allow overriding via command
    if (parts.length >= 3) {
      host = parts[1];
      port = Integer.parseInt(parts[2]);
    } else {
      port = mainApp.getServerPort();
      host = mainApp.getServerAddress();
    }

    return api.connect(host, port)
        .thenApply(v -> "✅ Connected to " + host + " and port number is " + port)
        .exceptionally(ex -> "❌ Connection failed: " + ex.getMessage());
  }

  private CompletableFuture<String> handlePing() {
    return api.ping()
        .thenApply(jsonResponse -> {
          if ("pong".equals(jsonResponse.path("type").asText())) {
            return "✅ Server responded to ping. ID: " + jsonResponse.path("id").asText();
          }
          return "❌ Unexpected server response type: " + jsonResponse.toPrettyString();
        })
        .exceptionally(ex -> "❌ Ping failed: " + ex.getMessage());
  }



  private CompletableFuture<String> handleGetTopology() {
    return api.getTopology()
        .thenApply(topology -> {
          if (topology == null) {
            return "❌ Received null topology";
          }
          if (topology.nodes == null) {
            return "🗺️ Topology received: 0 nodes (nodes list is null)";
          }
          return "🗺️ Topology received: " + topology.nodes.size() + " nodes.";
        })
        .exceptionally(ex -> {
          return "❌ Failed to get topology: " + ex.getMessage();
        });
  }



  private CompletableFuture<String> handleCreateNode(String[] parts) {
    if (parts.length < 2) {
      return CompletableFuture.completedFuture("Usage: create_node <name>");
    }

    var node = new Topology.Node();
    node.name = parts[1];
    return api.createNode(node)
        .thenApply(id -> "✅ Node created: " + id)
        .exceptionally(ex -> "❌ Failed to create node: " + ex.getMessage());
  }

  private CompletableFuture<String> handleStatus() {
    return CompletableFuture.completedFuture(
        "📊 Status:\n" +
            "Server: " + mainApp.getServerAddress() + ":" + mainApp.getServerPort() + "\n" +
            "Nodes in cache: " + api.state().allNodes().size()
    );
  }

  private CompletableFuture<String> handleExit() {
    try {
      api.close();
      return CompletableFuture.completedFuture("👋 Goodbye!");
    } catch (Exception e) {
      return CompletableFuture.completedFuture("❌ Error closing connection: " + e.getMessage());
    }
  }

  private String getHelpText() {
    return """
            📋 Available Commands:
            connect [host] [port] - Connect to server (default: %s:%d)
            ping                   - Test server connection
            topology              - Get network topology
            create_node <name>    - Create new node
            status                - Show connection status
            help                  - Show this help
            exit/quit             - Exit application
            """.formatted(mainApp.getServerAddress(), mainApp.getServerPort());
  }
}