package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Hold;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.ExpiringHoldInfo;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * MySQL implementation for the Hold data access object (DAO).
 * <p>
 * This class handles all CRUD operations and specific queries (like finding expiring holds)
 * related to the 'holds' table in the database. It implements the generic {@code DAO<Hold>} interface.
 * </p>
 *
 * Access Keyword Explanation: {@code public} - Allows the service layer to interact with this specific DAO implementation.
 */
public class HoldDAOMySQLImpl implements DAO<Hold> {

    // --- Singleton Fields ---
    // The single instance of this DAO (Singleton pattern). It is declared as the interface type DAO<Hold>
    // to promote programming to the interface.
    private static DAO<Hold> dao = null;
    private static Logger logger = null; // Logger for error reporting
    // Formatter used to convert LocalDateTime objects into the string format required by MySQL
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Protected constructor to enforce the Singleton pattern.
     * <p>
     * Prevents direct instantiation but allows for potential subclassing within the same package.
     * </p>
     *
     * Access Keyword Explanation: {@code protected} - Restricts external creation but allows package/subclass access.
     */
    protected HoldDAOMySQLImpl() {}

    /**
     * Provides the global access point to the single instance of the Hold DAO.
     * <p>
     * Implements lazy initialization of the Singleton.
     * </p>
     *
     * Access Keyword Explanation: {@code public static} - Provides global, class-level access.
     *
     * @return The single instance of the HoldDAOMySQLImpl, cast to the generic DAO interface.
     */
    public static DAO<Hold> getInstance() {
        if (dao == null) {
            // Initialization happens only on the first call
            dao = new HoldDAOMySQLImpl();
            logger = Logger.getLogger(HoldDAOMySQLImpl.class.getName());
        }
        return dao;
    }

    // Assumed Hold Pickup Window: 1 day. This constant defines the business rule for hold validity.
    private static final int HOLD_VALIDITY_DAYS = 1;

    /**
     * SQL to fetch active holds for a specific user, joining with the materials table
     * to include material title and author in the result set.
     * <p>
     * The WHERE clause uses the MySQL function DATE_ADD to check if the hold's calculated
     * expiration date is still in the future (greater than the current date, CURDATE()).
     * </p>
     *
     * Access Keyword Explanation: {@code private static final} - Private to this class,
     * static because it's shared by all methods, and final because the SQL string should not change.
     */
    private static final String SQL_SELECT_ACTIVE_HOLDS_FOR_USER =
            "SELECT " +
                    "h.idHold, " +
                    "h.hold_date, " +
                    "m.title, " +
                    "m.author " +
                    "FROM " +
                    "holds h " +
                    "JOIN " +
                    "materials m ON h.idMaterial = m.idMaterial " +
                    "WHERE " +
                    "h.idUser = ? " +
                    // Only condition needed: The hold's expiration date (hold_date + X days) is in the future.
                    "AND DATE_ADD(h.hold_date, INTERVAL " + HOLD_VALIDITY_DAYS + " DAY) > CURDATE()";

    /**
     * Selects Hold records based on criteria specified in the input Hold object (h).
     * <p>
     * Implements a dynamic query builder: only fields in the input object (h) that are not
     * default (-1 or null) are used to generate the WHERE clause.
     * </p>
     *
     * @param h A Hold object used as a search template.
     * @return A list of matching Hold objects.
     * @throws DAOException if a database error occurs.
     */
    @Override
    public List<Hold> select(Hold h) throws DAOException {
        List<Hold> list = new ArrayList<>();
        if (h == null) {
            // If null is passed, treat it as a request to fetch all records (all IDs are -1)
            h = new Hold(); // all -1
        }
        // Start of dynamic query. 'WHERE 1=1' is a common pattern to easily chain AND clauses.
        StringBuilder sql = new StringBuilder("SELECT * FROM holds WHERE 1=1");
        if (h.getIdHold() != -1) sql.append(" AND idHold = ?");
        if (h.getIdUser() != -1) sql.append(" AND idUser = ?");
        if (h.getIdMaterial() != -1) sql.append(" AND idMaterial = ?");
        if (h.getHold_date() != null) sql.append(" AND hold_date = ?");

        // Use try-with-resources to ensure database resources are closed
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // Dynamically bind parameters to the SQL placeholders (?) based on which clauses were added
            int index = 1;
            if (h.getIdHold() != -1) ps.setInt(index++, h.getIdHold());
            if (h.getIdUser() != -1) ps.setInt(index++, h.getIdUser());
            if (h.getIdMaterial() != -1) ps.setInt(index++, h.getIdMaterial());
            if (h.getHold_date() != null) ps.setTimestamp(index++, java.sql.Timestamp.valueOf(h.getHold_date()));

            logger.info("SQL: " + ps); // Log the final executed SQL string

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Map database columns to a new Hold object
                    Hold hold = new Hold(
                            rs.getInt("idHold"),
                            rs.getInt("idUser"),
                            rs.getInt("idMaterial"),
                            // Convert Timestamp from DB to modern Java LocalDateTime object
                            rs.getTimestamp("hold_date") != null ? rs.getTimestamp("hold_date").toLocalDateTime() : null
                    );
                    list.add(hold);
                }
            }
        } catch (SQLException e) {
            // Wrap the technical SQL error into a business-friendly DAOException and rethrow
            throw new DAOException("In select(): " + e.getMessage());
        }

        return list;
    }

    /**
     * Retrieves all holds for a user that are currently active based on the validity period.
     * <p>
     * This method joins the 'holds' table with the 'materials' table to enrich the result
     * with material details, returning specialized {@code ExpiringHoldInfo} objects.
     * </p>
     *
     * @param userId The ID of the currently logged-in user.
     * @return A list of ExpiringHoldInfo objects.
     * @throws DAOException if a database error occurs.
     */
    public List<ExpiringHoldInfo> getExpiringHoldsForUser(int userId) throws DAOException {
        List<ExpiringHoldInfo> expiringHolds = new ArrayList<>();

        // Use the predefined SQL_SELECT_ACTIVE_HOLDS_FOR_USER query
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ACTIVE_HOLDS_FOR_USER)) {

            // Bind the user ID to the placeholder
            ps.setInt(1, userId);

            logger.info("SQL: " + ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int holdId = rs.getInt("idHold");
                    String title = rs.getString("title");
                    String author = rs.getString("author");

                    // Get the hold creation date from the database
                    LocalDateTime holdDateCreation = rs.getTimestamp("hold_date").toLocalDateTime();

                    // Calculate the final Expiration Date (Creation Date + HOLD_VALIDITY_DAYS)
                    LocalDate holdExpirationDate = holdDateCreation.toLocalDate().plusDays(HOLD_VALIDITY_DAYS);

                    // Create the specialized info object for the service/view layer
                    ExpiringHoldInfo item = new ExpiringHoldInfo(holdId, title, author, holdExpirationDate);
                    expiringHolds.add(item);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("In getExpiringHoldsForUser(): " + e.getMessage());
        }

        return expiringHolds;
    }

    /**
     * Inserts a new Hold record into the database and retrieves the auto-generated ID.
     * <p>
     * Calls {@code verifyObject} for basic validation before execution.
     * </p>
     *
     * @param h The Hold object to insert. The ID field will be updated upon successful insertion.
     * @throws DAOException if validation fails or a database error occurs.
     */
    @Override
    public void insert(Hold h) throws DAOException {
        // Perform basic input validation
        verifyObject(h);
        String sql = "INSERT INTO holds (idUser, idMaterial, hold_date) VALUES (?, ?, ?)";

        try (Connection conn = DAOMySQLSettings.getConnection();
             // Instruct PreparedStatement to return the auto-generated key (idHold)
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, h.getIdUser());
            ps.setInt(2, h.getIdMaterial());
            // Convert Java LocalDateTime back to SQL Timestamp for database insertion
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(h.getHold_date()));
            logger.info("SQL: " + ps);
            ps.executeUpdate(); // Execute the INSERT statement

            // Retrieve the auto-generated primary key (idHold)
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) h.setIdHold(rs.getInt(1)); // Update the original Hold object with the new ID
            }
        } catch (SQLException e) {
            throw new DAOException("In insert(): " + e.getMessage());
        }
    }

    /**
     * Updates an existing Hold record in the database.
     * <p>
     * Calls {@code verifyObject} for basic validation before execution.
     * </p>
     *
     * @param h The Hold object with updated data (must include a valid idHold).
     * @throws DAOException if validation fails or a database error occurs.
     */
    @Override
    public void update(Hold h) throws DAOException {
        verifyObject(h);
        String sql = "UPDATE holds SET idUser=?, idMaterial=?, hold_date=? WHERE idHold=?";

        // FIXED: Added Connection to try-with-resources
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, h.getIdUser());
            ps.setInt(2, h.getIdMaterial());
            // Format the LocalDateTime object into a string using the predefined FORMATTER
            ps.setString(3, h.getHold_date().format(FORMATTER));
            ps.setInt(4, h.getIdHold()); // Use ID for the WHERE clause

            logger.info("SQL: " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In update(): " + e.getMessage());
        }
    }

    /**
     * Deletes a Hold record from the database based on its ID.
     * <p>
     * Ensures the input object is not null and has a valid ID before proceeding.
     * </p>
     *
     * @param h The Hold object containing the ID of the record to delete.
     * @throws DAOException if the ID is missing or a database error occurs.
     */
    @Override
    public void delete(Hold h) throws DAOException {
        // Validate that a record ID is present for deletion
        if (h == null || h.getIdHold() == -1) {
            throw new DAOException("In delete: idHold cannot be null");
        }

        String sql = "DELETE FROM holds WHERE idHold=?";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, h.getIdHold());
            logger.info("SQL: " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In delete(): " + e.getMessage());
        }
    }

    /**
     * Selects all Hold records from the database.
     * <p>
     * This is an implementation of the {@code selectAll()} method from the {@code DAO<Hold>} interface,
     * which delegates the work to the more flexible {@code select(null)} method.
     * </p>
     *
     * @return A list of all Hold objects.
     * @throws DAOException if a database error occurs.
     */
    @Override
    public List<Hold> selectAll() throws DAOException {
        // Delegate to the select method, passing null to signify no filtering
        return select(null);
    }

    /**
     * Internal method to check if the essential fields of a Hold object are valid
     * before an INSERT or UPDATE operation.
     * <p>
     * Ensures all required foreign keys (User ID, Material ID) and the date are present.
     * </p>
     *
     * @param h The Hold object to verify.
     * @throws DAOException if any essential field is missing or invalid.
     */
    private void verifyObject(Hold h) throws DAOException {
        if (h == null || h.getIdUser() == -1 ||
                h.getIdMaterial() == -1 || h.getHold_date() == null) {
            throw new DAOException("In verifyObject: all fields must be non-null or valid");
        }
    }
}