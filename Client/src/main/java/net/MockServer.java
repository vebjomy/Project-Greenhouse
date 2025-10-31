package net;

import dto.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * A fake greenhouse server that simulates sensor updates
 * and accepts commands from the client.
 */
public class MockServer {
  private final MessageCodec codec = new MessageCodec();
  private final ScheduledExecutorService ses =
          Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "mock-scheduler"));
  private Consumer<String> deliver;
  private final Random rnd = new Random();

  public void attachClient(Consumer<String> clientLineConsumer) {
    this.deliver = clientLineConsumer;

    // Send welcome and topology immediately after connection
    emit(new Welcome() {{
      server = "mock-greenhouse";
      version = "1.0";
    }});

    Topology topo = new Topology();
    Topology.Node n = new Topology.Node();
    n.id = "node-1";
    n.sensors = List.of("temperature", "humidity", "light", "ph");
    n.actuators = List.of("fan", "water_pump", "co2", "window");
    topo.nodes = List.of(n);
    emit(topo);

    // Periodically send sensor updates
    ses.scheduleAtFixedRate(() -> {
      SensorUpdate su = new SensorUpdate();
      su.nodeId = "node-1";
      su.timestamp = System.currentTimeMillis();
      su.data = Map.of(
              "temperature", 21.0 + rnd.nextDouble() * 3,
              "humidity", 50 + rnd.nextDouble() * 10,
              "light", 400 + rnd.nextInt(60),
              "ph", 6.2 + rnd.nextDouble() * 0.4
      );
      emit(su);
    }, 0, 1, TimeUnit.SECONDS);
  }

  public void onClientCommand(Command cmd) {
    // Immediately respond with ACK
    Ack ack = new Ack();
    ack.id = cmd.id != null ? cmd.id : UUID.randomUUID().toString();
    ack.status = "ok";
    emit(ack);
  }

  private void emit(Object dto) {
    try {
      String line = codec.toJsonLine(dto);
      if (deliver != null) deliver.accept(line);
    } catch (Exception e) { e.printStackTrace(); }
  }

  public void shutdown() { ses.shutdownNow(); }
}

