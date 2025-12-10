package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Genre;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.MaterialGenre;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.util.List;
import java.util.Set;

public class EditMaterialService {

    private final DAO<Material> materialDao;
    private final DAO<MaterialGenre> materialGenreDao;

    public EditMaterialService(DAO<Material> materialDao, DAO<MaterialGenre> materialGenreDao) {
        this.materialDao = materialDao;
        this.materialGenreDao = materialGenreDao;
    }

    /**
     * Update material and replace its genre associations with given genreIds.
     * Throws IllegalArgumentException for validation issues, DAOException for DB errors.
     */
    public void updateMaterial(Material m, Set<Integer> genreIds) throws DAOException {
        if (m == null) throw new IllegalArgumentException("Material is required");
        if (m.getTitle() == null || m.getTitle().trim().isEmpty()) throw new IllegalArgumentException("Title is required");
        if (m.getYear() <= 0) throw new IllegalArgumentException("Year must be positive");
        if (m.getIdMaterialType() == null) throw new IllegalArgumentException("Material type required");

        // Update the material row
        materialDao.update(m);

        // Delete existing associations (select by material id)
        MaterialGenre deleteCriteria = new MaterialGenre(m.getIdMaterial(), -1);
        List<MaterialGenre> existing = materialGenreDao.select(deleteCriteria);
        for (MaterialGenre mg : existing) {
            materialGenreDao.delete(mg);
        }

        // Insert new associations
        if (genreIds != null) {
            for (Integer gid : genreIds) {
                MaterialGenre mg = new MaterialGenre(m.getIdMaterial(), gid);
                materialGenreDao.insert(mg);
            }
        }
    }
}
