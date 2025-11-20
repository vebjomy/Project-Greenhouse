package net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
      while ((line = in.readLine()) != null) {
        if (onLine != null) {
          onLine.accept(line);
        }
      }

      // Connection closed by server
      System.out.println("🔌 [NetworkClient] Connection closed by server");
      if (onError != null) {
        onError.accept(new IOException("Connection closed by server"));
      }
    } catch (java.net.SocketException e) {
      System.err.println("❌ [NetworkClient] Socket error: " + e.getMessage());
      if (onError != null) {
        onError.accept(e);
      }
    } catch (java.io.EOFException e) {
      System.err.println("❌ [NetworkClient] Unexpected end of stream");
      if (onError != null) {
        onError.accept(new IOException("Unexpected connection termination", e));
      }
    } catch (Throwable t) {
      System.err.println("❌ [NetworkClient] Read error: " + t.getMessage());
      t.printStackTrace();
      if (onError != null) {
        onError.accept(t);
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
      throw new IOException("Not connected to server");
    }

    if (socket == null || socket.isClosed()) {
      throw new IOException("Socket is closed");
    }

    try {
    out.write(jsonLine);
    out.flush();
    } catch (IOException e) {
      System.err.println("❌ [NetworkClient] Send error: " + e.getMessage());
      throw new IOException("Failed to send message: " + e.getMessage(), e);
    }
  }

  @Override
  public void close() throws IOException {
    try {
      if (socket != null) {
        socket.close();
      }
    } finally {
      reader.shutdownNow();
    }
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

