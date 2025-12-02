package it.unicas.project.template.address;
import it.unicas.project.template.address.model.Materialmar;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

public class MaterialService {
    private final DAO<Materialmar> materialDao;

    public MaterialService(DAO<Materialmar> materialDao) {
        this.materialDao = materialDao;
    }

    public Materialmar save(Materialmar m) throws DAOException {
        if (m.getTitle() == null || m.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (m.getAuthor() == null || m.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("Author is required");
        }
        if (m.getYear() == null) {
            throw new IllegalArgumentException("Year is required");
        }
        if (m.getISBN() == null || m.getISBN().trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN is required");
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
