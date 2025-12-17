package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.MaterialType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation for the MaterialType data access object (DAO).
 * <p>
 * This class specifically handles read operations for the {@code MATERIAL_TYPE} lookup table.
 * Since this is generally a static lookup table, it only provides a method to select all types.
 * </p>
 *
 * Access Keyword Explanation: {@code public} - Allows the service layer to access this DAO.
 */
public class MaterialTypeDAOMySQLImpl {

    // --- Singleton Field ---
    private static MaterialTypeDAOMySQLImpl instance; // The single instance of this class (Singleton pattern)

    /**
     * Private constructor to enforce the Singleton pattern.
     * <p>
     * Prevents external classes from using 'new MaterialTypeDAOMySQLImpl()'.
     * </p>
     *
     * Access Keyword Explanation: {@code private} - Essential for the Singleton pattern.
     */
    private MaterialTypeDAOMySQLImpl() {}

    /**
     * Provides the global access point to the single instance of the MaterialType DAO.
     * <p>
     * Implements lazy initialization of the Singleton.
     * </p>
     *
     * Access Keyword Explanation: {@code public static} - Provides global, class-level access.
     *
     * @return The single MaterialTypeDAOMySQLImpl instance.
     */
    public static MaterialTypeDAOMySQLImpl getInstance() {
        if (instance == null) {
            // Initialization happens only on the first call
            instance = new MaterialTypeDAOMySQLImpl();
        }
        return instance;
    }

    /**
     * Retrieves all Material Type records from the database.
     * <p>
     * This method fetches all entries from the lookup table, ordered alphabetically.
     * </p>
     *
     * @return A list containing all {@code MaterialType} objects, or an empty list if none are found or an error occurs.
     */
    public List<MaterialType> selectAll() {
        List<MaterialType> list = new ArrayList<>();
        // SQL query to fetch all material types, ordered alphabetically for presentation
        String sql = "SELECT * FROM MATERIAL_TYPE ORDER BY material_type";

        // Use try-with-resources to ensure Connection, PreparedStatement, and ResultSet are closed automatically.
        try (Connection conn = DAOMySQLSettings.getConnection(); // Get new database connection from utility
             PreparedStatement ps = conn.prepareStatement(sql);    // Prepare the static SQL statement
             ResultSet rs = ps.executeQuery()) {                   // Execute the query and get results

            // Iterate through the result set row by row
            while (rs.next()) {
                // Map the current row's data to a new MaterialType object
                list.add(new MaterialType(
                        rs.getInt("idMaterialType"),
                        rs.getString("material_type")
                ));
            }

        } catch (SQLException e) {
            // Print error to standard error stream (no dedicated logger used here)
            System.err.println("Error in selectAll(): " + e.getMessage());
        }

        return list;
    }
}