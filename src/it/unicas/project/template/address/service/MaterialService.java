package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.MaterialGenre;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialGenreDAOMySQLImpl;

import java.util.List;
import java.util.logging.Logger;

/**
 * Service layer for Material-related business logic.
 * <p>
 * Handles operations that involve multiple DAOs (e.g., Material and MaterialGenre)
 * or complex logic (like cross-DAO transactions and business validation).
 * </p>
 *
 * Access Keyword Explanation: {@code public} - Accessible by controllers or other service components.
 */
public class MaterialService {

    private static final Logger logger = Logger.getLogger(MaterialService.class.getName());

    // --- Dependencies ---
    // Use the DAO interface type, initialized with MySQL implementations.
    private final DAO<Material> materialDao;
    private final DAO<MaterialGenre> materialGenreDAO;

    /**
     * Constructor for Dependency Injection (mostly for testing).
     *
     * @param materialDao The {@code Material} DAO implementation to use.
     */
    public MaterialService(DAO<Material> materialDao) {
        this.materialDao = materialDao;
        // Initialize other DAOs needed by the service methods
        this.materialGenreDAO = MaterialGenreDAOMySQLImpl.getInstance();
    }

    /**
     * Performs business validation and then inserts a new Material into the database.
     * <p>
     * This method is a simpler insertion focusing only on the Material entity itself
     * and applying default status.
     * </p>
     *
     * @param m The Material object to validate and insert.
     * @return The inserted Material object (with its generated ID).
     * @throws IllegalArgumentException if any business rule validation fails (Title, Year, Type).
     * @throws DAOException if the database insertion fails.
     */
    public Material save(Material m) throws DAOException {
        if (m.getTitle() == null || m.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (m.getYear() == null) {
            throw new IllegalArgumentException("Year is required");
        }
        if (m.getYear() <= 0) {
            throw new IllegalArgumentException("Year must be a positive number");
        }
        if (m.getIdMaterialType() == null) {
            throw new IllegalArgumentException("Material type is required");
        }

        // Assign default status
        m.setMaterial_status("available");

        // Save to DAO
        materialDao.insert(m);

        return m;
    }
}