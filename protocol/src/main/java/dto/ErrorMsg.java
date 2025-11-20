package dto;

/**
 * Represents an error message in the protocol.
 * Contains the error type, request ID, error code, and descriptive message.
 */
public class ErrorMsg {
  public String type = "error";
  public String id;
  public String code;
  public String message;
}
