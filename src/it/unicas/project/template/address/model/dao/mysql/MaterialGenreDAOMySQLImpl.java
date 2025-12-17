package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.MaterialGenre;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * MySQL implementation for the MaterialGenre data access object (DAO).
 * <p>
 * This DAO manages the join table {@code materials_genres}, which links {@code Material}
 * records to {@code Genre} records (a many-to-many relationship).
 * Since the primary key is composite (idMaterial, idGenre), the update operation is not supported.
 * </p>
 *
 * Access Keyword Explanation: {@code public} - Allows the service layer to interact with this DAO.
 */
public class MaterialGenreDAOMySQLImpl implements DAO<MaterialGenre> {

    // --- Singleton Fields ---
    private static DAO<MaterialGenre> dao = null; // The single instance of this DAO (Singleton pattern)
    private static Logger logger = null; // Logger for error reporting

    /**
     * Private constructor to enforce the Singleton pattern.
     * <p>
     * Prevents external classes from using 'new MaterialGenreDAOMySQLImpl()'.
     * </p>
     *
     * Access Keyword Explanation: {@code private} - Essential for the Singleton pattern.
     */
    private MaterialGenreDAOMySQLImpl() {}

    /**
     * Provides the global access point to the single instance of the MaterialGenre DAO.
     * <p>
     * Implements lazy initialization of the Singleton.
     * </p>
     *
     * Access Keyword Explanation: {@code public static} - Provides global, class-level access.
     *
     * @return The single instance of the MaterialGenreDAOMySQLImpl, cast to the generic DAO interface.
     */
    public static DAO<MaterialGenre> getInstance() {
        if (dao == null) {
            // Initialization happens only on the first call
            dao = new MaterialGenreDAOMySQLImpl();
            logger = Logger.getLogger(MaterialGenreDAOMySQLImpl.class.getName());
        }
        return dao;
    }

    /**
     * Selects {@code MaterialGenre} association records based on criteria specified in the input object (mg).
     * <p>
     * Allows filtering by material ID, genre ID, or both.
     * </p>
     *
     * @param mg A MaterialGenre object used as a search template.
     * @return A list of matching {@code MaterialGenre} association objects.
     * @throws DAOException if a database error occurs.
     */
    @Override
    public List<MaterialGenre> select(MaterialGenre mg) throws DAOException {
        List<MaterialGenre> list = new ArrayList<>();
        // Start of dynamic query. 'WHERE 1=1' is a common pattern to easily chain AND clauses.
        String sql = "SELECT * FROM materials_genres WHERE 1=1";

        // Dynamically append AND clauses based on non-default fields
        if (mg != null) {
            if (mg.getIdMaterial() != -1) sql += " AND idMaterial=?";
            if (mg.getIdGenre() != -1) sql += " AND idGenre=?";
        }

        // Use try-with-resources to ensure database resources are closed
        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Bind parameters dynamically
            int index = 1;
            if (mg != null) {
                if (mg.getIdMaterial() != -1) ps.setInt(index++, mg.getIdMaterial());
                if (mg.getIdGenre() != -1) ps.setInt(index++, mg.getIdGenre());
            }

            logger.info("SQL: " + ps);

            // Execute query and process results
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Map results to new MaterialGenre association objects
                    list.add(new MaterialGenre(
                            rs.getInt("idMaterial"),
                            rs.getInt("idGenre")
                    ));
                }
            }
        } catch (SQLException e) {
            // Wrap the technical SQL error into a business-friendly DAOException and rethrow
            throw new DAOException("In select(): " + e.getMessage());
        }

        return list;
    }

    /**
     * Inserts a new Material-Genre association record into the join table.
     * <p>
     * This creates a link between one material and one genre.
     * </p>
     *
     * @param mg The MaterialGenre object containing the IDs to link.
     * @throws DAOException if validation fails or a database error occurs (e.g., foreign key violation).
     */
    @Override
    public void insert(MaterialGenre mg) throws DAOException {
        verifyObject(mg);
        String sql = "INSERT INTO materials_genres (idMaterial, idGenre) VALUES (?, ?)";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Bind the composite primary key fields
            ps.setInt(1, mg.getIdMaterial());
            ps.setInt(2, mg.getIdGenre());

            logger.info("SQL: " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In insert(): " + e.getMessage());
        }
    }

    /**
     * Updates are not supported for the MaterialGenre association table.
     * <p>
     * Since the primary key is composite (idMaterial, idGenre) and the record contains no other fields,
     * an update would be functionally equivalent to a delete followed by an insert, which is handled
     * directly by the service layer.
     * </p>
     *
     * @param mg The MaterialGenre object.
     * @throws DAOException Always throws an exception to indicate the operation is unsupported.
     */
    @Override
    public void update(MaterialGenre mg) throws DAOException {
        // No se puede hacer update porque la PK es compuesta
        throw new DAOException("Update no soportado para MaterialGenre (PK compuesta)");
    }

    /**
     * Deletes a single Material-Genre association record from the join table.
     * <p>
     * Requires both the material ID and the genre ID to uniquely identify the record.
     * </p>
     *
     * @param mg The MaterialGenre object containing the IDs to delete.
     * @throws DAOException if the required IDs are missing or a database error occurs.
     */
    @Override
    public void delete(MaterialGenre mg) throws DAOException {
        if (mg == null || mg.getIdMaterial() == -1 || mg.getIdGenre() == -1) {
            throw new DAOException("In delete: idMaterial y idGenre no pueden ser nulos");
        }

        String sql = "DELETE FROM materials_genres WHERE idMaterial=? AND idGenre=?";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Bind the composite primary key fields
            ps.setInt(1, mg.getIdMaterial());
            ps.setInt(2, mg.getIdGenre());

            logger.info("SQL: " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In delete(): " + e.getMessage());
        }
    }

    /**
     * Deletes all genre associations for a given material ID.
     * <p>
     * This method is typically used to clear out all existing genres linked to a material
     * before saving a new list of genres (a common pattern for updating many-to-many relationships).
     * </p>
     *
     * @param materialId The ID of the representative material.
     * @throws DAOException If a database error occurs.
     */
    public void deleteAllByMaterialId(Integer materialId) throws DAOException {
        if (materialId == null || materialId == -1) {
            throw new DAOException("In deleteAllByMaterialId: idMaterial cannot be null");
        }

        String sql = "DELETE FROM materials_genres WHERE idMaterial=?";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, materialId);
            logger.info("SQL (Delete Genres): " + ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("In deleteAllByMaterialId(): " + e.getMessage());
        }
    }

    /**
     * Selects all MaterialGenre records.
     * <p>
     * This implementation currently returns an empty list, suggesting it might be intentionally
     * disabled or not fully implemented, as retrieving all join records is rarely needed.
     * </p>
     *
     * @return An empty list.
     * @throws DAOException if a database error occurs.
     */
    @Override
    public List<MaterialGenre> selectAll() throws DAOException {
        return List.of();
    }

    /**
     * Internal method to check if the essential fields of a MaterialGenre object are valid
     * before an INSERT or DELETE operation.
     * <p>
     * Ensures both the material ID and genre ID are present.
     * </p>
     *
     * @param mg The MaterialGenre object to verify.
     * @throws DAOException if any essential field is missing or invalid.
     */
    private void verifyObject(MaterialGenre mg) throws DAOException {
        if (mg == null || mg.getIdMaterial() == -1 || mg.getIdGenre() == -1) {
            throw new DAOException("In verifyObject: idMaterial y idGenre deben ser válidos");
        }
    }
}