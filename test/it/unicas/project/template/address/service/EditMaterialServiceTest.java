package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.MaterialGenre;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EditMaterialServiceTest {

    static class FakeMaterialDAO implements DAO<Material> {
        boolean updateCalled = false;
        Material lastUpdated;
        @Override public void insert(Material item) throws DAOException { }
        @Override public void update(Material item) throws DAOException { updateCalled = true; lastUpdated = item; }
        @Override public void delete(Material item) throws DAOException { }
        @Override public List<Material> select(Material criteria) throws DAOException { return new ArrayList<>(); }
        @Override public List<Material> selectAll() throws DAOException { return new ArrayList<>(); }
    }

    static class FakeMaterialGenreDAO implements DAO<MaterialGenre> {
        List<MaterialGenre> inserted = new ArrayList<>();
        List<MaterialGenre> deleted = new ArrayList<>();
        List<MaterialGenre> present = new ArrayList<>(); // what select will return

        @Override public void insert(MaterialGenre item) { inserted.add(item); }
        @Override public void update(MaterialGenre item) { }
        @Override public void delete(MaterialGenre item) { deleted.add(item); }
        @Override public List<MaterialGenre> select(MaterialGenre criteria) { return new ArrayList<>(present); }
        @Override public List<MaterialGenre> selectAll() { return new ArrayList<>(present); }
    }

    private FakeMaterialDAO materialDAO;
    private FakeMaterialGenreDAO materialGenreDAO;
    private EditMaterialService service;

    @BeforeEach
    void setUp() {
        materialDAO = new FakeMaterialDAO();
        materialGenreDAO = new FakeMaterialGenreDAO();
        service = new EditMaterialService(materialDAO, materialGenreDAO);
    }

    @Test
    void updateMaterial_valid_updatesAndReplacesGenres() throws DAOException {
        Material m = new Material();
        m.setIdMaterial(10);
        m.setTitle("New title");
        m.setYear(2010);
        m.setIdMaterialType(2);

        // pretend there were two existing entries
        materialGenreDAO.present.add(new MaterialGenre(10, 1));
        materialGenreDAO.present.add(new MaterialGenre(10, 2));

        Set<Integer> newGenres = new HashSet<>();
        newGenres.add(3);
        newGenres.add(4);

        service.updateMaterial(m, newGenres);

        assertTrue(materialDAO.updateCalled);
        assertEquals(m, materialDAO.lastUpdated);

        // existing two should have been deleted
        assertEquals(2, materialGenreDAO.deleted.size());

        // two new inserted
        assertEquals(2, materialGenreDAO.inserted.size());
        assertTrue(materialGenreDAO.inserted.stream().anyMatch(i -> i.getIdGenre() == 3));
        assertTrue(materialGenreDAO.inserted.stream().anyMatch(i -> i.getIdGenre() == 4));
    }

    @Test
    void updateMaterial_invalidYear_throws() {
        Material m = new Material();
        m.setIdMaterial(11);
        m.setTitle("t");
        m.setYear(0);
        m.setIdMaterialType(1);

        assertThrows(IllegalArgumentException.class, () -> service.updateMaterial(m, null));
    }
}

