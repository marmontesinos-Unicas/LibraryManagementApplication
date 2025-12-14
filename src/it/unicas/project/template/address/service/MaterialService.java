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
 * Handles operations that involve multiple DAOs or complex logic (like transactions).
 */
public class MaterialService {

    private static final Logger logger = Logger.getLogger(MaterialService.class.getName());

    // We must use the concrete implementation (or a parent interface) and the Genre DAO.
    // Assuming MaterialDAOMySQLImpl implements DAO<Material>
    private final DAO<Material> materialDao;
    private final DAO<MaterialGenre> materialGenreDAO;

    // ADDED: No-arg constructor to support the call: new MaterialService()
    public MaterialService() {
        // Initialize DAOs using their singletons/instances
        this.materialDao = MaterialDAOMySQLImpl.getInstance();
        this.materialGenreDAO = MaterialGenreDAOMySQLImpl.getInstance();
    }

    // Existing constructor (kept for compatibility)
    public MaterialService(DAO<Material> materialDao) {
        this.materialDao = materialDao;
        // Initialize other DAOs needed by the service methods
        this.materialGenreDAO = MaterialGenreDAOMySQLImpl.getInstance();
    }

    /**
     * Inserts a new Material into the database, retrieves its generated ID,
     * and then links all provided genres to it.
     *
     * @param material The Material object to insert. Its ID will be updated by the DAO.
     * @param genreIds A list of IDGenre integers.
     * @throws DAOException If the insertion fails at any step.
     */
    public void insertNewMaterialWithGenres(Material material, List<Integer> genreIds) throws DAOException {

        // 1. Insert the material. The DAO's insert method (as provided by you)
        //    will automatically set the generated ID onto the 'material' object.
        logger.info("Service: Attempting to insert new material.");

        materialDao.insert(material); // This call modifies 'material' by setting its ID.

        Integer newMaterialId = material.getIdMaterial();

        if (newMaterialId == null || newMaterialId <= 0) {
            // This case should be rare if the DAO is correct, but is a safe check.
            throw new DAOException("Failed to retrieve ID for new Material after insertion. Insertion failed or ID was not set by DAO.");
        }

        // 2. Insert the material-genre links using the newly generated ID
        if (genreIds != null && !genreIds.isEmpty()) {
            logger.info("Service: Linking " + genreIds.size() + " genres for material ID: " + newMaterialId);
            for (Integer idGenre : genreIds) {
                // MaterialGenre constructor is (idMaterial, idGenre)
                MaterialGenre mg = new MaterialGenre(newMaterialId, idGenre);
                materialGenreDAO.insert(mg);
            }
        }

        logger.info("Service: Material and genres successfully inserted for ID: " + newMaterialId);
    }

    // Existing `save` method from your original file.
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