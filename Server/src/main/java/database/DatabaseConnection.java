package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the connection to the SQLite database for storing and retrieving sensor data. This class
 * is responsible for establishing a connection and creating necessary tables.
 */
public class DatabaseConnection {

  private static final String DB_NAME = "greenhouse_sensors.db"; // Database file name
  private static final String DB_URL = "jdbc:sqlite:" + DB_NAME; // JDBC URL for SQLite

  /**
   * Establishes a connection to the SQLite database. If the database file does not exist, it will
   * be created. It also ensures that the necessary tables are created.
   *
   * @return Connection object to the database
   */
  public static Connection connect() {
    try {
      Connection connection = DriverManager.getConnection(DB_URL);
      createTablesIfNotExist(connection);
      return connection;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to connect to database", e);
    }
  }

  /**
   * Creates necessary tables in the database if they do not already exist.
   *
   * @param connection the database connection
   */
  private static void createTablesIfNotExist(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      // Create sensor_readings table
      String createSensorReadingsTable = "CREATE TABLE IF NOT EXISTS sensor_readings ("
          + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
          + "environment_id TEXT NOT NULL,"
          + "timestamp TEXT NOT NULL,"
          + "temperature REAL,"
          + "humidity REAL,"
          + "light_level REAL,"
          + "co2_level REAL,"
          + "soil_ph REAL,"
          + "soil_moisture REAL"
          + ");";
      statement.execute(createSensorReadingsTable);
    }
  }
}
