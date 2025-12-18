package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Genre;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.GenreDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * MySQL implementation of the GenreDAO interface.
 * <p>
 * This class is responsible for all Create, Read, Update, and Delete (CRUD) operations
 * related to the 'Genre' entity in the MySQL database.
 * </p>
 *
 * Access Keyword Explanation: {@code public} - This class must be public so it can
 * be instantiated and used by the service layer.
 */
public class GenreDAOMySQLImpl implements GenreDAO {

    // --- Singleton Fields ---
    private static GenreDAOMySQLImpl instance; // The single instance of this class (Singleton pattern)
    private static Logger logger = null;       // Logger for centralized error reporting

    /**
     * Private constructor to enforce the Singleton pattern.
     * <p>
     * Prevents external classes from using 'new GenreDAOMySQLImpl()'.
     * </p>
     *
     * Access Keyword Explanation: {@code private} - Essential for the Singleton pattern.
     */
    private GenreDAOMySQLImpl() {}

    /**
     * Provides the global access point to the single instance of the DAO.
     * <p>
     * Implements lazy initialization of the Singleton.
     * </p>
     *
     * Access Keyword Explanation: {@code public static} - Public for external access,
     * static so it can be called directly on the class without an instance.
     *
     * @return The single GenreDAOMySQLImpl instance.
     */
    public static GenreDAOMySQLImpl getInstance() {
        if (instance == null) {
            // Initialize instance and logger only on first call
            instance = new GenreDAOMySQLImpl();
            logger = Logger.getLogger(GenreDAOMySQLImpl.class.getName());
        }
        return instance;
    }

    /**
     * Retrieves all Genre records from the database.
     * <p>
     * Access Keyword Explanation: {@code public} - Implements the method defined in the GenreDAO interface.
     * </p>
     *
     * @return A list containing all Genre objects, or an empty list if none are found or an error occurs.
     */
    @Override
    public List<Genre> selectAll() {
        List<Genre> genres = new ArrayList<>();
        // SQL query to fetch all genres, ordered alphabetically for presentation
        String sql = "SELECT * FROM GENRE ORDER BY genre";

        // Use try-with-resources to ensure Connection, PreparedStatement, and ResultSet are closed automatically.
        try (Connection conn = DAOMySQLSettings.getConnection();   // Get new database connection from utility
             PreparedStatement ps = conn.prepareStatement(sql);    // Prepare the static SQL statement
             ResultSet rs = ps.executeQuery()) {                   // Execute the query and get results

            // Iterate through the result set row by row
            while (rs.next()) {
                // Map the current row's data to a new Genre object
                genres.add(new Genre(
                        rs.getInt("idGenre"),
                        rs.getString("genre")
                ));
            }

        } catch (SQLException e) {
            // Handle SQL exceptions (e.g., connection errors, syntax errors)
            System.err.println("Error in selectAll(): " + e.getMessage());
            logger.severe("Error in selectAll(): " + e.getMessage());
        }

        return genres;
    }

    /**
     * Finds the primary key (idGenre) corresponding to a given genre name.
     * <p>
     * Access Keyword Explanation: {@code public} - Implements the method defined in the GenreDAO interface.
     * </p>
     *
     * @param name The name of the genre to search for (e.g., "Fiction").
     * @return The ID of the matching genre, or null if no match is found.
     */
    @Override
    public Integer findIdByName(String name) {
        // SQL query uses a placeholder (?) for the genre name to prevent SQL injection (prepared statement)
        String sql = "SELECT idGenre FROM GENRE WHERE genre=?";

        // Use try-with-resources for Connection and PreparedStatement.
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Set the first placeholder (?) value to the provided genre name
            ps.setString(1, name);

            // Execute the query within its own try-with-resources block for the ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // If a row is found, return the ID
                    return rs.getInt("idGenre");
                }
            } // rs is closed here

        } catch (SQLException e) {
            // Log the error but don't rethrow, as a null return is an acceptable outcome here
            System.err.println("Error in findIdByName(): " + e.getMessage());
            logger.severe("Error in findIdByName(): " + e.getMessage());
        }

        return null; // If no row was found or an error occurred, return null
    }

    /**
     * Inserts a new Genre record into the database.
     * <p>
     * Access Keyword Explanation: {@code public} - Implements the method defined in the GenreDAO interface.
     * </p>
     *
     * @param g The Genre object containing the new name to insert.
     * @throws DAOException If any SQLException occurs, it is wrapped in a DAOException and rethrown.
     */
    @Override
    public void insert(Genre g) throws DAOException {
        // SQL statement for insertion, only the genre name is needed (ID is auto-generated)
        String sql = "INSERT INTO GENRE (genre) VALUES (?)";

        // Use try-with-resources for Connection and PreparedStatement.
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, g.getGenre()); // Set the placeholder (?) value to the genre name from the object
            ps.executeUpdate(); // Execute the update. This returns the number of affected rows.

        } catch (SQLException e) {
            throw new DAOException("In insert(): " + e.getMessage());
        }
    }
}
