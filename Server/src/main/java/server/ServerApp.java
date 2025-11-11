package server;

import java.sql.SQLOutput;

/**
 * Entry point for Greenhouse Server.
 */
public class ServerApp {
  public static void main(String[] args) throws Exception {
    int port = 5555;
    GreenhouseServer server = new GreenhouseServer(port);
    server.start();
    System.out.println("🌿 Greenhouse Server started on port " + port);
  }
}

