package dto;

/**
 * Represents a welcome message from the server.
 * Contains the server name, version, and message of the day (motd).
 */
public class Welcome {
  /** Message type identifier. */
  public String type = "welcome";
  /** Server name. */
  public String server;
  /** Server version. */
  public String version;
  /** Message of the day. */
  public String motd;
}
