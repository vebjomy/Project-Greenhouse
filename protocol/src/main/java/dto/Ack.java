package dto;

public class Ack {
  public String type = "ack";
  public String id;
  public String status;
  public String nodeId; // optional (e.g., create_node reply)
}