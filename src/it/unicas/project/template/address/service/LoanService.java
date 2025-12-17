package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class that handles all business logic related to loan operations.
 * <p>
 * This class acts as the middle layer between controllers and DAOs,
 * ensuring that business rules are applied before interacting with the database.
 * </p>
 */
public class LoanService {

    /** DAO handling User entity persistence */
    private final DAO<User> userDao;

    /** DAO handling Material entity persistence */
    private final DAO<Material> materialDao;

    /** DAO handling Loan entity persistence */
    private final DAO<Loan> loanDao;

    /**
     * Constructs a LoanService with its required DAO dependencies.
     *
     * @param userDao DAO used for retrieving and managing {@link User} data
     * @param materialDao DAO used for retrieving and managing {@link Material} data
     * @param loanDao DAO used for creating and managing {@link Loan} entries
     */
    public LoanService(DAO<User> userDao, DAO<Material> materialDao, DAO<Loan> loanDao) {
        this.userDao = userDao;
        this.materialDao = materialDao;
        this.loanDao = loanDao;
    }

    /**
     * Retrieves all materials that are currently marked as "available".
     * <p>
     * A material is considered available if its {@code material_status}
     * equals the string "available" (case-insensitive).
     * </p>
     *
     * @return a list of all available materials
     * @throws DAOException if the material DAO encounters a database error
     */
    public List<Material> getAvailableMaterials() throws DAOException {
        List<Material> all = materialDao.select(null);

        // Filter out any material that is not explicitly marked as "available"
        all.removeIf(m -> m.getMaterial_status() == null ||
                !"available".equalsIgnoreCase(m.getMaterial_status()));

        return all;
    }

    /**
     * Creates a new loan for a specific material and user.
     * <p>
     * Business rules enforced:
     * <ul>
     *     <li>The user must exist (matched by national ID).</li>
     *     <li>The material must exist and be marked as "available".</li>
     *     <li>The loan start date is set to the current time.</li>
     *     <li>The due date is set to one month from the start date.</li>
     *     <li>The material status is updated to "loaned".</li>
     * </ul>
     * </p>
     *
     * @param nationalID the unique national identifier of the user borrowing the material
     * @param materialId the ID of the material that will be loaned
     * @return the newly created {@link Loan} instance
     * @throws DAOException if the user is not found, the material is unavailable,
     *                      or a database error occurs while saving the loan
     */
    public Loan createLoan(String nationalID, int materialId) throws DAOException {

        // --- 1️ Locate the user by National ID ---
        User userFilter = new User();
        userFilter.setNationalID(nationalID);
        List<User> users = userDao.select(userFilter);

        if (users.isEmpty())
            throw new DAOException("User not found");

        User user = users.get(0);

        // --- 2️ Locate the material and verify availability ---
        Material materialFilter = new Material();
        materialFilter.setIdMaterial(materialId);
        List<Material> materials = materialDao.select(materialFilter);

        if (materials.isEmpty() ||
                !"available".equalsIgnoreCase(materials.get(0).getMaterial_status())) {
            throw new DAOException("Material not available");
        }

        Material material = materials.get(0);

        // --- 3️ Create and configure the new loan ---
        Loan loan = new Loan();
        loan.setIdUser(user.getIdUser());
        loan.setIdMaterial(material.getIdMaterial());
        loan.setStart_date(LocalDateTime.now());
        loan.setDue_date(LocalDateTime.now().plusMonths(1));
        loan.setReturn_date(null);

        loanDao.insert(loan);

        // --- 4️ Update material status to reflect the loan ---
        material.setMaterial_status("loaned");
        materialDao.update(material);

        return loan;
    }
}
