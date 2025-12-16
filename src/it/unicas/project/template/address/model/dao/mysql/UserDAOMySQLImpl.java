package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * MySQL implementation for the User data access object (DAO).
 * <p>
 * This class handles all CRUD operations and specific queries (like searching by username)
 * related to the {@code users} table in the database.
 * </p>
 *
 * Access Keyword Explanation: {@code public} - Allows the service layer to interact with this DAO.
 */
public class UserDAOMySQLImpl {

    // --- Singleton Fields ---
    private static UserDAOMySQLImpl instance = null; // The single instance of this class (Singleton pattern)
    // Logger for error reporting and SQL logging
    private static Logger logger = Logger.getLogger(UserDAOMySQLImpl.class.getName());

    /**
     * Protected constructor to enforce the Singleton pattern.
     * <p>
     * Prevents direct instantiation but allows for potential subclassing within the same package.
     * </p>
     *
     * Access Keyword Explanation: {@code protected} - Restricts external creation but allows package/subclass access.
     */
    protected UserDAOMySQLImpl() {}

    /**
     * Provides the global access point to the single instance of the User DAO.
     * <p>
     * Implements lazy initialization of the Singleton.
     * </p>
     *
     * Access Keyword Explanation: {@code public static} - Provides global, class-level access.
     *
     * @return The single UserDAOMySQLImpl instance.
     */
    public static UserDAOMySQLImpl getInstance() {
        if (instance == null) {
            // Initialization happens only on the first call
            instance = new UserDAOMySQLImpl();
        }
        return instance;
    }

    /**
     * Retrieves a single {@code User} record from the database using their unique username.
     * <p>
     * This is commonly used for user authentication/login purposes.
     * </p>
     *
     * @param username The unique username to search for.
     * @return The matching {@code User} object, or {@code null} if no user is found.
     * @throws DAOException if a database error occurs.
     */
    public User getByUsername(String username) throws DAOException {
        String sql = "SELECT * FROM users WHERE username = ?";

        // Use try-with-resources to ensure Connection, PreparedStatement, and ResultSet are closed.
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            logger.info("SQL: " + ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Map database columns to a new User object
                    return new User(
                            rs.getInt("idUser"),
                            rs.getString("name"),
                            rs.getString("surname"),
                            rs.getString("username"),
                            rs.getString("nationalID"),
                            // Convert java.sql.Date to modern Java LocalDate object
                            rs.getDate("birthdate") != null ? rs.getDate("birthdate").toLocalDate() : null,
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getInt("idRole")
                    );
                } else {
                    return null; // No user found with the given username
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error retrieving user by username: " + e.getMessage());
        }
    }
    /**
     * Selects users based on the non-null/non-default fields in the filter User object.
     * This method supports filtering by National ID AND Role ID for the uniqueness check.
     * It is made robust against a null idRole property in the filter object.
     *
     * @param u The User object containing filter criteria. If null, returns all users.
     * @return A list of users matching the criteria.
     * @throws DAOException if a database error occurs.
     */
    public List<User> select(User u) throws DAOException {
        List<User> list = new ArrayList<>();
        // If null is passed, create a default User object to ensure the dynamic query builder works
        if (u == null) u = new User(null, "", "", "", "", null, "", "", -1);

        // Start of dynamic query. 'WHERE 1=1' is a common pattern to easily chain AND clauses.
        String sql = "SELECT * FROM users WHERE 1=1";
        if (u.getIdUser() != -1) sql += " AND idUser=?";
        // Using LIKE for partial matching on name, surname, username, and email
        if (u.getName() != null && !u.getName().isEmpty()) sql += " AND name LIKE ?";
        if (u.getSurname() != null && !u.getSurname().isEmpty()) sql += " AND surname LIKE ?";
        if (u.getUsername() != null && !u.getUsername().isEmpty()) sql += " AND username LIKE ?";
        // Using exact match (=) for National ID as it is assumed to be a unique identifier
        if (u.getNationalID() != null && !u.getNationalID().isEmpty()) sql += " AND nationalID = ?";
        if (u.getEmail() != null && !u.getEmail().isEmpty()) sql += " AND email LIKE ?";
        if (u.getIdRole() != null && u.getIdRole() != -1) sql += " AND idRole = ?";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Dynamically bind parameters to the SQL placeholders (?)
            int index = 1;
            if (u.getIdUser() != -1) ps.setInt(index++, u.getIdUser());
            // Bind LIKE patterns (prefix matching for name, surname, username, substring matching for email)
            if (u.getName() != null && !u.getName().isEmpty()) ps.setString(index++, u.getName() + "%");
            if (u.getSurname() != null && !u.getSurname().isEmpty()) ps.setString(index++, u.getSurname() + "%");
            if (u.getUsername() != null && !u.getUsername().isEmpty()) ps.setString(index++, u.getUsername() + "%");
            if (u.getNationalID() != null && !u.getNationalID().isEmpty()) ps.setString(index++, u.getNationalID());
            if (u.getEmail() != null && !u.getEmail().isEmpty()) ps.setString(index++, "%" + u.getEmail() + "%");
            if (u.getIdRole() != null && u.getIdRole() != -1) ps.setInt(index++, u.getIdRole());

            logger.info("SQL: " + ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Map results to new User objects
                    User user = new User(
                            rs.getInt("idUser"),
                            rs.getString("name"),
                            rs.getString("surname"),
                            rs.getString("username"),
                            rs.getString("nationalID"),
                            rs.getDate("birthdate") != null ? rs.getDate("birthdate").toLocalDate() : null,
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getInt("idRole")
                    );
                    list.add(user);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("In select(): " + e.getMessage());
        }
        return list;
    }

    /**
     * Inserts a new {@code User} record into the database and retrieves the auto-generated ID.
     * <p>
     * Handles the optional {@code birthdate} field by inserting NULL if it is not present.
     * </p>
     *
     * @param u The User object to insert. The ID field will be updated upon successful insertion.
     * @throws DAOException if the user object is null or a database error occurs.
     */
    public void insert(User u) throws DAOException {
        if (u == null) throw new DAOException("User cannot be null");

        String sql = "INSERT INTO users (name, surname, username, nationalID, birthdate, password, email, idRole) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DAOMySQLSettings.getConnection();
             // Instruct PreparedStatement to return the auto-generated key (idUser)
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getName());
            ps.setString(2, u.getSurname());
            ps.setString(3, u.getUsername());
            ps.setString(4, u.getNationalID());
            // Conditional handling for the optional birthdate field
            if (u.getBirthdate() != null) ps.setDate(5, Date.valueOf(u.getBirthdate()));
            else ps.setNull(5, Types.DATE);
            ps.setString(6, u.getPassword());
            ps.setString(7, u.getEmail());
            ps.setInt(8, u.getIdRole());
            ps.executeUpdate(); // Execute the INSERT statement

            // Retrieve the auto-generated primary key (idUser)
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setIdUser(rs.getInt(1)); // Update the original User object with the new ID
            }

        } catch (SQLException e) {
            throw new DAOException("In insert(): " + e.getMessage());
        }
    }

    /**
     * Updates an existing {@code User} record in the database.
     * <p>
     * This method updates all descriptive fields and ensures the optional {@code birthdate}
     * is handled correctly (setting to NULL if needed).
     * </p>
     *
     * @param u The User object with updated data (must include a valid idUser).
     * @throws DAOException if the user object is null or a database error occurs.
     */
    public void update(User u) throws DAOException {
        if (u == null) throw new DAOException("User cannot be null");

        String sql = "UPDATE users SET name=?, surname=?, username=?, nationalID=?, birthdate=?, password=?, email=?, idRole=? WHERE idUser=?";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Bind all fields for the SET clause
            ps.setString(1, u.getName());
            ps.setString(2, u.getSurname());
            ps.setString(3, u.getUsername());
            ps.setString(4, u.getNationalID());
            // Conditional handling for the birthdate field
            if (u.getBirthdate() != null) ps.setDate(5, Date.valueOf(u.getBirthdate()));
            else ps.setNull(5, Types.DATE);
            ps.setString(6, u.getPassword());
            ps.setString(7, u.getEmail());
            ps.setInt(8, u.getIdRole());
            ps.setInt(9, u.getIdUser()); // ID for the WHERE clause
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("In update(): " + e.getMessage());
        }
    }

    /**
     * Deletes a single {@code User} record from the database based on its ID.
     * <p>
     * Ensures the user object and its ID are valid before executing the deletion.
     * </p>
     *
     * @param u The User object containing the ID of the record to delete.
     * @throws DAOException if the ID is missing or a database error occurs (e.g., foreign key violation).
     */
    public void delete(User u) throws DAOException {
        if (u == null || u.getIdUser() == -1) throw new DAOException("idUser cannot be null");

        String sql = "DELETE FROM users WHERE idUser=?";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, u.getIdUser());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("In delete(): " + e.getMessage());
        }
    }
}