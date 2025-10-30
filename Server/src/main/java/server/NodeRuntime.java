package server;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runtime for a single node: environment + actuator states + sampling config.
 */
public class NodeRuntime {
  public final String nodeId;
  public final EnvironmentState env = new EnvironmentState();

  // Actuator states
  public final AtomicBoolean fanOn = new AtomicBoolean(false);
  public final AtomicBoolean pumpOn = new AtomicBoolean(false);
  public final AtomicBoolean co2On = new AtomicBoolean(false);
  public volatile EnvironmentState.WindowLevel window = EnvironmentState.WindowLevel.CLOSED;

  // Sampling interval (ms)
  public volatile int intervalMs = 1000;

  public NodeRuntime(String nodeId) {
    this.nodeId = nodeId;
  }
}

