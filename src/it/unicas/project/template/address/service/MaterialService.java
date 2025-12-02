package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

public class MaterialService {

    private final DAO<Material> materialDao;

    public MaterialService(DAO<Material> materialDao) {
        this.materialDao = materialDao;
    }

    public Material save(Material m) throws DAOException {
        if (m.getTitle() == null || m.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (m.getYear() == null) {
            throw new IllegalArgumentException("Year is required");
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

