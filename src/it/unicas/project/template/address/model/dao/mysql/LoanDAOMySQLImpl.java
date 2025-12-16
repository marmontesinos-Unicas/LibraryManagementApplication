package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import it.unicas.project.template.address.model.OverdueLoan;
import java.time.LocalDate;

/**
 * MySQL implementation for the Loan data access object (DAO).
 * <p>
 * This class handles all CRUD operations and specific queries (like counting active loans
 * and finding overdue loans) related to the 'loans' table in the database.
 * </p>
 *
 * Access Keyword Explanation: {@code public} - Allows the service layer to interact with this DAO.
 */
public class LoanDAOMySQLImpl implements DAO<Loan> {

    // --- Singleton Fields ---
    private static DAO<Loan> dao = null; // The single instance of this DAO (Singleton pattern)
    private static Logger logger = null; // Logger for error reporting
    // Formatter used to convert LocalDateTime objects into the string format required by MySQL
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // --- Complex SQL Query ---
    /**
     * SQL to fetch overdue loans for a specific user.
     * <p>
     * It joins the 'loans' table with the 'materials' table to retrieve the item's title and author.
     * The criteria ensure the loan's due date is past the current time (NOW()) and the return date is NULL.
     * </p>
     *
     * Access Keyword Explanation: {@code private static final} - Private to this class,
     * static because it's shared by all methods, and final because the SQL string should not change.
     */
    private static final String SQL_SELECT_OVERDUE_FOR_USER =
            "SELECT " +
                    "l.idLoan, " +
                    "l.due_date, " +
                    "m.title, " +
                    "m.author " +
                    "FROM " +
                    "loans l " +
                    "JOIN " +
                    "materials m ON l.idMaterial = m.idMaterial " + // Joining to get material details
                    "WHERE " +
                    "l.idUser = ? " +              // Filter by current user
                    "AND l.due_date < NOW() " +    // Checks if the due date is before the current time
                    "AND l.return_date IS NULL";   // Ensures the loan is still active

    /**
     * Protected constructor to enforce the Singleton pattern.
     * <p>
     * Prevents direct instantiation but allows for potential subclassing within the same package.
     * </p>
     *
     * Access Keyword Explanation: {@code protected} - Restricts external creation but allows package/subclass access.
     */
    protected LoanDAOMySQLImpl() {}

    /**
     * Provides the global access point to the single instance of the Loan DAO.
     * <p>
     * Implements lazy initialization of the Singleton.
     * </p>
     *
     * Access Keyword Explanation: {@code public static} - Provides global, class-level access.
     *
     * @return The single instance of the LoanDAOMySQLImpl, cast to the generic DAO interface.
     */
    public static DAO<Loan> getInstance() {
        // Initialization happens only on the first call
        if (dao == null) {
            dao = new LoanDAOMySQLImpl();
            logger = Logger.getLogger(LoanDAOMySQLImpl.class.getName());
        }
        return dao;
    }

    /**
     * Selects Loan records based on criteria specified in the input Loan object (l).
     * <p>
     * Implements a dynamic query builder: only fields in the input object (l) that are not
     * default (-1 or null) are used to generate the WHERE clause.
     * </p>
     *
     * @param l A Loan object used as a search template.
     * @return A list of matching Loan objects.
     * @throws DAOException if a database error occurs.
     */
    @Override
    public List<Loan> select(Loan l) throws DAOException {
        List<Loan> list = new ArrayList<>();
        if (l == null) l = new Loan(null, null, null, null, null, null);

        // Start of dynamic query. 'WHERE 1=1' is a common pattern to easily chain AND clauses.
        String sql = "SELECT * FROM loans WHERE 1=1";
        if (l.getIdLoan() != -1) sql += " AND idLoan=?";
        if (l.getIdUser() != -1) sql += " AND idUser=?";
        if (l.getIdMaterial() != -1) sql += " AND idMaterial=?";
        if (l.getStart_date() != null) sql += " AND start_date LIKE ?";

        // Use try-with-resources to ensure database resources are closed
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;
            // Dynamically bind parameters to the SQL placeholders (?)
            if (l.getIdLoan() != -1) ps.setInt(index++, l.getIdLoan());
            if (l.getIdUser() != -1) ps.setInt(index++, l.getIdUser());
            if (l.getIdMaterial() != -1) ps.setInt(index++, l.getIdMaterial());
            if (l.getStart_date() != null) ps.setString(index++, l.getStart_date().format(FORMATTER) + "%");

            logger.info("SQL: " + ps);

            // Execute query and process results
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Handle potential NULL values from the database for foreign keys gracefully
                    Integer idUser = (Integer) rs.getObject("idUser");
                    Integer idMaterial = (Integer) rs.getObject("idMaterial");

                    // Map database columns to a new Loan object
                    Loan loan = new Loan(
                            rs.getInt("idLoan"),
                            idUser != null ? idUser : -1,
                            idMaterial != null ? idMaterial : -1,
                            rs.getTimestamp("start_date") != null ? rs.getTimestamp("start_date").toLocalDateTime() : null,
                            rs.getTimestamp("due_date") != null ? rs.getTimestamp("due_date").toLocalDateTime() : null,
                            rs.getTimestamp("return_date") != null ? rs.getTimestamp("return_date").toLocalDateTime() : null
                    );
                    list.add(loan);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("In select(): " + e.getMessage());
        }

        return list;
    }

    /**
     * Inserts a new Loan record into the database and retrieves the auto-generated ID.
     * <p>
     * Calls {@code verifyObject} for basic validation before execution. Handles the optional {@code return_date}
     * by inserting NULL if it is not present.
     * </p>
     *
     * @param l The Loan object to insert. The ID field will be updated upon successful insertion.
     * @throws DAOException if validation fails or a database error occurs.
     */
    @Override
    public void insert(Loan l) throws DAOException {
        verifyObject(l);
        String sql = "INSERT INTO loans (idUser, idMaterial, start_date, due_date, return_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, l.getIdUser());
            ps.setInt(2, l.getIdMaterial());
            ps.setTimestamp(3, Timestamp.valueOf(l.getStart_date()));  // Changed
            ps.setTimestamp(4, Timestamp.valueOf(l.getDue_date()));    // Changed
            if (l.getReturn_date() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(l.getReturn_date())); // Changed
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            logger.info("SQL: " + ps);
            ps.executeUpdate();

            // Retrieve the auto-generated primary key (idLoan)
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) l.setIdLoan(rs.getInt(1)); // Update the original Loan object with the new ID
            }
        } catch (SQLException e) {
            throw new DAOException("In insert(): " + e.getMessage());
        }
    }

    /**
     * Updates an existing Loan record in the database.
     * <p>
     * Calls {@code verifyObject} for basic validation before execution. Handles the optional {@code return_date}
     * by setting it to NULL in the database if the object's value is NULL.
     * </p>
     *
     * @param l The Loan object with updated data (must include a valid idLoan).
     * @throws DAOException if validation fails or a database error occurs.
     */
    @Override
    public void update(Loan l) throws DAOException {
        verifyObject(l);
        String sql = "UPDATE loans SET idUser=?, idMaterial=?, start_date=?, due_date=?, return_date=? WHERE idLoan=?";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, l.getIdUser());
            ps.setInt(2, l.getIdMaterial());
            ps.setTimestamp(3, Timestamp.valueOf(l.getStart_date()));
            ps.setTimestamp(4, Timestamp.valueOf(l.getDue_date()));

            // Conditional handling for the optional return_date field
            if (l.getReturn_date() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(l.getReturn_date()));
            } else {
                ps.setNull(5, Types.TIMESTAMP); // Set SQL column to NULL
            }
            ps.setInt(6, l.getIdLoan()); // Use ID for the WHERE clause

            logger.info("SQL: " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In update(): " + e.getMessage());
        }
    }

    /**
     * Deletes a Loan record from the database based on its ID.
     * <p>
     * Ensures the input object is not null and has a valid ID before proceeding.
     * </p>
     *
     * @param l The Loan object containing the ID of the record to delete.
     * @throws DAOException if the ID is missing or a database error occurs.
     */
    @Override
    public void delete(Loan l) throws DAOException {
        // Validate that a record ID is present for deletion
        if (l == null || l.getIdLoan() == -1) {
            throw new DAOException("In delete: idLoan cannot be null");
        }

        String sql = "DELETE FROM loans WHERE idLoan=?";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, l.getIdLoan());
            logger.info("SQL: " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In delete(): " + e.getMessage());
        }
    }

    /**
     * Selects all Loan records from the database.
     * <p>
     * This implementation currently returns an empty list, suggesting it might not be fully implemented
     * or is intentionally disabled to enforce use of the more flexible {@code select(Loan l)} method.
     * </p>
     *
     * @return An empty list.
     * @throws DAOException if a database error occurs.
     */
    @Override
    public List<Loan> selectAll() throws DAOException {
        return List.of();
    }

    /**
     * Internal method to check if the essential fields of a Loan object are valid
     * before an INSERT or UPDATE operation.
     * <p>
     * Ensures all required fields (User ID, Material ID, Start Date, Due Date) are present.
     * </p>
     *
     * @param l The Loan object to verify.
     * @throws DAOException if any essential field is missing or invalid.
     */
    private void verifyObject(Loan l) throws DAOException {
        if (l == null || l.getIdUser() == -1 || l.getIdMaterial() == -1 ||
                l.getStart_date() == null || l.getDue_date() == null) {
            throw new DAOException("In verifyObject: required fields must be non-null or valid");
        }
    }

    /**
     * Counts the number of loans associated with a user that have not yet been returned.
     * <p>
     * An active loan is defined as one where the return_date column is NULL in the database.
     * </p>
     *
     * @param userId The ID of the user.
     * @return The count of active loans.
     * @throws DAOException if a database error occurs.
     */
    public int countActiveLoansByUserId(int userId) throws DAOException {
        String sql = "SELECT COUNT(*) FROM loans WHERE idUser = ? AND return_date IS NULL";
        int count = 0;

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            logger.info("SQL: " + ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1); // COUNT(*) returns a single column, usually column 1
                }
            }
        } catch (SQLException e) {
            throw new DAOException("In countActiveLoansByUserId(): " + e.getMessage());
        }
        return count;
    }

    /**
     * Retrieves a list of all materials that are currently overdue for a specific user.
     * <p>
     * Executes the complex join query {@code SQL_SELECT_OVERDUE_FOR_USER} and maps the results
     * into {@code OverdueLoan} objects for the service layer.
     * </p>
     *
     * @param userId The ID of the currently logged-in user.
     * @return A List of OverdueLoan objects, or an empty list if none are found.
     * @throws DAOException if a database error occurs.
     */
    public List<OverdueLoan> getOverdueLoansForUser(int userId) throws DAOException {
        List<OverdueLoan> overdueItems = new ArrayList<>();

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_OVERDUE_FOR_USER)) {

            ps.setInt(1, userId);

            logger.info("SQL: " + ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Map the ResultSet fields to the OverdueLoan model
                    int loanId = rs.getInt("idLoan");
                    String title = rs.getString("title");
                    String author = rs.getString("author");

                    // Convert java.sql.Timestamp to java.time.LocalDate for clean display
                    LocalDate dueDate = rs.getTimestamp("due_date")
                            .toLocalDateTime()
                            .toLocalDate();

                    OverdueLoan item = new OverdueLoan(loanId, title, author, dueDate); // <-- Object creation changed
                    overdueItems.add(item);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("In getOverdueLoansForUser(): " + e.getMessage());
        }

        return overdueItems;
    }
}

