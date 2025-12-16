package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.MaterialInventory;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * MySQL implementation for the Material data access object (DAO).
 * <p>
 * This class handles CRUD operations for individual {@code Material} records, as well as complex
 * queries needed to aggregate materials into a simplified inventory view (MaterialInventory).
 * It implements the generic {@code DAO<Material>} interface.
 * </p>
 *
 * Access Keyword Explanation: {@code public} - Allows the service layer to interact with this DAO.
 */
public class MaterialDAOMySQLImpl implements DAO<Material> {

    // --- Singleton Fields ---
    private static DAO<Material> dao = null; // The single instance of this DAO (Singleton pattern)
    private static Logger logger = null; // Logger for error reporting

    /**
     * Private constructor to enforce the Singleton pattern.
     * <p>
     * Prevents external classes from using 'new MaterialDAOMySQLImpl()'.
     * </p>
     *
     * Access Keyword Explanation: {@code private} - Essential for the Singleton pattern.
     */
    private MaterialDAOMySQLImpl() {}

    /**
     * Provides the global access point to the single instance of the Material DAO.
     * <p>
     * Implements lazy initialization of the Singleton.
     * </p>
     *
     * Access Keyword Explanation: {@code public static} - Provides global, class-level access.
     *
     * @return The single instance of the MaterialDAOMySQLImpl, cast to the generic DAO interface.
     */
    public static DAO<Material> getInstance() {
        if (dao == null) {
            // Initialization happens only on the first call
            dao = new MaterialDAOMySQLImpl();
            logger = Logger.getLogger(MaterialDAOMySQLImpl.class.getName());
        }
        return dao;
    }

    /**
     * Helper method to map a {@code ResultSet} row, containing aggregated inventory data,
     * into a specialized {@code MaterialInventory} object.
     * <p>
     * This method assumes the ResultSet comes from a JOIN/GROUP BY query that includes
     * both basic material fields and COUNT aggregates.
     * </p>
     *
     * @param rs The ResultSet containing the aggregated material and inventory counts.
     * @return A fully populated MaterialInventory object.
     * @throws SQLException If a column access error occurs.
     */
    private MaterialInventory createMaterialInventoryFromResultSet(ResultSet rs) throws SQLException {
        Material baseMaterial = new Material(
                rs.getInt("idMaterial"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getInt("year"),
                rs.getString("ISBN"),
                rs.getInt("idMaterialType"),
                rs.getString("material_status_summary")
        );

        MaterialInventory mi = new MaterialInventory(
                baseMaterial,
                rs.getInt("quantity_count")
        );

        // Set aggregated fields for inventory status
        mi.setMaterialTypeName(rs.getString("material_type_name"));
        mi.setAvailableCount(rs.getInt("available_count"));
        mi.setOnHoldCount(rs.getInt("on_hold_count"));
        mi.setLoanedCount(rs.getInt("loaned_count"));

        return mi;
    }

    // --- Centralized SQL for Inventory View ---
    /**
     * Base SQL query used to create the inventory view.
     * <p>
     * It uses aggregate functions (MIN, COUNT, SUM(CASE)) and joins {@code material_type}
     * to calculate the total quantity and status counts for a group of materials.
     * </p>
     */
    private static final String INVENTORY_SELECT_BASE_SQL =
            "SELECT " +
                    "    MIN(m.idMaterial) AS idMaterial, " + // Use MIN ID as representative ID for the group
                    "    m.title, " +
                    "    m.author, " +
                    "    m.year, " +
                    "    m.ISBN, " +
                    "    m.idMaterialType, " +
                    "    mt.material_type AS material_type_name, " +
                    "    GROUP_CONCAT(DISTINCT m.material_status SEPARATOR ', ') AS material_status_summary, " + // Summarize unique statuses
                    "    COUNT(*) AS quantity_count, " + // Total number of copies
                    "    SUM(CASE WHEN m.material_status = 'available' THEN 1 ELSE 0 END) AS available_count, " + // Count of available copies
                    "    SUM(CASE WHEN m.material_status = 'holded' THEN 1 ELSE 0 END) AS on_hold_count, " +     // Count of copies on hold
                    "    SUM(CASE WHEN m.material_status = 'loaned' THEN 1 ELSE 0 END) AS loaned_count " +       // Count of loaned copies
                    "FROM materials m " +
                    "JOIN material_type mt ON m.idMaterialType = mt.idMaterialType ";

    /**
     * GROUP BY and ORDER BY clause for the inventory view.
     * <p>
     * Materials are grouped by their common descriptive attributes (title, author, etc.)
     * to aggregate the inventory counts.
     * </p>
     */
    private static final String INVENTORY_GROUP_BY_SQL =
            "GROUP BY " +
                    "    m.title, " +
                    "    m.author, " +
                    "    m.year, " +
                    "    m.ISBN, " +
                    "    m.idMaterialType, " +
                    "    mt.material_type " +
                    "ORDER BY m.title";

    /**
     * Retrieves all material inventory records (groups of materials).
     * <p>
     * Executes the full inventory query without filtering.
     * </p>
     *
     * @return A list of {@code MaterialInventory} objects representing the entire catalog.
     * @throws DAOException if a database error occurs.
     */
    public List<MaterialInventory> selectAllInventory() throws DAOException {
        String sql = INVENTORY_SELECT_BASE_SQL + INVENTORY_GROUP_BY_SQL;
        List<MaterialInventory> list = new ArrayList<>();

        // Use try-with-resources to ensure Connection, PreparedStatement, and ResultSet are closed
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(createMaterialInventoryFromResultSet(rs)); // Map results using helper
            }
        } catch (SQLException e) {
            logger.severe("SQL Error in selectAllInventory: " + e.getMessage());
            throw new DAOException("In selectAllInventory(): " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves material inventory records (groups) matching a specific search term.
     * <p>
     * If the search term is empty, it delegates to {@code selectAllInventory()}.
     * The search pattern is applied to title, author, and ISBN fields.
     * </p>
     *
     * @param searchTerm The term to search for (e.g., "Potter").
     * @return A list of matching {@code MaterialInventory} objects.
     * @throws DAOException if a database error occurs.
     */
    public List<MaterialInventory> selectInventoryBySearchTerm(String searchTerm) throws DAOException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return selectAllInventory();
        }

        // Combine base SQL with WHERE clause and final grouping/ordering
        String sql = INVENTORY_SELECT_BASE_SQL +
                "WHERE m.title LIKE ? OR m.author LIKE ? OR m.ISBN LIKE ? " +
                INVENTORY_GROUP_BY_SQL;

        List<MaterialInventory> list = new ArrayList<>();

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            // Bind search pattern to all three placeholders
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            logger.info("SQL: " + ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(createMaterialInventoryFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("SQL Error in selectInventoryBySearchTerm: " + e.getMessage());
            throw new DAOException("In selectInventoryBySearchTerm(): " + e.getMessage());
        }
        return list;
    }

    /**
     * Selects individual {@code Material} records based on criteria specified in the input object (m).
     * <p>
     * This is a dynamic query builder that allows filtering by ID, title, author, ISBN, type, and status.
     * It performs a LEFT JOIN with {@code material_type} to retrieve the type name for display or mapping.
     * </p>
     *
     * @param m A Material object used as a search template.
     * @return A list of matching individual {@code Material} objects.
     * @throws DAOException if a database error occurs.
     */
    @Override
    public List<Material> select(Material m) throws DAOException {
        List<Material> list = new ArrayList<>();
        if (m == null) m = new Material("", "", null, "", null, "");

        String sql = "SELECT m.*, mt.material_type " +
                "FROM materials m " +
                "LEFT JOIN material_type mt ON m.idMaterialType = mt.idMaterialType " +
                "WHERE 1=1";

        // Dynamically append AND clauses based on non-default fields in the criteria object (m)
        if (m.getIdMaterial() != -1)
            sql += " AND m.idMaterial=?";
        if (m.getTitle() != null && !m.getTitle().isEmpty())
            sql += " AND m.title LIKE ?";
        if (m.getAuthor() != null && !m.getAuthor().isEmpty())
            sql += " AND m.author LIKE ?";
        if (m.getISBN() != null && !m.getISBN().isEmpty())
            sql += " AND m.ISBN LIKE ?";
        if (m.getIdMaterialType() != null && m.getIdMaterialType() != 0)
            sql += " AND m.idMaterialType=?";
        if (m.getMaterial_status() != null && !m.getMaterial_status().isEmpty())
            sql += " AND m.material_status=?";

        sql += " ORDER BY m.title";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Bind parameters dynamically
            int index = 1;
            if (m.getIdMaterial() != -1)
                ps.setInt(index++, m.getIdMaterial());
            if (m.getTitle() != null && !m.getTitle().isEmpty())
                ps.setString(index++, "%" + m.getTitle() + "%");
            if (m.getAuthor() != null && !m.getAuthor().isEmpty())
                ps.setString(index++, "%" + m.getAuthor() + "%");
            if (m.getISBN() != null && !m.getISBN().isEmpty())
                ps.setString(index++, "%" + m.getISBN() + "%");
            if (m.getIdMaterialType() != null && m.getIdMaterialType() != 0)
                ps.setInt(index++, m.getIdMaterialType());
            if (m.getMaterial_status() != null && !m.getMaterial_status().isEmpty())
                ps.setString(index++, m.getMaterial_status());

            logger.info("SQL: " + ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Map results to individual Material objects
                    Material mat = new Material(
                            rs.getInt("idMaterial"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("year"),
                            rs.getString("ISBN"),
                            rs.getInt("idMaterialType"),
                            rs.getString("material_status")
                    );
                    list.add(mat);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("In select(): " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all individual {@code Material} records.
     * <p>
     * Delegates the request to the flexible {@code select(Material m)} method by passing {@code null}.
     * </p>
     *
     * @return A list of all individual {@code Material} objects.
     * @throws DAOException if a database error occurs.
     */
    public List<Material> selectAll() throws DAOException {
        return select(null);
    }

    /**
     * Selects individual {@code Material} records based on a generic search term.
     * <p>
     * Filters materials where the search term matches the title, author, or ISBN.
     * </p>
     *
     * @param searchTerm The string to search for.
     * @return A list of matching individual {@code Material} objects.
     * @throws DAOException if a database error occurs.
     */
    public List<Material> selectBySearchTerm(String searchTerm) throws DAOException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return selectAll();
        }

        // SQL to search across primary text fields (title, author, ISBN)
        String sql = "SELECT m.*, mt.material_type " +
                "FROM materials m " +
                "LEFT JOIN material_type mt ON m.idMaterialType = mt.idMaterialType " +
                "WHERE m.title LIKE ? OR m.author LIKE ? OR m.ISBN LIKE ? " +
                "ORDER BY m.title ASC";

        List<Material> list = new ArrayList<>();

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            // Bind search pattern to all three placeholders
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            logger.info("SQL: " + ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Material mat = new Material(
                            rs.getInt("idMaterial"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("year"),
                            rs.getString("ISBN"),
                            rs.getInt("idMaterialType"),
                            rs.getString("material_status")
                    );
                    list.add(mat);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("In selectBySearchTerm(): " + e.getMessage());
        }

        return list;
    }

    /**
     * Finds materials by availability status.
     * <p>
     * Uses the flexible {@code select(Material m)} method by setting only the status field
     * in the criteria object.
     * </p>
     *
     * @param status The material status (e.g., "available", "loaned").
     * @return A list of materials matching the status.
     * @throws DAOException if a database error occurs.
     */
    public List<Material> selectByStatus(String status) throws DAOException {
        Material criteria = new Material();
        criteria.setMaterial_status(status);
        return select(criteria);
    }

    /**
     * Finds materials by type.
     * <p>
     * Uses the flexible {@code select(Material m)} method by setting only the material type ID
     * in the criteria object.
     * </p>
     *
     * @param idMaterialType The ID of the material type (e.g., 1 for Book).
     * @return A list of materials matching the type.
     * @throws DAOException if a database error occurs.
     */
    public List<Material> selectByType(Integer idMaterialType) throws DAOException {
        Material criteria = new Material();
        criteria.setIdMaterialType(idMaterialType);
        return select(criteria);
    }

    /**
     * Inserts a new individual {@code Material} record into the database and retrieves the auto-generated ID.
     * <p>
     * Calls {@code verifyObject} for basic validation before execution.
     * </p>
     *
     * @param m The Material object to insert. The ID field will be updated upon successful insertion.
     * @throws DAOException if validation fails or a database error occurs.
     */
    @Override
    public void insert(Material m) throws DAOException {
        verifyObject(m);
        String sql = "INSERT INTO materials (title, author, year, ISBN, idMaterialType, material_status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DAOMySQLSettings.getConnection();
             // Instruct PreparedStatement to return the auto-generated key (idMaterial)
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Bind all fields to the placeholders
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getAuthor());
            ps.setInt(3, m.getYear());
            ps.setString(4, m.getISBN());
            ps.setInt(5, m.getIdMaterialType());
            ps.setString(6, m.getMaterial_status());

            logger.info("SQL: " + ps);
            ps.executeUpdate();

            // Retrieve the auto-generated primary key (idMaterial)
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    m.setIdMaterial(rs.getInt(1)); // Update the original Material object with the new ID
            }

        } catch (SQLException e) {
            throw new DAOException("In insert(): " + e.getMessage());
        }
    }

    /**
     * Updates descriptive fields (title, author, year, ISBN) for an entire group of materials.
     * <p>
     * This is used when a librarian updates the general details of a book, and all copies (materials)
     * belonging to that book group must be updated simultaneously. It uses a different WHERE clause
     * depending on the material type (ISBN for books, title/author/year for others).
     * </p>
     *
     * @param updatedMaterial The Material object containing the new, desired group values.
     * @param originalGroup The MaterialInventory object representing the material group before the update, used for the WHERE clause.
     * @throws DAOException if the ISBN is missing for a book group update or a database error occurs.
     */
    public void updateMaterialGroup(Material updatedMaterial, MaterialInventory originalGroup) throws DAOException {
        // Keys used to identify the group in the database before the update
        String originalGroupingKeyTitle = originalGroup.getTitle();
        String originalGroupingKeyAuthor = originalGroup.getAuthor();
        Integer originalGroupingKeyYear = originalGroup.getYear();
        String originalGroupingKeyISBN = originalGroup.getISBN();
        Integer materialType = originalGroup.getIdMaterialType();

        String whereClause = "";

        // Determine the WHERE clause based on the material type (assuming type 1 is 'Book' and uses ISBN)
        if (materialType == 1) {
            if (originalGroupingKeyISBN == null || originalGroupingKeyISBN.isEmpty()) {
                throw new DAOException("Cannot update book group: Original ISBN is missing.");
            }
            whereClause = "WHERE idMaterialType = ? AND ISBN = ?";
        } else {
            // Use title, author, and year for non-book materials
            whereClause = "WHERE idMaterialType = ? AND title = ? AND author = ? AND year = ?";
        }

        // Construct the final SQL query
        String sql = "UPDATE materials SET title=?, author=?, year=?, ISBN=? " + whereClause;

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Bind the NEW values first (SET clause)
            int paramIndex = 1;
            ps.setString(paramIndex++, updatedMaterial.getTitle());
            ps.setString(paramIndex++, updatedMaterial.getAuthor());
            ps.setInt(paramIndex++, updatedMaterial.getYear());
            ps.setString(paramIndex++, updatedMaterial.getISBN());

            // Bind the ORIGINAL values for the WHERE clause
            ps.setInt(paramIndex++, materialType);

            if (materialType == 1) {
                ps.setString(paramIndex++, originalGroupingKeyISBN);
            } else {
                ps.setString(paramIndex++, originalGroupingKeyTitle);
                ps.setString(paramIndex++, originalGroupingKeyAuthor);
                ps.setInt(paramIndex++, originalGroupingKeyYear);
            }

            logger.info("SQL (MaterialGroup Update): " + ps);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("In updateMaterialGroup(): " + e.getMessage());
        }
    }

    /**
     * Updates an existing individual {@code Material} record in the database.
     * <p>
     * This update is usually used to change the status of a single copy (e.g., from 'available' to 'loaned')
     * or to correct a specific material's individual properties.
     * </p>
     *
     * @param m The Material object with updated data (must include a valid idMaterial).
     * @throws DAOException if validation fails or a database error occurs.
     */
    @Override
    public void update(Material m) throws DAOException {
        verifyObject(m);
        String sql = "UPDATE materials SET title=?, author=?, year=?, ISBN=?, idMaterialType=?, material_status=? WHERE idMaterial=?";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Bind all fields for the SET clause
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getAuthor());
            ps.setInt(3, m.getYear());
            ps.setString(4, m.getISBN());
            ps.setInt(5, m.getIdMaterialType());
            ps.setString(6, m.getMaterial_status());
            ps.setInt(7, m.getIdMaterial()); // ID for the WHERE clause

            logger.info("SQL: " + ps);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("In update(): " + e.getMessage());
        }
    }

    /**
     * Deletes a single {@code Material} record from the database based on its ID.
     * <p>
     * This effectively removes one copy of the item from the inventory.
     * </p>
     *
     * @param m The Material object containing the ID of the record to delete.
     * @throws DAOException if the ID is missing or a database error occurs (e.g., foreign key violation).
     */
    @Override
    public void delete(Material m) throws DAOException {
        if (m == null || m.getIdMaterial() == -1) {
            throw new DAOException("In delete: idMaterial cannot be null");
        }

        String sql = "DELETE FROM materials WHERE idMaterial=?";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, m.getIdMaterial());
            logger.info("SQL: " + ps);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("In delete(): " + e.getMessage());
        }
    }

    /**
     * Internal method to check if the essential fields of a Material object are valid
     * before an INSERT or UPDATE operation.
     * <p>
     * Ensures all required fields (Title, Type ID, Status) are present.
     * </p>
     *
     * @param m The Material object to verify.
     * @throws DAOException if any essential field is missing or invalid.
     */
    private void verifyObject(Material m) throws DAOException {
        if (m == null || m.getTitle() == null || m.getTitle().isEmpty()
                || m.getIdMaterialType() == null
                || m.getMaterial_status() == null || m.getMaterial_status().isEmpty())  {
            throw new DAOException("In verifyObject: all fields must be non-null");
        }
    }
}