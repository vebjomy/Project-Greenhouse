package server;

import dto.SensorUpdate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Ticks all nodes according to their sampling interval and pushes sensor_update. Designed for
 * multi-node / multi-client broadcasting (via provided callback).
 */
public class SensorEngine implements AutoCloseable {

  /**
   * Callback interface for broadcasting sensor updates.
   */
  public interface Broadcast {

    /**
     * Push a sensor update.
     *
     * @param su the sensor update to push
     */
    void push(SensorUpdate su);
  }

  // Use a thread factory for better thread identification
  private final ScheduledExecutorService scheduler =
      Executors.newScheduledThreadPool(2, r -> new Thread(r, "sensor-engine"));
  private final NodeManager nodeManager;
  private final Broadcast broadcast;

  // Keep a scheduled future per nodeId to allow interval changes
  private final ConcurrentHashMap<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

  /**
   * Constructor for SensorEngine.
   *
   * @param nodeManager the NodeManager to manage nodes
   * @param broadcast   the Broadcast callback to push sensor updates
   */
  public SensorEngine(NodeManager nodeManager, Broadcast broadcast) {
    this.nodeManager = nodeManager;
    this.broadcast = broadcast;
  }

  /**
   * Start ticking a node at its current interval.
   */
  public void scheduleNode(String nodeId) {
    cancelNode(nodeId);
    NodeRuntime rt = nodeManager.getRuntime(nodeId);
    if (rt == null) {
      return;
    }

    Runnable tick =
        () -> {
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
    ScheduledFuture<?> f =
        scheduler.scheduleAtFixedRate(tick, 0, Math.max(rt.intervalMs, 200), TimeUnit.MILLISECONDS);
    tasks.put(nodeId, f);
  }

  /**
   * Cancel ticking a node.
   */
  public void cancelNode(String nodeId) {
    var f = tasks.remove(nodeId);
    if (f != null) {
      // false means we don't interrupt the running task, but prevent future ones
      f.cancel(false);
    }
  }

  /**
   * Re-schedule when interval changes.
   */
  public void rescheduleNode(String nodeId) {
    scheduleNode(nodeId);
  }

  /**
   * Called when a node is added (initial schedule).
   *
   * @param nodeId the ID of the new node.
   */
  public void onNodeAdded(String nodeId) {
    scheduleNode(nodeId);
  }

  /**
   * Called when a node is removed (cancellation).
   *
   * @param nodeId the ID of the removed node.
   */
  public void onNodeRemoved(String nodeId) {
    cancelNode(nodeId);
  }

  /**
   * Shuts down the scheduled thread pool, waiting up to 5 seconds for tasks to complete.
   */
  @Override
  public void close() {
    System.out.println("Stopping SensorEngine scheduler...");
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
        System.err.println("SensorEngine tasks did not finish in time, forcing shutdown.");
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      scheduler.shutdownNow();
    }
    System.out.println("SensorEngine stopped.");
  }
}