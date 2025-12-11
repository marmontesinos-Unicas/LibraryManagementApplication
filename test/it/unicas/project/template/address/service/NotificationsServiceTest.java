package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.OverdueLoan;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.LoanDAOMySQLImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Test Class for NotificationsService using Manual Stubbing (Task #159).
 * Focuses on business logic: formatting and counting notifications.
 */
public class NotificationsServiceTest {

    private final int TEST_USER_ID = 1;
    private final int OTHER_USER_ID = 2;
    private final String EXCEPTION_MESSAGE = "Database connection failed during fetch";

    // --- Test Data for Stub ---
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
    private final List<OverdueLoan> twoLoansList = Arrays.asList(loan1, loan2);
    // ---------------------------


    /**
     * Inner class to act as a Test Stub for LoanDAOMySQLImpl.
     * It overrides the getOverdueLoansForUser method to return pre-defined data
     * based on the input userId, simulating successful and failing database calls.
     */
    private class TestLoanDAOStub extends LoanDAOMySQLImpl {

        // Overrides the specific method the NotificationsService calls
        @Override
        public List<OverdueLoan> getOverdueLoansForUser(int userId) throws DAOException {
            if (userId == TEST_USER_ID) {
                // Simulate success: user has multiple overdue loans
                return twoLoansList;
            } else if (userId == OTHER_USER_ID) {
                // Simulate success: user has no overdue loans
                return Collections.emptyList();
            } else {
                // Simulate database failure
                throw new DAOException(EXCEPTION_MESSAGE);
            }
        }
    }

    // Create the stub instance and the service using the stub
    private final TestLoanDAOStub loanDAOStub = new TestLoanDAOStub();
    private final NotificationsService notificationsService = new NotificationsService(loanDAOStub);


    // =========================================================================
    //                              TEST METHODS
    // =========================================================================

    /**
     * Test Case 1: Multiple Overdue Loans - Ensures correct counting and formatting.
     */
    @Test
    void getFormattedNotifications_MultipleLoans_ReturnsFormattedList() throws DAOException {
        // Act
        List<String> notifications = notificationsService.getFormattedNotifications(TEST_USER_ID);

        // Assert
        assertNotNull(notifications, "The list should not be null.");
        assertEquals(2, notifications.size(), "Should return exactly two notifications.");

        // Assert correct formatting (Task #159: checks for delayed material details)
        String expected1 = "OVERDUE: The material 'Moby Dick' (Author: Herman Melville) was due on 2025-11-15.";
        String expected2 = "OVERDUE: The material 'Dune' (Author: Frank Herbert) was due on 2025-12-01.";

        assertEquals(expected1, notifications.get(0), "First notification message is incorrectly formatted.");
        assertEquals(expected2, notifications.get(1), "Second notification message is incorrectly formatted.");
    }

    /**
     * Test Case 2: No Overdue Loans - Ensures an empty list is handled correctly.
     */
    @Test
    void getFormattedNotifications_NoLoans_ReturnsEmptyList() throws DAOException {
        // Act
        List<String> notifications = notificationsService.getFormattedNotifications(OTHER_USER_ID);

        // Assert
        assertNotNull(notifications, "The list should not be null.");
        assertTrue(notifications.isEmpty(), "Should return an empty list when no loans are overdue.");
    }

    /**
     * Test Case 3: DAO Fails - Ensures the exception is thrown through the Service Layer.
     */
    @Test
    void getFormattedNotifications_DAOFails_ThrowsDAOException() {
        // Arrange: Use a placeholder ID that the stub is configured to treat as a failure

        // Act & Assert
        DAOException thrown = assertThrows(DAOException.class, () -> {
            notificationsService.getFormattedNotifications(0); // Use 0 to trigger the failure
        }, "Should throw DAOException when the underlying DAO call fails.");

        assertTrue(thrown.getMessage().contains(EXCEPTION_MESSAGE), "The exception message should match the one thrown by the stub.");
    }

    // Test Case for the hasOverdueMaterials helper method used by the controller (Task #156 logic)
    @Test
    void hasOverdueMaterials_TestCases() {
        // Case A: Has overdue materials (should return true)
        assertTrue(notificationsService.hasOverdueMaterials(TEST_USER_ID), "Should be true when loans exist.");

        // Case B: Has NO overdue materials (should return false)
        assertFalse(notificationsService.hasOverdueMaterials(OTHER_USER_ID), "Should be false when no loans exist.");

        // Case C: DAO fails (should return false for safety)
        assertFalse(notificationsService.hasOverdueMaterials(0), "Should be false when DAO fails.");
    }
}