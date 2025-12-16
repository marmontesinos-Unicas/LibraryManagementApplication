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

    // --- Getters for Current Configuration ---
    public String getHost() { return host; }
    public String getUserName() { return userName; }
    public String getPwd() { return pwd; }
    public String getSchema() { return schema; }
    // Access Keyword Explanation: {@code public} - Getters are public to provide
    // read-only access to the current configuration settings to external classes.

    // --- Setters for Current Configuration ---
    public void setHost(String host) { this.host = host; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setPwd(String pwd) { this.pwd = pwd; }
    public void setSchema(String schema) { this.schema = schema; }
    // Access Keyword Explanation: {@code public} - Setters are public to allow
    // external classes (e.g., a configuration controller) to change the DB connection
    // parameters at runtime.

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
     * <p>Retrieves the current (active) DAO settings instance.
     * Initializes to default settings if no instance has been set yet.</p>
     *
     * <p>Access Keyword Explanation: {@code public static} - This is the standard
     * access point for the Singleton instance. It must be static so it can be called
     * without an instance of the class.</p>
     *
     * @return The single, current DAOMySQLSettings instance.
     */
    public static DAOMySQLSettings getCurrentDAOMySQLSettings() {
        if (currentDAOMySQLSettings == null) {
            // Lazy initialization: create default settings only when they are first requested.
            currentDAOMySQLSettings = getDefaultDAOSettings();
        }
        return currentDAOMySQLSettings;
    }

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

    /**
     * <p>Sets the active configuration instance. Used to switch connection details at runtime.</p>
     *
     * Access Keyword Explanation: {@code public static} - Allows other components
     * to globally change the application's database configuration.
     *
     * @param daoMySQLSettings The new settings instance to use globally.
     */
    public static void setCurrentDAOMySQLSettings(DAOMySQLSettings daoMySQLSettings) {
        // Overwrites the current global settings with a new provided set of settings.
        currentDAOMySQLSettings = daoMySQLSettings;
    }

    // --- Connection Utility Methods ---

    /**
     * <p>Establishes a new database connection and creates a Statement object for executing SQL queries.
     * This method is convenient for simple, one-off query execution.</p>
     *
     * Access Keyword Explanation: {@code public static} - This is a central utility
     * method used by all DAO classes to interact with the database; it must be static
     * for easy access without requiring an instance.
     *
     * @return A new Statement object ready for use.
     * @throws SQLException if a database access error occurs.
     */
    public static Statement getStatement() throws SQLException {
        Connection connection = getConnection(); // Get a Connection object first, then create a Statement from it.
        return connection.createStatement(); // Statement is used to execute static SQL statements and return their results.
    }

    /**
     * <p>Closes the provided Statement and its underlying Connection, ensuring resources are freed.
     * JDBC resources MUST be closed to prevent resource leaks (connections, file handles).</p>
     *
     * Access Keyword Explanation: {@code public static} - Essential utility for
     * resource management, must be static for easy access by DAOs.
     *
     * @param st The Statement object to be closed.
     * @throws SQLException if a database access error occurs during closing.
     */
    public static void closeStatement(Statement st) throws SQLException {
        // Retrieve the Connection object associated with the Statement
        Connection conn = st.getConnection();

        // 1. Close the connection first
        if (conn != null) {
            conn.close();
        }
        // 2. Close the Statement itself
        if (st != null) {
            st.close();
        }
    }

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
