package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Multi-client server with per-node ticking sensor engine and subscriptions. */
public class GreenhouseServer {
  private final int port;
  private final ExecutorService clientPool = Executors.newCachedThreadPool();
  private final NodeManager nodeManager = new NodeManager();
  private final ClientRegistry registry = new ClientRegistry();
  private final SensorEngine engine =
      new SensorEngine(nodeManager, registry::broadcastSensorUpdate);
  private final UserService userService = new UserService();

  /**
   * Creates a new GreenhouseServer listening on the specified port.
   *
   * @param port the port number to listen on
   */
  public GreenhouseServer(int port) {
    this.port = port;
  }

  /** Starts the server to accept client connections and handle sensor updates. */
  public void start() throws IOException {
    // Schedule existing nodes
    nodeManager.getAllNodes().forEach(n -> engine.onNodeAdded(n.id));

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("🌿 Server listening on " + port);
      while (true) {
        Socket client = serverSocket.accept();
        clientPool.submit(new ClientHandler(client, nodeManager, registry, engine, userService));
      }
    }
  }
}
