package Zoho.LibraryManagementSystem.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class responsible for establishing and providing database connections.
 * It encapsulates the database connection details (URL, user, password)
 * and loads the necessary JDBC driver.
 */
public class DatabaseConnector {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Library"; // Replace 'library_system_db'
    private static final String DB_USER = "postgres"; // Replace with your username
    private static final String DB_PASSWORD = "postgres123"; // Replace with your password

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found. Include it in your library path.", e);
        }
    }
    /**
     * Establishes and returns a new connection to the PostgreSQL database.
     * The caller is responsible for closing this connection when done.
     *
     * @return A {@link Connection} object to the database.
     * @throws SQLException if a database access error occurs or the URL is null.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}