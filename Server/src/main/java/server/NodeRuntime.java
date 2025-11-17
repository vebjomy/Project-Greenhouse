package server;

import java.util.concurrent.atomic.AtomicBoolean;

/** Runtime for a single node: environment + actuator states + sampling config. */
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

  /**
   * Creates a runtime context for a single node and initializes its state.
   *
   * <p>Initializes a fresh {@link EnvironmentState} and default actuator/sampling values:
   * fan/pump/CO2 off, window {@link EnvironmentState.WindowLevel#CLOSED}, and sampling interval set
   * to 1000&nbsp;ms. Associates this runtime with the provided {@code nodeId}.
   *
   * <p>Thread-safety: actuator flags are {@link java.util.concurrent.atomic.AtomicBoolean}s; {@code
   * window} and {@code intervalMs} are {@code volatile}.
   *
   * @param nodeId unique identifier of the node this runtime belongs to
   */
  public NodeRuntime(String nodeId) {
    this.nodeId = nodeId;
  }
}
