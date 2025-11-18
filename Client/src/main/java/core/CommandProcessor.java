package core;

import App.MainApp;
import dto.Topology;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Parses user commands from terminal and executes corresponding actions through ClientApi.
 */
public class CommandProcessor {

  private final ClientApi api;
  private final MainApp mainApp;

  /**
   * Constructor for CommandProcessor.
   *
   * @param api     Client API instance.
   * @param mainApp Main application instance.
   */
  public CommandProcessor(ClientApi api, MainApp mainApp) {
    this.api = api;
    this.mainApp = mainApp;
  }

  /**
   * Execute a command based on user input.
   *
   * @param input User input command string.
   * @return Future with result message.
   */
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
        default -> CompletableFuture.completedFuture(
            "Unknown command: "
                + cmd
                + "\nType 'help' for available commands.");
      };
    } catch (Exception e) {
      return CompletableFuture.completedFuture("❌ Command error: " + e.getMessage());
    }
  }

  /**
   * Handle 'connect' command to connect to server.
   *
   * @param parts Command parts.
   * @return Future with result message.
   */
  private CompletableFuture<String> handleConnect(String[] parts) {
    String host;
    int port;

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

  /**
   * Handle 'topology' command to get network topology.
   *
   */
  private CompletableFuture<String> handleGetTopology() {
    System.out.println("🔧 [CommandProcessor] Getting topology...");

    return api.getTopology()
        .thenApply(topology -> {
          System.out.println("🔧 [CommandProcessor] Topology received");
          System.out.println("   Topology object: " + topology);
          System.out.println("   Nodes field: " + topology.nodes);

          if (topology == null) {
            return "❌ Received null topology";
          }

          if (topology.nodes == null) {
            return "🗺️ Topology received: 0 nodes (nodes list is null)";
          }

          if (topology.nodes.isEmpty()) {
            return "🗺️ Topology received: 0 nodes (nodes list is empty)";
          }

          StringBuilder result = new StringBuilder();
          result.append("🗺️ Topology received: ").append(topology.nodes.size())
              .append(" node(s)\n\n");

          for (Topology.Node node : topology.nodes) {
            result.append("📍 ").append(node.name).append("\n");
            result.append("   ID: ").append(node.id).append("\n");
            result.append("   IP: ").append(node.ip).append("\n");
            result.append("   Location: ").append(node.location).append("\n");
            result.append("   Sensors: ").append(node.sensors != null ? node.sensors.size() : 0)
                .append("\n");
            result.append("   Actuators: ")
                .append(node.actuators != null ? node.actuators.size() : 0).append("\n");
            result.append("\n");
          }

          return result.toString();
        })
        .exceptionally(ex -> {
          System.err.println("🔧 [CommandProcessor] Topology error: " + ex.getMessage());
          ex.printStackTrace();
          return "❌ Failed to get topology: " + ex.getMessage();
        });
  }

  private CompletableFuture<String> handleCreateNode(String[] parts) {
    if (parts.length < 2) {
      return CompletableFuture.completedFuture("❌ Usage: create_node <name>");
    }

    var node = new Topology.Node();
    node.name = parts[1];
    return api.createNode(node)
        .thenApply(id -> "✅ Node created: " + id)
        .exceptionally(ex -> "❌ Failed to create node: " + ex.getMessage());
  }

  private CompletableFuture<String> handleStatus() {
    int nodeCount = api.state().allNodes().size();

    return CompletableFuture.completedFuture(
        "📊 Status:\n"
            + "Server: "
            + mainApp.getServerAddress()
            + ":" + mainApp.getServerPort()
            + "\n"
            + "Connected: "
            + (mainApp.isConnected() ? "Yes ✅" : "No ❌")
            + "\n"
            + "Nodes in cache: "
            + nodeCount
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
        
        connect [host] [port]  - Connect to server (default: %s:%d)
        ping                   - Test server connection
        topology               - Get network topology (list all nodes)
        create_node <name>     - Create new node
        status                 - Show connection status and cached nodes
        help                   - Show this help
        exit/quit              - Exit application
        
        Examples:
        > topology
        > create_node "Greenhouse-A"
        > status
        """.formatted(mainApp.getServerAddress(), mainApp.getServerPort());
  }
}