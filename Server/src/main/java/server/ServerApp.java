package server;

import java.io.IOException;

/**
 * Entry point for Greenhouse Server.
 */
public class ServerApp {

  /**
   * Main method to start the Greenhouse Server.
   *
   * @param args command line arguments
   * @throws Exception if server fails to start
   */
  public static void main(String[] args) throws IOException {
    int port = 5555;
    final GreenhouseServer server = new GreenhouseServer(port);

    // Register a shutdown hook to clean up non-daemon threads
    Runtime.getRuntime().addShutdownHook(
        new Thread(
            () -> {
              System.out.println("\n*** JVM Shutdown Signal Received ***");
              server.close();
              System.out.println("*** Shutdown complete. Exiting. ***");
            },
            "Server-Shutdown-Hook"));

    // Start the server (which will block in its while(true) loop)
    System.out.println("🌿 Greenhouse Server starting...");
    server.start();

    // Note: The execution reaches this point only if server.start() throws an exception (e.g.,
    // ServerSocket is closed from the outside)
    System.out.println("🌿 Greenhouse Server main thread exited.");
  }
}