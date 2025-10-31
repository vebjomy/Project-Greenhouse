package server;


import dto.SensorUpdate;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Ticks all nodes according to their sampling interval and pushes sensor_update.
 * Designed for multi-node / multi-client broadcasting (via provided callback).
 */
public class SensorEngine implements AutoCloseable {
  public interface Broadcast {
    void push(SensorUpdate su);
  }

  private final ScheduledExecutorService scheduler =
          Executors.newScheduledThreadPool(2, r -> new Thread(r, "sensor-engine"));
  private final NodeManager nodeManager;
  private final Broadcast broadcast;

  // Keep a scheduled future per nodeId to allow interval changes
  private final ConcurrentHashMap<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

  public SensorEngine(NodeManager nodeManager, Broadcast broadcast) {
    this.nodeManager = nodeManager;
    this.broadcast = broadcast;
  }

  /** Start ticking a node at its current interval. */
  public void scheduleNode(String nodeId) {
    cancelNode(nodeId);
    NodeRuntime rt = nodeManager.getRuntime(nodeId);
    if (rt == null) return;

    Runnable tick = () -> {
      // advance environment by 1.0s for simplicity (dt=1)
      nodeManager.advance(nodeId, 1.0);
      var snapshot = nodeManager.snapshot(nodeId);

      SensorUpdate su = new SensorUpdate();
      su.nodeId = nodeId;
      su.timestamp = System.currentTimeMillis();
      su.data = snapshot;

      broadcast.push(su);
    };

    // Fixed-rate by intervalMs
    ScheduledFuture<?> f = scheduler.scheduleAtFixedRate(tick, 0, Math.max(rt.intervalMs, 200), TimeUnit.MILLISECONDS);
    tasks.put(nodeId, f);
  }

  /** Cancel ticking a node. */
  public void cancelNode(String nodeId) {
    var f = tasks.remove(nodeId);
    if (f != null) f.cancel(false);
  }

  /** Re-schedule when interval changes. */
  public void rescheduleNode(String nodeId) {
    scheduleNode(nodeId);
  }

  /** Called when a new node is created. */
  public void onNodeAdded(String nodeId) {
    scheduleNode(nodeId);
  }

  /** Called when a node is deleted. */
  public void onNodeRemoved(String nodeId) {
    cancelNode(nodeId);
  }

  @Override public void close() {
    scheduler.shutdownNow();
  }
}
