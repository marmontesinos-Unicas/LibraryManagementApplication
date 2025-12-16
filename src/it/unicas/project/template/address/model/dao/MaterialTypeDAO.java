package it.unicas.project.template.address.model.dao;

import it.unicas.project.template.address.model.MaterialType;
import java.util.List;

/**
 * The Data Access Object (DAO) Interface for the MaterialType entity.
 * <p>
 * This interface defines the contract for accessing type-related data
 * in the persistence layer. Material Types (e.g., Book, Journal, Video)
 * are static lookup values.
 * </p>
 *
 * Access Keyword Explanation: {@code public} - Allows the service layer to interact with this DAO interface.
 */
public interface MaterialTypeDAO {

    /**
     * Retrieves all {@code MaterialType} records from the persistence layer.
     * <p>
     * This method is used to fetch the complete list of available material
     * types for use in filtering, selection forms, or display.
     * </p>
     *
     * @return A list containing all {@code MaterialType} objects.
     * @throws DAOException if a database error occurs.
     */
    List<MaterialType> selectAll() throws DAOException;
}