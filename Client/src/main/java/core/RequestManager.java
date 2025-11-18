package core;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks in-flight requests by correlation id. Completes futures upon matching responses.
 */
public class RequestManager {

  private final Map<String, CompletableFuture<JsonNode>> pending = new ConcurrentHashMap<>();

  /**
   * Generate a new client correlation id (e.g., "c-...").
   */
  public String newId() {
    return "c-" + UUID.randomUUID();
  }

  /**
   * Register a future for a given id.
   */
  public CompletableFuture<JsonNode> register(String id) {
    CompletableFuture<JsonNode> f = new CompletableFuture<>();
    pending.put(id, f);
    return f;
  }

  /**
   * Try to complete a future for this id with payload.
   */
  public void complete(String id, JsonNode payload) {
    if (id == null) {
      return;
    }
    var f = pending.remove(id);
    if (f != null) {
      f.complete(payload);
    }
  }

  /**
   * Fail the future for this id with error.
   */
  public void fail(String id, Throwable t) {
    var f = pending.remove(id);
    if (f != null) {
      f.completeExceptionally(t);
    }
  }
}

