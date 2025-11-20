package net;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for JSON serialization and deserialization. Each JSON message is newline-delimited
 * ("\n").
 */
public class MessageCodec {
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Serializes an object to a JSON string with a newline at the end.
   *
   * @param msg the object to serialize
   * @return the JSON string representation of the object with a newline
   * @throws Exception if serialization fails
   */
  public String toJsonLine(Object msg) throws Exception {
    return mapper.writeValueAsString(msg) + "\n";
  }

  /**
   * Deserializes a JSON string to an object of the specified type.
   *
   * @param json the JSON string to deserialize
   * @param type the class of the object to deserialize to
   * @return the deserialized object
   * @param <T> the type of the object
   * @throws Exception if deserialization fails
   */
  public <T> T fromJson(String json, Class<T> type) throws Exception {
    return mapper.readValue(json, type);
  }

  /**
   * Gets the ObjectMapper instance used for JSON serialization and deserialization.
   *
   * @return the ObjectMapper instance
   */
  public ObjectMapper mapper() {
    return mapper;
  }
}
