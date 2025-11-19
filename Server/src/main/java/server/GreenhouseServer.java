package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Multi-client server with per-node ticking sensor engine and subscriptions.
 */
public class GreenhouseServer implements AutoCloseable {

  private final int port;
  private final ExecutorService clientPool = Executors.newCachedThreadPool(
      r -> new Thread(r, "client-handler")); // Added ThreadFactory for clarity
  private final NodeManager nodeManager = new NodeManager();
  private final ClientRegistry registry = new ClientRegistry();
  private final SensorEngine engine =
      new SensorEngine(nodeManager, registry::broadcastSensorUpdate);
  private final UserService userService = new UserService();

  // New: Store the ServerSocket reference so we can close it from the outside
  private volatile ServerSocket serverSocket;

  /**
   * Creates a new GreenhouseServer listening on the specified port.
   *
   * @param port the port number to listen on
   */
  public GreenhouseServer(int port) {
    this.port = port;
  }

  /**
   * Starts the server to accept client connections and handle sensor updates.
   */
  public void start() throws IOException {
    // Schedule existing nodes
    nodeManager.getAllNodes().forEach(n -> engine.onNodeAdded(n.id));

    // Store the ServerSocket reference
    this.serverSocket = new ServerSocket(port);
    System.out.println("🌿 Server listening on " + port);

    // The main thread is now outside the try-with-resources, allowing the hook to close it
    try {
      while (true) {
        Socket client = serverSocket.accept();
        clientPool.submit(new ClientHandler(client, nodeManager, registry, engine, userService));
      }
    } catch (IOException e) {
      if (!serverSocket.isClosed()) {
        // Only report error if the close was unexpected (i.e., not from the shutdown hook)
        System.err.println("Server accept loop interrupted: " + e.getMessage());
      }
    }
  }

  /**
   * Gracefully shuts down the server, closing the server socket, client connections, and the sensor
   * engine.
   */
  @Override
  public void close() {
    System.out.println("Shutting down GreenhouseServer...");

    // 1. Close the listening socket to stop accepting new connections
    if (serverSocket != null && !serverSocket.isClosed()) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        System.err.println("Error closing ServerSocket: " + e.getMessage());
      }
    }

    // 2. Shut down the client handler pool
    clientPool.shutdown();
    try {
      // Wait for all ClientHandlers to finish (max 10 seconds)
      if (!clientPool.awaitTermination(10, TimeUnit.SECONDS)) {
        System.err.println("Client handlers did not finish in time, forcing shutdown.");
        // Forcefully shut down threads (this will interrupt the blocking readLine in ClientHandler)
        clientPool.shutdownNow();
      }
    } catch (InterruptedException e) {
      // Re-cancel and set interrupt flag
      clientPool.shutdownNow();
      Thread.currentThread().interrupt();
    }

    // 3. Shut down the SensorEngine (which closes its scheduler)
    engine.close();

    System.out.println("GreenhouseServer stopped.");
  }
}