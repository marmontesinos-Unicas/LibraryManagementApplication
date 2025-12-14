package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.OverdueLoan;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.LoanDAOMySQLImpl;
import it.unicas.project.template.address.model.ExpiringHoldInfo;
import it.unicas.project.template.address.model.dao.mysql.HoldDAOMySQLImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Test Class for NotificationsService using Manual Stubbing.
 * Focuses on business logic: formatting and counting notifications (Overdue Loans and Expiring Holds).
 */
public class NotificationsServiceTest {

    private final int TEST_USER_ID = 1;
    private final int OTHER_USER_ID = 2; // User with no notifications
    private final int ONLY_HOLD_USER_ID = 3; // New user for hold-only tests
    private final String EXCEPTION_MESSAGE = "Database connection failed during fetch";

    private NotificationsService notificationsService;

    // --- Test Data for Loan Stub ---
    private final OverdueLoan loan1 = new OverdueLoan(
            101,
            "Moby Dick",
            "Herman Melville",
            LocalDate.of(2025, 11, 15)
    );
    private final OverdueLoan loan2 = new OverdueLoan(
            102,
            "Dune",
            "Frank Herbert",
            LocalDate.of(2025, 12, 1)
    );

    // --- NEW Test Data for Hold Stub ---
    private final ExpiringHoldInfo hold1 = new ExpiringHoldInfo(
            201,
            "The Hitchhiker's Guide to the Galaxy",
            "Douglas Adams",
            LocalDate.now().plusDays(1) // Expires tomorrow
    );
    private final ExpiringHoldInfo hold2 = new ExpiringHoldInfo(
            202,
            "Foundation",
            "Isaac Asimov",
            LocalDate.now().plusDays(2) // Expires in 2 days
    );

    /**
     * Custom Stub for LoanDAOMySQLImpl (Handles Overdue Loans)
     */
    private class TestLoanDAOStub extends LoanDAOMySQLImpl {
        @Override
        public List<OverdueLoan> getOverdueLoansForUser(int userId) throws DAOException {
            if (userId == TEST_USER_ID) {
                // User 1 has overdue loans
                return Arrays.asList(loan1, loan2);
            } else if (userId == OTHER_USER_ID || userId == ONLY_HOLD_USER_ID) {
                // User 2 and 3 have no overdue loans
                return Collections.emptyList();
            } else if (userId == 0) {
                // User 0 triggers a DAO failure
                throw new DAOException(EXCEPTION_MESSAGE);
            }
            return Collections.emptyList();
        }
    }

    /**
     * Custom Stub for HoldDAOMySQLImpl (Handles Expiring Holds)
     */
    private class TestHoldDAOStub extends HoldDAOMySQLImpl {
        // We override the one specific method used by the NotificationsService
        @Override
        public List<ExpiringHoldInfo> getExpiringHoldsForUser(int userId) throws DAOException {
            if (userId == TEST_USER_ID) {
                // User 1 has expiring holds
                return Arrays.asList(hold1);
            } else if (userId == ONLY_HOLD_USER_ID) {
                // User 3 has expiring holds only
                return Arrays.asList(hold1, hold2);
            } else if (userId == OTHER_USER_ID) {
                // User 2 has no expiring holds
                return Collections.emptyList();
            } else if (userId == 0) {
                // User 0 triggers a DAO failure
                throw new DAOException(EXCEPTION_MESSAGE);
            }
            return Collections.emptyList();
        }
    }


    @BeforeEach
    void setUp() {
        // Initialize the service with BOTH stubs
        notificationsService = new NotificationsService(new TestLoanDAOStub(), new TestHoldDAOStub());
    }

    /**
     * Test Case 1: Overdue Loans AND Expiring Holds - Ensures both are combined and formatted correctly.
     */
    @Test
    void getFormattedNotifications_ContainsOverdueAndHolds() throws DAOException {
        // Arrange
        List<String> notifications = notificationsService.getFormattedNotifications(TEST_USER_ID);

        // Assert
        assertNotNull(notifications, "The notification list should not be null.");
        assertEquals(3, notifications.size(), "Should contain 2 overdue loans and 1 expiring hold.");

        // Check for correct formatting for overdue loans
        assertTrue(notifications.get(0).startsWith("OVERDUE: The material 'Moby Dick'"), "First notification should be for Moby Dick.");
        assertTrue(notifications.get(2).startsWith("HOLD EXPIRING: The material 'The Hitchhiker's Guide to the Galaxy'"), "Third notification should be for Hitchhiker's Guide.");
    }

    /**
     * Test Case 2: Expiring Holds ONLY - Ensures only hold notifications are returned.
     */
    @Test
    void getFormattedNotifications_ContainsHoldsOnly() throws DAOException {
        // Arrange
        List<String> notifications = notificationsService.getFormattedNotifications(ONLY_HOLD_USER_ID);

        // Assert
        assertNotNull(notifications, "The notification list should not be null.");
        assertEquals(2, notifications.size(), "Should contain 2 expiring holds only.");

        // Check for correct formatting
        assertTrue(notifications.get(0).startsWith("HOLD EXPIRING:"), "First notification should be a hold.");
        assertTrue(notifications.get(1).startsWith("HOLD EXPIRING:"), "Second notification should be a hold.");

        // Check content (using substring matching for simplicity)
        assertTrue(notifications.get(0).contains("The Hitchhiker's Guide to the Galaxy"), "Notification 1 is correct.");
        assertTrue(notifications.get(1).contains("Foundation"), "Notification 2 is correct.");
    }

    /**
     * Test Case 3 (Existing logic validation): No Notifications - Ensures an empty list is returned.
     */
    @Test
    void getFormattedNotifications_NoNotifications_ReturnsEmptyList() throws DAOException {
        // Act
        List<String> notifications = notificationsService.getFormattedNotifications(OTHER_USER_ID);

        // Assert
        assertNotNull(notifications, "The notification list should not be null.");
        assertTrue(notifications.isEmpty(), "Should return an empty list when no loans or holds are found.");
    }

    /**
     * Test Case 4 (Existing logic validation): DAO Fails - Ensures the exception is thrown through the Service Layer.
     */
    @Test
    void getFormattedNotifications_DAOFails_ThrowsDAOException() {
        // Use a placeholder ID that the stub is configured to treat as a failure
        // Since both DAOs are called, the first one to fail will throw the exception.
        DAOException thrown = assertThrows(DAOException.class, () -> {
            notificationsService.getFormattedNotifications(0);
        }, "Should throw DAOException when the underlying DAO call fails.");

        assertTrue(thrown.getMessage().contains(EXCEPTION_MESSAGE), "The exception message should match the one thrown by the stub.");
    }

    /**
     * Test Case for the hasPendingNotifications helper method used by the controller. (Updated method name and logic)
     */
    @Test
    void hasPendingNotifications_TestCases() {
        // Case A: Has BOTH overdue and holds (should return true)
        assertTrue(notificationsService.hasPendingNotifications(TEST_USER_ID), "Should be true when both loans and holds exist.");

        // Case B: Has ONLY holds (should return true)
        assertTrue(notificationsService.hasPendingNotifications(ONLY_HOLD_USER_ID), "Should be true when only holds exist.");

        // Case C: Has NO pending notifications (should return false)
        assertFalse(notificationsService.hasPendingNotifications(OTHER_USER_ID), "Should be false when no notifications exist.");

        // Case D: DAO fails (should return false for safety)
        assertFalse(notificationsService.hasPendingNotifications(0), "Should be false when DAO fails.");
    }
}