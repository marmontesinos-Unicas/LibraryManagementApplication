package it.unicas.project.template.address;

import it.unicas.project.template.address.model.Materialmar;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;

import java.util.ArrayList;
import java.util.List;

public class FakeMaterialDAO implements DAO<Materialmar> {

    private final List<Materialmar> storage = new ArrayList<>();
    private int autoIncrement = 1;

    @Override
    public List<Materialmar> select(Materialmar m) throws DAOException {
        return new ArrayList<>(storage);
    }

    @Override
    public void insert(Materialmar m) throws DAOException {
        m.setIdMaterial(autoIncrement++);
        storage.add(m);
    }

    @Override
    public void update(Materialmar m) throws DAOException {
        // Not needed for this test
    }

    @Override
    public void delete(Materialmar m) throws DAOException {
        storage.removeIf(mat -> mat.getIdMaterial().equals(m.getIdMaterial()));
    }
}

