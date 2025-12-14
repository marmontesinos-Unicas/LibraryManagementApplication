package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Hold;
import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.view.HoldRow;
import it.unicas.project.template.address.view.LoanRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UserLandingService}.
 * <p>
 * This test suite validates retrieval and formatting of user loans and holds.
 * It uses in-memory DAOs to simulate database operations.
 * </p>
 */
public class UserLandingServiceTest {

    private UserLandingService service;
    private List<User> users;
    private List<Material> materials;
    private List<Loan> loans;
    private List<Hold> holds;

    /**
     * Initializes sample in-memory data and DAOs before each test.
     * <p>
     * Creates a sample user, sample materials, a loan, and a hold.
     * Sets up inline DAO implementations that simulate filtering by user ID or material ID.
     * </p>
     */
    @BeforeEach
    public void setUp() {
        users = new ArrayList<>();
        materials = new ArrayList<>();
        loans = new ArrayList<>();
        holds = new ArrayList<>();

        // Sample user
        User user = new User();
        user.setIdUser(1);
        user.setNationalID("1234A");
        users.add(user);

        // Sample materials
        Material mat1 = new Material();
        mat1.setIdMaterial(1);
        mat1.setTitle("Java Book");
        materials.add(mat1);

        Material mat2 = new Material();
        mat2.setIdMaterial(2);
        mat2.setTitle("Python Book");
        materials.add(mat2);

        // Sample loan
        Loan loan = new Loan();
        loan.setIdLoan(1);
        loan.setIdUser(1);
        loan.setIdMaterial(1);
        loan.setStart_date(LocalDateTime.now().minusDays(5));
        loan.setDue_date(LocalDateTime.now().plusDays(5));
        loan.setReturn_date(null);
        loans.add(loan);

        // Sample hold
        Hold hold = new Hold();
        hold.setIdHold(1);
        hold.setIdUser(1);
        hold.setIdMaterial(2);
        hold.setHold_date(LocalDateTime.now().plusDays(2));
        holds.add(hold);

        // Inline DAOs with proper filtering by id
        DAO<Loan> loanDao = new DAO<>() {
            @Override
            public void insert(Loan entity) {}
            @Override
            public void delete(Loan a) throws DAOException {}
            @Override
            public List<Loan> selectAll() { return List.of(); }
            @Override
            public void update(Loan entity) {}
            @Override
            public List<Loan> select(Loan filter) {
                List<Loan> result = new ArrayList<>();
                for (Loan l : loans) {
                    if (filter == null || filter.getIdUser() == null || filter.getIdUser().equals(l.getIdUser())) {
                        result.add(l);
                    }
                }
                return result;
            }
        };

        DAO<Hold> holdDao = new DAO<>() {
            @Override
            public void insert(Hold entity) {}
            @Override
            public void delete(Hold a) throws DAOException {}
            @Override
            public List<Hold> selectAll() { return List.of(); }
            @Override
            public void update(Hold entity) {}
            @Override
            public List<Hold> select(Hold filter) {
                List<Hold> result = new ArrayList<>();
                for (Hold h : holds) {
                    if (filter == null || filter.getIdUser() == null || filter.getIdUser().equals(h.getIdUser())) {
                        result.add(h);
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
            public List<Material> selectAll() { return List.of(); }
            @Override
            public void update(Material entity) {}
            @Override
            public List<Material> select(Material filter) {
                List<Material> result = new ArrayList<>();
                for (Material m : materials) {
                    if (filter == null || filter.getIdMaterial() == null || filter.getIdMaterial().equals(m.getIdMaterial())) {
                        result.add(m);
                    }
                }
                return result;
            }
        };

        service = new UserLandingService(loanDao, holdDao, materialDao);
    }

    /**
     * Tests that {@link UserLandingService#getUserLoans(User)} returns all loans
     * for the specified user and formats them correctly as {@link LoanRow}.
     *
     * @throws DAOException if DAO operations fail
     */
    @Test
    public void testGetUserLoans() throws DAOException {
        User user = users.get(0);
        var loanRows = service.getUserLoans(user);

        assertEquals(1, loanRows.size());
        assertEquals("Java Book", loanRows.get(0).titleProperty().get());
        assertEquals("Active", loanRows.get(0).delayedProperty().get());
    }

    /**
     * Tests that {@link UserLandingService#getUserHolds(User)} returns all holds
     * for the specified user and formats them correctly as {@link HoldRow}.
     *
     * @throws DAOException if DAO operations fail
     */
    @Test
    public void testGetUserHolds() throws DAOException {
        User user = users.get(0);
        var holdRows = service.getUserHolds(user);

        assertEquals(1, holdRows.size());
        assertEquals("Python Book", holdRows.get(0).getTitle());
    }
}
