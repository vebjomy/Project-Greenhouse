package net;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Low-level TCP client that reads/writes newline-delimited JSON messages.
 */
public class NetworkClient implements AutoCloseable {
  private final MessageCodec codec = new MessageCodec();
  private final ExecutorService reader = Executors.newSingleThreadExecutor(r -> new Thread(r, "tcp-reader"));
  private volatile Socket socket;
  private volatile BufferedReader in;
  private volatile BufferedWriter out;
  private Consumer<String> onLine;
  private Consumer<Throwable> onError;

  public void setOnLine(Consumer<String> onLine){ this.onLine = onLine; }
  public void setOnError(Consumer<Throwable> onError){ this.onError = onError; }

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
        if (onLine != null) onLine.accept(line);
      }
      if (onError != null) {
        onError.accept(new IOException("Connection closed by server"));
      }
    } catch (Throwable t) {
      if (onError != null) onError.accept(t);
    }
  }

  public synchronized void sendLine(String jsonLine) throws IOException {
    if (out == null) throw new IOException("Not connected");
    out.write(jsonLine);
    out.flush();
  }

  @Override public void close() throws IOException {
    try { if (socket != null) socket.close(); } finally { reader.shutdownNow(); }
  }

  public MessageCodec codec(){ return codec; }
}

