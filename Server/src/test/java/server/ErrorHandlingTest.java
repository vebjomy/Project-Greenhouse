package server;

import com.fasterxml.jackson.databind.JsonNode;
import dto.*;
import net.MessageCodec;
import org.junit.jupiter.api.*;
import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for error handling in the Greenhouse Server.
 */
class ErrorHandlingTest {

  private static GreenhouseServer server;
  private static Thread serverThread;
  private static final int TEST_PORT = 5556;
  private MessageCodec codec = new MessageCodec();

  @BeforeAll
  static void startServer() throws InterruptedException {
    server = new GreenhouseServer(TEST_PORT);
    serverThread = new Thread(() -> {
      try {
        server.start();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    serverThread.start();
    Thread.sleep(1000); // Give server time to start
  }

  @AfterAll
  static void stopServer() {
    if (serverThread != null) {
      serverThread.interrupt();
    }
  }

  /**
   * Test that server returns INVALID_JSON error for malformed JSON.
   */
  @Test
  void testMalformedJson() throws Exception {
    try (Socket socket = new Socket("localhost", TEST_PORT);
         BufferedReader in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()));
         PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

      // Read welcome message
      String welcome = in.readLine();
      assertNotNull(welcome);

      // Send malformed JSON
      out.println("{invalid json}");
      out.flush();

      // Read error response
      String response = in.readLine();
      assertNotNull(response);

      JsonNode json = codec.mapper().readTree(response);
      assertEquals("error", json.get("type").asText());
      assertEquals("INVALID_JSON", json.get("code").asText());
      assertTrue(json.get("message").asText().contains("Malformed JSON"));
    }
  }

  /**
   * Test that server returns UNSUPPORTED error for unknown message type.
   */
  @Test
  void testUnknownMessageType() throws Exception {
    try (Socket socket = new Socket("localhost", TEST_PORT);
         BufferedReader in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()));
         PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

      // Read welcome
      in.readLine();

      // Send unknown message type
      String msg = "{\"type\":\"unknown_type\",\"id\":\"test-1\"}";
      out.println(msg);
      out.flush();

      // Read error response
      String response = in.readLine();
      JsonNode json = codec.mapper().readTree(response);

      assertEquals("error", json.get("type").asText());
      assertEquals("test-1", json.get("id").asText());
      assertEquals("UNSUPPORTED", json.get("code").asText());
    }
  }
}