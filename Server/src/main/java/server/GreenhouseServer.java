package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Multi-client server with per-node ticking sensor engine and subscriptions.
 */
public class GreenhouseServer {
  private final int port;
  private final ExecutorService clientPool = Executors.newCachedThreadPool();
  private final NodeManager nodeManager = new NodeManager();
  private final ClientRegistry registry = new ClientRegistry();
  private final SensorEngine engine = new SensorEngine(nodeManager, registry::broadcastSensorUpdate);

  public GreenhouseServer(int port) { this.port = port; }

  public void start() throws IOException {
    // Schedule existing nodes
    nodeManager.getAllNodes().forEach(n -> engine.onNodeAdded(n.id));

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("🌿 Server listening on " + port);
      while (true) {
        Socket client = serverSocket.accept();
        clientPool.submit(new ClientHandler(client, nodeManager, registry, engine));
      }
    }
  }
}


