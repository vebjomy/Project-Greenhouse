package model;

/**
 * Class representing the configuration of a server.
 * It includes the server's name and IP address.
 * @author Your Name
 * @version 1.0
 */


public class ServerConfig {
  private final String name;
  private final String ipAddress;

  /**
   * Constructs a ServerConfig with the specified name and IP address.
   * @param name the name of the server
   * @param ipAddress the IP address of the server
   */

  public ServerConfig(String name, String ipAddress) {
    this.name = name;
    this.ipAddress = ipAddress;
  }

  /** Getter for the server name. */

  public String getName() { return name; }
  public String getIpAddress() { return ipAddress; }

  @Override
  public String toString() {
    return name + " (" + ipAddress + ")";
  }
}