package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.time.LocalDateTime;
import java.util.List;

public class LoanService {

    private final DAO<User> userDao;
    private final DAO<Material> materialDao;
    private final DAO<Loan> loanDao;

    public LoanService(DAO<User> userDao, DAO<Material> materialDao, DAO<Loan> loanDao) {
        this.userDao = userDao;
        this.materialDao = materialDao;
        this.loanDao = loanDao;
    }

    /** Buscar materiales disponibles */
    public List<Material> getAvailableMaterials() throws DAOException {
        List<Material> all = materialDao.select(null);
        all.removeIf(m -> m.getMaterial_status() == null || !"available".equalsIgnoreCase(m.getMaterial_status()));
        return all;
    }

    /** Buscar material por título, autor o ISBN */
    public List<Material> searchAvailableMaterials(String query) throws DAOException {
        String q = query == null ? "" : query.trim().toLowerCase();
        List<Material> available = getAvailableMaterials();

        if (q.isEmpty()) return available;

        available.removeIf(m ->
                (m.getTitle() == null || !m.getTitle().toLowerCase().contains(q)) &&
                        (m.getAuthor() == null || !m.getAuthor().toLowerCase().contains(q)) &&
                        (m.getISBN() == null || !m.getISBN().toLowerCase().contains(q))
        );

        return available;
    }

    /** Crear un préstamo */
    public Loan createLoan(String nationalID, int materialId) throws DAOException {
        // 1️⃣ Buscar usuario
        User userFilter = new User();
        userFilter.setNationalID(nationalID);
        List<User> users = userDao.select(userFilter);
        if (users.isEmpty()) throw new DAOException("User not found");

        User user = users.get(0);

        // 2️⃣ Buscar material
        Material materialFilter = new Material();
        materialFilter.setIdMaterial(materialId);
        List<Material> materials = materialDao.select(materialFilter);
        if (materials.isEmpty() || !"available".equalsIgnoreCase(materials.get(0).getMaterial_status())) {
            throw new DAOException("Material not available");
        }

        Material material = materials.get(0);

        // 3️⃣ Crear Loan
        Loan loan = new Loan();
        loan.setIdUser(user.getIdUser());
        loan.setIdMaterial(material.getIdMaterial());
        loan.setStart_date(LocalDateTime.now());
        loan.setDue_date(LocalDateTime.now().plusMonths(1));
        loan.setReturn_date(null);

        loanDao.insert(loan);

        // 4️⃣ Actualizar material
        material.setMaterial_status("loaned");
        materialDao.update(material);

        return loan;
    }
}
