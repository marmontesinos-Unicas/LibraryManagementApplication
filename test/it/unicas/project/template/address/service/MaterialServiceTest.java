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
            // Emulate DB assigning ID if needed
            if (item.getIdMaterial() == null || item.getIdMaterial() == -1 || item.getIdMaterial() == 0) {
                item.setIdMaterial(1);
            }
            lastInserted = item;
        }

        @Override
        public void update(Material item) throws DAOException {
            updateCalled = true;
            lastUpdated = item;
        }

        // The rest are unused in these tests; provide simple no-op implementations
        @Override
        public void delete(Material item) throws DAOException { }

        @Override
        public java.util.List<Material> select(Material criteria) throws DAOException {
            return java.util.Collections.emptyList();
        }

        @Override
        public java.util.List<Material> selectAll() throws DAOException {
            return java.util.Collections.emptyList();
        }
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
        m.setAuthor("Test Author");
        m.setYear(2020);
        m.setIdMaterialType(1);

        Material returned = service.save(m);

        assertNotNull(returned, "service should return a material");
        assertEquals("available", returned.getMaterial_status(), "status should be set to available");
        assertTrue(fakeDao.insertCalled, "insert should have been called on DAO");
        assertNotNull(fakeDao.lastInserted, "DAO should have received a material object");
        assertEquals("Test Title", fakeDao.lastInserted.getTitle());
    }

    @Test
    void save_missingTitle_throwsIllegalArgumentException() {
        Material m = new Material();
        m.setAuthor("Author");
        m.setYear(2000);
        m.setIdMaterialType(1);
        // Title is null by default after Material() constructor

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.save(m));
        assertTrue(ex.getMessage().toLowerCase().contains("title"),
                "Exception message should mention 'title'");
    }

    @Test
    void save_emptyTitle_throwsIllegalArgumentException() {
        Material m = new Material();
        m.setTitle("   ");
        m.setAuthor("Author");
        m.setYear(2000);
        m.setIdMaterialType(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.save(m));
        assertTrue(ex.getMessage().toLowerCase().contains("title"),
                "Exception message should mention 'title'");
    }

    @Test
    void save_zeroYear_throwsIllegalArgumentException() {
        Material m = new Material();
        m.setTitle("Title");
        m.setAuthor("Author");
        m.setYear(0);
        m.setIdMaterialType(1);
        // Year of 0 should be invalid

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.save(m));
        assertTrue(ex.getMessage().toLowerCase().contains("year"),
                "Exception message should mention 'year'");
    }

    @Test
    void save_negativeYear_throwsIllegalArgumentException() {
        Material m = new Material();
        m.setTitle("Title");
        m.setAuthor("Author");
        m.setYear(-5);
        m.setIdMaterialType(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.save(m));
        assertTrue(ex.getMessage().toLowerCase().contains("year"),
                "Exception message should mention 'year'");
    }

    @Test
    void save_validMaterialWithISBN_succeeds() throws DAOException {
        Material m = new Material();
        m.setTitle("Java Guide");
        m.setAuthor("Smith");
        m.setYear(2022);
        m.setISBN("978-3-16-148410-0");
        m.setIdMaterialType(1);

        Material returned = service.save(m);

        assertNotNull(returned);
        assertEquals("978-3-16-148410-0", returned.getISBN());
        assertTrue(fakeDao.insertCalled);
    }

    @Test
    void save_materialsWithDifferentYears_allSucceed() throws DAOException {
        Material m1 = new Material();
        m1.setTitle("Old Book");
        m1.setAuthor("Ancient Author");
        m1.setYear(1800);
        m1.setIdMaterialType(1);

        Material m2 = new Material();
        m2.setTitle("Recent Book");
        m2.setAuthor("Modern Author");
        m2.setYear(2024);
        m2.setIdMaterialType(1);

        Material result1 = service.save(m1);
        Material result2 = service.save(m2);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(1800, result1.getYear());
        assertEquals(2024, result2.getYear());
    }

    @Test
    void save_materialWithoutAuthor_succeeds() throws DAOException {
        // Author might be optional in your domain
        Material m = new Material();
        m.setTitle("Anonymous Work");
        m.setYear(2020);
        m.setIdMaterialType(1);

        Material returned = service.save(m);

        assertNotNull(returned);
        assertEquals("Anonymous Work", returned.getTitle());
        assertTrue(fakeDao.insertCalled);
    }

    @Test
    void save_materialWithAllFields_succeeds() throws DAOException {
        Material m = new Material();
        m.setTitle("Complete Book");
        m.setAuthor("Full Author");
        m.setYear(2023);
        m.setISBN("123-456-789");
        m.setIdMaterialType(2);

        Material returned = service.save(m);

        assertNotNull(returned);
        assertEquals("Complete Book", returned.getTitle());
        assertEquals("Full Author", returned.getAuthor());
        assertEquals(2023, returned.getYear());
        assertEquals("123-456-789", returned.getISBN());
        assertEquals(2, returned.getIdMaterialType());
        assertEquals("available", returned.getMaterial_status());
    }
}