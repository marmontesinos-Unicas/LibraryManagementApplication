package it.unicas.project.template.address.model.dao;

import it.unicas.project.template.address.model.Material;
import java.util.List;

public interface MaterialDAO {
    /**
     * Retrieves all materials from the catalog
     * @return List of all materials
     * @throws DAOException if database error occurs
     */
    List<Material> selectAll() throws DAOException;

    /**
     * Filters materials by search criteria
     * @param title Title to search (can be partial, null to ignore)
     * @param author Author to search (can be partial, null to ignore)
     * @param materialType Type of material (null to ignore)
     * @param status Availability status (null to ignore)
     * @return List of materials matching criteria
     * @throws DAOException if database error occurs
     */
    List<Material> findByCriteria(String title, String author,
                                  Integer materialType, String status) throws DAOException;

    /**
     * Retrieves a single material by ID
     * @param idMaterial Material ID
     * @return Material object or null if not found
     * @throws DAOException if database error occurs
     */
    Material findById(int idMaterial) throws DAOException;

    /**
     * Inserts a new material
     * @param m Material to insert
     * @throws DAOException if database error occurs
     */
    void insert(Material m) throws DAOException;

    /**
     * Updates an existing material
     * @param m Material to update
     * @throws DAOException if database error occurs
     */
    void update(Material m) throws DAOException;

    /**
     * Deletes a material
     * @param m Material to delete
     * @throws DAOException if database error occurs
     */
    void delete(Material m) throws DAOException;
}