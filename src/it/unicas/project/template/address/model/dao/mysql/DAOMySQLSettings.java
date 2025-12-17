package it.unicas.project.template.address.model.dao.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class to hold all MySQL connection settings and provide static methods
 * for establishing and closing database connections and statements.
 * It implements a basic Singleton pattern for managing the current configuration.
 *
 * Access Keyword Explanation: {@code public} - This class must be public because it is
 * an essential configuration and utility class accessed by all DAO implementations.
 */
public class DAOMySQLSettings {

    // --- Static Final Fields (Default Configuration) ---
    public final static String DRIVERNAME = "com.mysql.cj.jdbc.Driver"; // JDBC driver class name for MySQL Connector/J 8.0+
    public final static String HOST = "localhost";                     // Default database server location
    public final static String USERNAME = "mar";                       // Default database user
    public final static String PWD = "ProjectSE1234";                  // Default database password
    public final static String SCHEMA = "dls_schema";                  // Default database name (schema)
    public final static String PARAMETERS = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Madrid"; // Essential connection parameters for modern MySQL/Java
    // Access Keyword Explanation: {@code public final static} - These are constants
    // used as the default settings for the application. They are public so they can
    // be referenced by other classes (e.g., if dynamically setting up the DB).

    // --- Instance Fields (Current Configuration) ---
    private String host = "localhost";
    private String userName = "mar";
    private String pwd = "ProjectSE1234";
    private String schema = "dls_schema";
    // Access Keyword Explanation: {@code private} - These fields hold the actual
    // current configuration settings. They are private to ensure they are accessed
    // and modified only through controlled public methods (getters/setters).

    // --- Static Initialization Block ---
    static {
        // This static block runs exactly once when the class is first loaded into memory by the JVM.
        try {
            // Register the JDBC driver class with the DriverManager.
            // This explicitly loads the MySQL driver, making it available for use in getConnection().
            Class.forName(DRIVERNAME);
        } catch (ClassNotFoundException e) {
            // This catch block handles the critical case where the MySQL Connector/J JAR is missing from the classpath.
            System.err.println("FATAL ERROR: MySQL JDBC Driver not found in classpath.");
            e.printStackTrace();
        }
    }

    // --- Singleton Pattern Implementation ---
    private static DAOMySQLSettings currentDAOMySQLSettings = null;
    // This private static field holds the single, globally accessible configuration instance (the Singleton).

    /**
     * <p>Creates and returns a new DAOMySQLSettings instance populated with the static default constants.</p>
     *
     * Access Keyword Explanation: {@code public static} - Public so external classes
     * can easily create a clean instance of default settings, and static because it
     * doesn't rely on the state of a specific instance.
     *
     * @return A new DAOMySQLSettings object with default values.
     */
    public static DAOMySQLSettings getDefaultDAOSettings() {
        DAOMySQLSettings daoMySQLSettings = new DAOMySQLSettings();
        // Populate the new instance with the static default values defined above
        daoMySQLSettings.host = HOST;
        daoMySQLSettings.userName = USERNAME;
        daoMySQLSettings.schema = SCHEMA;
        daoMySQLSettings.pwd = PWD;
        return daoMySQLSettings;
    }

    // --- Connection Utility Methods ---


    /**
     * <p>Establishes and returns a new Connection to the MySQL database
     * using the current DAOMySQLSettings configuration.</p>
     *
     * Access Keyword Explanation: {@code public static} - This is a core utility
     * method for obtaining database connections, accessible without an instance.
     *
     * @return A new Connection object to the database.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        if (currentDAOMySQLSettings == null) {
            currentDAOMySQLSettings = getDefaultDAOSettings(); // Ensure settings are loaded before attempting connection
        }
        // Build the full JDBC URL string using the current settings and
        // establish the connection using the driver manager and credentials
        return DriverManager.getConnection(
                "jdbc:mysql://" + currentDAOMySQLSettings.host + "/" + currentDAOMySQLSettings.schema + PARAMETERS,
                currentDAOMySQLSettings.userName,
                currentDAOMySQLSettings.pwd
        );
    }
}
