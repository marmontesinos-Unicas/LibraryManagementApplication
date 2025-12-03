package it.unicas.project.template.address.service;

import it.unicas.project.template.address.FakeMaterialDAO;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.dao.DAOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MaterialServiceTest {

    private MaterialService service;

    @BeforeEach
    public void setUp() {
        // This is where we create the MaterialService with a fake DAO
        service = new MaterialService(new FakeMaterialDAO());
    }

    @Test
    public void save_validMaterial_setsStatusAndAssignsId() throws DAOException {
        Material m = new Material();
        m.setTitle("Test Title");
        m.setAuthor("Author");
        m.setYear(2023);
        m.setISBN("ISBN-001");
        m.setIdMaterialType(1);

        Material saved = service.save(m);

        assertEquals("available", saved.getMaterial_status());
        assertNotNull(saved.getIdMaterial());
        assertTrue(saved.getIdMaterial() > 0);
    }

    @Test
    public void save_missingTitle_throwsIllegalArgument() {
        Material m = new Material();
        m.setAuthor("A");
        m.setYear(2023);
        m.setISBN("I");
        m.setIdMaterialType(1);

        assertThrows(IllegalArgumentException.class, () -> service.save(m));
    }

    @Test
    public void save_illegalyear_throwsIllegalArgument() {
        Material m = new Material();
        m.setAuthor("A");
        m.setYear(-2023);
        m.setISBN("I");
        m.setIdMaterialType(1);

        assertThrows(IllegalArgumentException.class, () -> service.save(m));
    }
}
