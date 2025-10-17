package model;


public class ServerConfig {
  private final String name;
  private final String ipAddress;

  public ServerConfig(String name, String ipAddress) {
    this.name = name;
    this.ipAddress = ipAddress;
  }

  public String getName() { return name; }
  public String getIpAddress() { return ipAddress; }

  @Override
  public String toString() {
    return name + " (" + ipAddress + ")";
  }
}