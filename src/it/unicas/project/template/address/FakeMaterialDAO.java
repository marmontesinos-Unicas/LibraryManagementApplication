package it.unicas.project.template.address;

import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.util.ArrayList;
import java.util.List;

public class FakeMaterialDAO implements DAO<Material> {

    private final List<Material> storage = new ArrayList<>();
    private int autoIncrement = 1;

    @Override
    public List<Material> select(Material m) throws DAOException {
        return new ArrayList<>(storage);
    }

    @Override
    public void insert(Material m) throws DAOException {
        m.setIdMaterial(autoIncrement++);
        storage.add(m);
    }

    @Override
    public void update(Material m) throws DAOException {
        // Not needed for this test
    }

    @Override
    public void delete(Material m) throws DAOException {
        storage.removeIf(mat -> mat.getIdMaterial().equals(m.getIdMaterial()));
    }
}
