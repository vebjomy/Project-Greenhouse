package core;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Simple connection test to a server.
 */
public class ConnectionTest {

  /**
   * Main method to test connection to server.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    String serverAddress = "127.0.0.1";
    int serverPort = 5555;

    try (Socket socket = new Socket(serverAddress, serverPort);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
      out.println("Connection Test");
      System.out.println("Connection to server successful!");
    } catch (IOException e) {
      System.err.println("Failed to connect to server: " + e.getMessage());
    }
  }
}