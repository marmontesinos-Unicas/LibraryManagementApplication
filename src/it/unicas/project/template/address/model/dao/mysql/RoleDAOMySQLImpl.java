package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation for the Role data access object (DAO).
 * <p>
 * This class specifically handles read operations for the {@code ROLES} lookup table.
 * It provides access to the different user roles (e.g., administrator, standard user)
 * stored in the database.
 * </p>
 *
 * Access Keyword Explanation: {@code public} - Allows the service layer to access this DAO.
 */
public class RoleDAOMySQLImpl {

    // --- Singleton Field ---
    private static RoleDAOMySQLImpl instance; // The single instance of this class (Singleton pattern)

    /**
     * Private constructor to enforce the Singleton pattern.
     * <p>
     * Prevents external classes from using 'new RoleDAOMySQLImpl()'.
     * </p>
     *
     * Access Keyword Explanation: {@code private} - Essential for the Singleton pattern.
     */
    private RoleDAOMySQLImpl() {}

    /**
     * Provides the global access point to the single instance of the Role DAO.
     * <p>
     * Implements lazy initialization of the Singleton.
     * </p>
     *
     * Access Keyword Explanation: {@code public static} - Provides global, class-level access.
     *
     * @return The single RoleDAOMySQLImpl instance.
     */
    public static RoleDAOMySQLImpl getInstance() {
        if (instance == null) {
            // Initialization happens only on the first call
            instance = new RoleDAOMySQLImpl();
        }
        return instance;
    }

    /**
     * Retrieves all Role records from the database.
     * <p>
     * This method fetches all entries from the lookup table.
     * </p>
     *
     * @return A list containing all {@code Role} objects, or an empty list if none are found or an error occurs.
     */
    public List<Role> selectAll() {
        List<Role> roles = new ArrayList<>();
        // SQL query to fetch all roles
        String sql = "SELECT * FROM ROLES";

        try (PreparedStatement ps = DAOMySQLSettings.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Map the current row's data to a new Role object
                roles.add(new Role(
                        rs.getInt("idRole"),
                        rs.getString("admin_type")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error in selectAll(): " + e.getMessage());
        }

        return roles;
    }
}