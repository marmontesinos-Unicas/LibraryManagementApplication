package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LoanServiceTest {

    private LoanService service;
    private List<User> users;
    private List<Material> materials;
    private List<Loan> loans;

    @BeforeEach
    public void setUp() {
        users = new ArrayList<>();
        materials = new ArrayList<>();
        loans = new ArrayList<>();

        // Crear un usuario
        User u = new User();
        u.setIdUser(1);
        u.setNationalID("1234A");
        users.add(u);

        // Material disponible
        Material m1 = new Material();
        m1.setIdMaterial(1);
        m1.setMaterial_status("available");
        materials.add(m1);

        // Material no disponible
        Material m2 = new Material();
        m2.setIdMaterial(2);
        m2.setMaterial_status("loaned");
        materials.add(m2);

        // DAOs "inline" para pasar al servicio
        DAO<User> userDao = new DAO<>() {
            @Override
            public void insert(User entity) {}

            @Override
            public void delete(User a) throws DAOException {

            }

            @Override
            public List<User> selectAll() throws DAOException {
                return List.of();
            }

            @Override
            public void update(User entity) {}
            @Override
            public List<User> select(User filter) {
                List<User> result = new ArrayList<>();
                for (User u1 : users) {
                    if (filter == null || filter.getNationalID() == null || filter.getNationalID().equals(u1.getNationalID())) {
                        result.add(u1);
                    }
                }
                return result;
            }
        };

        DAO<Material> materialDao = new DAO<>() {
            @Override
            public void insert(Material entity) {}

            @Override
            public void delete(Material a) throws DAOException {

            }

            @Override
            public List<Material> selectAll() throws DAOException {
                return List.of();
            }

            @Override
            public void update(Material entity) {
                for (int i = 0; i < materials.size(); i++) {
                    if (materials.get(i).getIdMaterial() == entity.getIdMaterial()) {
                        materials.set(i, entity);
                        return;
                    }
                }
            }
            @Override
            public List<Material> select(Material filter) {
                List<Material> result = new ArrayList<>();
                for (Material m : materials) {
                    if (filter == null || filter.getIdMaterial() == 0 || filter.getIdMaterial() == m.getIdMaterial()) {
                        result.add(m);
                    }
                }
                return result;
            }
        };

        DAO<Loan> loanDao = new DAO<>() {
            @Override
            public void insert(Loan entity) {
                entity.setIdLoan(loans.size() + 1);
                loans.add(entity);
            }

            @Override
            public void delete(Loan a) throws DAOException {

            }

            @Override
            public List<Loan> selectAll() throws DAOException {
                return List.of();
            }

            @Override
            public void update(Loan entity) {}
            @Override
            public List<Loan> select(Loan filter) { return new ArrayList<>(loans); }
        };

        service = new LoanService(userDao, materialDao, loanDao);
    }

    @Test
    public void testGetAvailableMaterials() throws DAOException {
        List<Material> available = service.getAvailableMaterials();
        assertEquals(1, available.size());
        assertEquals("available", available.get(0).getMaterial_status());
    }

    @Test
    public void testCreateLoanSuccess() throws DAOException {
        Loan loan = service.createLoan("1234A", 1);

        assertNotNull(loan.getIdLoan());
        assertEquals(1, loan.getIdMaterial());
        assertEquals(1, loan.getIdUser());

        // Material debe actualizarse
        List<Material> available = service.getAvailableMaterials();
        assertEquals(0, available.size()); // ya no hay materiales disponibles
    }

    @Test
    public void testCreateLoanUserNotFound() {
        DAOException ex = assertThrows(DAOException.class, () -> {
            service.createLoan("NOEXISTE", 1);
        });
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    public void testCreateLoanMaterialNotAvailable() {
        DAOException ex = assertThrows(DAOException.class, () -> {
            service.createLoan("1234A", 2);
        });
        assertEquals("Material not available", ex.getMessage());
    }
}
