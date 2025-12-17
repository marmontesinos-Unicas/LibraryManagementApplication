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

/**
 * Unit tests for the LoanService class.
 * This test suite validates loan creation, material availability,
 * user lookup, and error handling using inline DAO stubs.
 */
public class LoanServiceTest {

    private LoanService service;
    private List<User> users;
    private List<Material> materials;
    private List<Loan> loans;

    /**
     * Initializes in-memory test data and inline DAO implementations before each test.
     */
    @BeforeEach
    public void setUp() {
        users = new ArrayList<>();
        materials = new ArrayList<>();
        loans = new ArrayList<>();

        // Create a user
        User u = new User();
        u.setIdUser(1);
        u.setNationalID("1234A");
        users.add(u);

        // Available material
        Material m1 = new Material();
        m1.setIdMaterial(1);
        m1.setMaterial_status("available");
        materials.add(m1);

        // Material that is not available
        Material m2 = new Material();
        m2.setIdMaterial(2);
        m2.setMaterial_status("loaned");
        materials.add(m2);

        // Inline DAOs to pass to the service
        DAO<User> userDao = new DAO<>() {
            @Override
            public void insert(User entity) {}

            @Override
            public void delete(User a) throws DAOException {}

            @Override
            public List<User> selectAll() throws DAOException {
                return List.of();
            }

            @Override
            public void update(User entity) {}

            /**
             * Returns users matching the filter. Simulates a database lookup.
             */
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
            public void delete(Material a) throws DAOException {}

            @Override
            public List<Material> selectAll() throws DAOException {
                return List.of();
            }

            /**
             * Updates a material in the in-memory collection to simulate a database update.
             */
            @Override
            public void update(Material entity) {
                for (int i = 0; i < materials.size(); i++) {
                    if (materials.get(i).getIdMaterial() == entity.getIdMaterial()) {
                        materials.set(i, entity);
                        return;
                    }
                }
            }

            /**
             * Returns materials matching the filter. Simulates a database select.
             */
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
            /**
             * Inserts a loan into the in-memory list and simulates auto-increment ID.
             */
            @Override
            public void insert(Loan entity) {
                entity.setIdLoan(loans.size() + 1);
                loans.add(entity);
            }

            @Override
            public void delete(Loan a) throws DAOException {}

            @Override
            public List<Loan> selectAll() throws DAOException {
                return List.of();
            }

            @Override
            public void update(Loan entity) {}

            /**
             * Returns all loans stored in memory.
             */
            @Override
            public List<Loan> select(Loan filter) {
                return new ArrayList<>(loans);
            }
        };

        service = new LoanService(userDao, materialDao, loanDao);
    }

    /**
     * Tests that only available materials are returned by getAvailableMaterials().
     */
    @Test
    public void testGetAvailableMaterials() throws DAOException {
        List<Material> available = service.getAvailableMaterials();
        assertEquals(1, available.size());
        assertEquals("available", available.get(0).getMaterial_status());
    }

    /**
     * Tests successful loan creation and material status update.
     */
    @Test
    public void testCreateLoanSuccess() throws DAOException {
        Loan loan = service.createLoan("1234A", 1);

        assertNotNull(loan.getIdLoan());
        assertEquals(1, loan.getIdMaterial());
        assertEquals(1, loan.getIdUser());

        // Material should now be updated
        List<Material> available = service.getAvailableMaterials();
        assertEquals(0, available.size()); // no available materials left
    }

    /**
     * Tests that creating a loan with a non-existing user throws an exception.
     */
    @Test
    public void testCreateLoanUserNotFound() {
        DAOException ex = assertThrows(DAOException.class, () -> {
            service.createLoan("NOEXISTE", 1);
        });
        assertEquals("User not found", ex.getMessage());
    }

    /**
     * Tests that creating a loan with a non-available material throws an exception.
     */
    @Test
    public void testCreateLoanMaterialNotAvailable() {
        DAOException ex = assertThrows(DAOException.class, () -> {
            service.createLoan("1234A", 2);
        });
        assertEquals("Material not available", ex.getMessage());
    }
}
