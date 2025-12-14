package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.view.LoadReturnController.LoanRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ReturnService class.
 * This test suite validates the retrieval of active and delayed loans,
 * searching loans by title or user, returning a loan, and error handling.
 */
public class ReturnServiceTest {

    private ReturnService service;
    private List<User> users;
    private List<Material> materials;
    private List<Loan> loans;

    /**
     * Initializes test data before each test.
     * Creates sample users, materials, and loans in memory.
     */
    @BeforeEach
    public void setUp() {
        users = new ArrayList<>();
        materials = new ArrayList<>();
        loans = new ArrayList<>();

        // Users
        User u1 = new User(); u1.setIdUser(1); u1.setName("Alice"); u1.setSurname("Smith"); users.add(u1);
        User u2 = new User(); u2.setIdUser(2); u2.setName("Bob"); u2.setSurname("Jones"); users.add(u2);

        // Materials
        Material m1 = new Material();
        m1.setIdMaterial(1);
        m1.setTitle("Java Book");
        m1.setMaterial_status("loaned");
        m1.setIdMaterialType(1);
        materials.add(m1);

        Material m2 = new Material();
        m2.setIdMaterial(2);
        m2.setTitle("Python Book");
        m2.setMaterial_status("available");
        m2.setIdMaterialType(1);
        materials.add(m2);

        // Loans
        Loan l1 = new Loan();
        l1.setIdLoan(1);
        l1.setIdUser(1);
        l1.setIdMaterial(1);
        l1.setStart_date(LocalDateTime.now().minusDays(10));
        l1.setDue_date(LocalDateTime.now().minusDays(1));
        l1.setReturn_date(null);
        loans.add(l1);

        Loan l2 = new Loan();
        l2.setIdLoan(2);
        l2.setIdUser(2);
        l2.setIdMaterial(2);
        l2.setStart_date(LocalDateTime.now().minusDays(5));
        l2.setDue_date(LocalDateTime.now().plusDays(5));
        l2.setReturn_date(null);
        loans.add(l2);

        // Initialize the service with the in-memory lists
        service = new ReturnService(users, materials, loans);
    }

    /**
     * Tests that getActiveLoans() returns all loans that have not been returned.
     */
    @Test
    public void testGetActiveLoans() {
        List<LoanRow> active = service.getActiveLoans();
        assertEquals(2, active.size());
    }

    /**
     * Tests that getDelayedLoans() returns only loans past their due date.
     */
    @Test
    public void testGetDelayedLoans() {
        List<LoanRow> delayed = service.getDelayedLoans();
        assertEquals(1, delayed.size());
        assertEquals("Alice Smith", delayed.get(0).getUser());
    }

    /**
     * Tests searching loans by user name (case-insensitive).
     */
    @Test
    public void testSearchLoansByUser() {
        List<LoanRow> result = service.searchLoans("bob");
        assertEquals(1, result.size());
        assertEquals("Bob Jones", result.get(0).getUser());
    }

    /**
     * Tests searching loans by material title (case-insensitive).
     */
    @Test
    public void testSearchLoansByTitle() {
        List<LoanRow> result = service.searchLoans("java");
        assertEquals(1, result.size());
        assertEquals("Java Book", result.get(0).getTitle());
    }

    /**
     * Tests returning a loan.
     * Ensures that the material is marked available and the loan's return date is set.
     * @throws DAOException if the loan cannot be found
     */
    @Test
    public void testReturnLoan() throws DAOException {
        LoanRow toReturn = service.getActiveLoans().get(0); // Alice Smith, Java Book
        service.returnLoan(toReturn);

        // Check that the material is now available
        Material mat = materials.stream().filter(m -> m.getIdMaterial() == 1).findFirst().orElse(null);
        assertNotNull(mat);
        assertEquals("available", mat.getMaterial_status());

        // Check that the loan is marked as returned
        Loan loan = loans.stream().filter(l -> l.getIdLoan() == 1).findFirst().orElse(null);
        assertNotNull(loan);
        assertNotNull(loan.getReturn_date());
    }

    /**
     * Tests returning a loan that does not exist.
     * Ensures DAOException is thrown with correct message.
     */
    @Test
    public void testReturnLoanNotFound() {
        LoanRow fake = new LoanRow(999, "Book", "Nonexistent", "Nobody", "2000-01-01", "No");
        DAOException ex = assertThrows(DAOException.class, () -> service.returnLoan(fake));
        assertEquals("Loan not found", ex.getMessage());
    }
}
