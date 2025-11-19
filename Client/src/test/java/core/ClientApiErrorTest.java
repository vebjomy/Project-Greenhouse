package core;

import org.junit.jupiter.api.*;
import java.util.concurrent.CompletionException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ClientApi error handling.
 */
class ClientApiErrorTest {

  private ClientApi api;
  private static final String SERVER_HOST = "localhost";
  private static final int SERVER_PORT = 5555;

  @BeforeEach
  void setup() throws Exception {
    api = new ClientApi();
    api.connect(SERVER_HOST, SERVER_PORT).get();
  }

  @AfterEach
  void cleanup() throws Exception {
    if (api != null) {
      api.close();
    }
  }

  /**
   * Test that creating a node with empty name throws exception.
   */
  @Test
  void testCreateNodeWithEmptyName() {
    dto.Topology.Node node = new dto.Topology.Node();
    node.name = "";
    node.location = "Test";
    node.ip = "192.168.1.1";

    CompletionException exception = assertThrows(
            CompletionException.class,
            () -> api.createNode(node).join()
    );

    assertTrue(exception.getCause().getMessage().contains("INVALID_ARG"));
    assertTrue(exception.getCause().getMessage().contains("name is required"));
  }

  /**
   * Test that updating non-existent node throws exception.
   */
  @Test
  void testUpdateNonExistentNode() {
    CompletionException exception = assertThrows(
            CompletionException.class,
            () -> api.updateNode("node-999999",
                    java.util.Map.of("name", "New Name")).join()
    );

    assertTrue(exception.getCause().getMessage().contains("NOT_FOUND"));
  }

  /**
   * Test that commanding non-existent node throws exception.
   */
  @Test
  void testCommandNonExistentNode() {
    CompletionException exception = assertThrows(
            CompletionException.class,
            () -> api.sendCommand("node-999999", "fan", "set",
                    java.util.Map.of("on", true)).join()
    );

    assertTrue(exception.getCause().getMessage().contains("NOT_FOUND"));
  }
}
