package net;

/**
 * A utility class that holds constants for different message types used in the network communication.
 */
public final class MessageTypes {
  public static final String HELLO = "hello";
  public static final String AUTH = "auth";
  public static final String AUTH_RESPONSE = "auth_response";
  public static final String REGISTER = "register";
  public static final String REGISTER_RESPONSE = "register_response";
  public static final String WELCOME = "welcome";
  public static final String GET_USERS = "get_users";
  public static final String USERS_LIST = "users_list";
  public static final String UPDATE_USER = "update_user";
  public static final String DELETE_USER = "delete_user";
  public static final String PING = "ping";
  public static final String PONG = "pong";

  public static final String GET_TOPOLOGY = "get_topology";
  public static final String TOPOLOGY = "topology";
  public static final String NODE_CHANGE = "node_change";

  public static final String CREATE_NODE = "create_node";
  public static final String UPDATE_NODE = "update_node";
  public static final String DELETE_NODE = "delete_node";
  public static final String SET_SAMPLING = "set_sampling";

  public static final String SUBSCRIBE = "subscribe";
  public static final String UNSUBSCRIBE = "unsubscribe";
  public static final String SENSOR_UPDATE = "sensor_update";

  public static final String COMMAND = "command";
  public static final String ACK = "ack";
  public static final String ERROR = "error";

  public static final String LAST_VALUES = "last_values";

  private MessageTypes(){}
}

