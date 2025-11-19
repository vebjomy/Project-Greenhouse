package net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Low-level TCP client that reads/writes newline-delimited JSON messages.
 */
public class NetworkClient implements AutoCloseable {

  private final MessageCodec codec = new MessageCodec();
  private final ExecutorService reader = Executors.newSingleThreadExecutor(
      r -> new Thread(r, "tcp-reader"));
  private volatile Socket socket;
  private volatile BufferedReader in;
  private volatile BufferedWriter out;
  private Consumer<String> onLine;
  private Consumer<Throwable> onError;

  public void setOnLine(Consumer<String> onLine) {
    this.onLine = onLine;
  }

  public void setOnError(Consumer<Throwable> onError) {
    this.onError = onError;
  }

  /**
   * Connect to server.
   *
   * @param host Server hostname or IP
   * @param port Server port
   * @throws IOException on connection error
   */
  public void connect(String host, int port) throws IOException {
    socket = new Socket(host, port);
    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
    reader.submit(this::readLoop);
  }

  /**
   * Read loop running in background thread.
   *
   */
  private void readLoop() {
    try {
      String line;
      // This readLine() is the blocking call that holds the thread
      while ((line = in.readLine()) != null) {
        if (onLine != null) {
          onLine.accept(line);
        }
      }
      if (onError != null) {
        onError.accept(new IOException("Connection closed by server"));
      }
    } catch (Throwable t) {
      // Ignore exceptions if the socket is closed during shutdown
      if (socket == null || !socket.isClosed()) {
        if (onError != null) {
          onError.accept(t);
        }
      }
    }
  }

  /**
   * Send a JSON line to the server.
   *
   * @param jsonLine JSON string (without newline)
   * @throws IOException on send error
   */
  public synchronized void sendLine(String jsonLine) throws IOException {
    if (out == null) {
      throw new IOException("Not connected");
    }
    out.write(jsonLine);
    out.flush();
  }

  /**
   * Gracefully shuts down the network client, closing the socket and the reader thread pool.
   */
  @Override
  public void close() throws IOException {
    System.out.println("Shutting down NetworkClient...");

    // 1. Close the Socket/Streams (interrupts the blocking readLine in readLoop)
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      System.err.println("Error closing socket: " + e.getMessage());
    } finally {
      socket = null;
      in = null;
      out = null;
    }

    // 2. Shut down the ExecutorService
    reader.shutdown();
    try {
      // Wait a short time for the reader loop to terminate
      if (!reader.awaitTermination(500, TimeUnit.MILLISECONDS)) {
        System.err.println("TCP reader thread did not stop, forcing shutdown.");
        // If it doesn't stop, force it (though closing the socket should have worked)
        reader.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      reader.shutdownNow();
    }

    System.out.println("NetworkClient stopped.");
  }

  /**
   * Get the message codec.
   *
   * @return MessageCodec instance
   */
  public MessageCodec codec() {
    return codec;
  }
}

