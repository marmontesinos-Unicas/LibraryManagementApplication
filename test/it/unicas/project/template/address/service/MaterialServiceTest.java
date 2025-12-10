package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaterialServiceTest {

    // Simple in-memory DAO used only for tests
    static class FakeMaterialDAO implements DAO<Material> {
        Material lastInserted;
        Material lastUpdated;
        boolean insertCalled = false;
        boolean updateCalled = false;

        @Override
        public void insert(Material item) throws DAOException {
            insertCalled = true;
            // emulate DB assigning ID if needed
            if (item.getIdMaterial() == null) {
                // if your Material has a setter for id, use it; example below assumes setIdMaterial exists
                try {
                    item.getClass().getMethod("setIdMaterial", Integer.class).invoke(item, 1);
                } catch (Exception ignored) {}
            }
            lastInserted = item;
        }

        @Override
        public void update(Material item) throws DAOException {
            updateCalled = true;
            lastUpdated = item;
        }

        // The rest are unused in these tests; provide simple no-op implementations
        @Override public void delete(Material item) throws DAOException { }
        @Override public java.util.List<Material> select(Material criteria) throws DAOException { return java.util.Collections.emptyList(); }
        @Override
        public java.util.List<Material> selectAll() throws DAOException { return java.util.Collections.emptyList(); }
    }

    private FakeMaterialDAO fakeDao;
    private MaterialService service;

    @BeforeEach
    void setUp() {
        fakeDao = new FakeMaterialDAO();
        service = new MaterialService(fakeDao);
    }

    @Test
    void save_validMaterial_setsStatusAndCallsInsert() throws DAOException {
        Material m = new Material();
        m.setTitle("Test Title");
        m.setYear(2020);
        m.setIdMaterialType(1);

        Material returned = service.save(m);

        assertSame(m, returned, "service should return the same instance");
        assertEquals("available", m.getMaterial_status(), "status should be set to available");
        assertTrue(fakeDao.insertCalled, "insert should have been called on DAO");
        assertSame(fakeDao.lastInserted, m, "DAO should have received the same material object");
    }

    @Test
    void save_missingTitle_throwsIllegalArgumentException() {
        Material m = new Material();
        m.setTitle(null);
        m.setYear(2000);
        m.setIdMaterialType(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.save(m));
        assertTrue(ex.getMessage().contains("Title"));
    }

    @Test
    void save_emptyTitle_throwsIllegalArgumentException() {
        Material m = new Material();
        m.setTitle("   ");
        m.setYear(2000);
        m.setIdMaterialType(1);

        assertThrows(IllegalArgumentException.class, () -> service.save(m));
    }

    @Test
    void save_nullYear_throwsIllegalArgumentException() {
        Material m = new Material();
        m.setTitle("Title");
        m.setYear(null);
        m.setIdMaterialType(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(m));
        assertTrue(ex.getMessage().toLowerCase().contains("year"));
    }

    @Test
    void save_nonPositiveYear_throwsIllegalArgumentException() {
        Material m = new Material();
        m.setTitle("Title");
        m.setYear(0);
        m.setIdMaterialType(1);

        assertThrows(IllegalArgumentException.class, () -> service.save(m));
    }

    @Test
    void save_nullMaterialType_throwsIllegalArgumentException() {
        Material m = new Material();
        m.setTitle("Title");
        m.setYear(2001);
        m.setIdMaterialType(null);

        assertThrows(IllegalArgumentException.class, () -> service.save(m));
    }
}
