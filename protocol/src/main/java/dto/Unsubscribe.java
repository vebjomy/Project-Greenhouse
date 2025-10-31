package dto;

import java.util.List;

public class Unsubscribe {
  public String type = "unsubscribe";
  public String id;
  public List<String> nodes;
  public List<String> events;
}
