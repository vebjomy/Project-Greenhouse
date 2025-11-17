package net;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for JSON serialization and deserialization.
 * Each JSON message is newline-delimited ("\n").
 */
public class MessageCodec {
  private final ObjectMapper mapper = new ObjectMapper();

  public String toJsonLine(Object msg) throws Exception {
    return mapper.writeValueAsString(msg) + "\n";
  }

  public <T> T fromJson(String json, Class<T> type) throws Exception {
    return mapper.readValue(json, type);
  }

  public ObjectMapper mapper() { return mapper; }
}

