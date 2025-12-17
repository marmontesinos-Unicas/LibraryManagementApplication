package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.LoanDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.HoldDAOMySQLImpl;
import it.unicas.project.template.address.model.OverdueLoan; // For notifying about overdue loans
import it.unicas.project.template.address.model.ExpiringHoldInfo; // For notifying about holds

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class responsible for aggregating and formatting all user-facing notifications.
 * This includes alerts for overdue loans and holds that are about to expire.
 *
 * Access Keyword Explanation: {@code public} - This class is the dedicated
 * business logic provider for notifications and must be accessible by the
 * {@code UserLandingController} in the view layer.
 */
public class NotificationsService {

    // Instance of the DAO to fetch data
    private final LoanDAOMySQLImpl loanDAO;
    private final HoldDAOMySQLImpl holdDAO;
    // Access Keyword Explanation: {@code private final} - These fields hold the
    // necessary Data Access Objects (DAOs). They are private to enforce encapsulation
    // and final because dependencies should not change after construction.

    /**
     * 1. Production Constructor: Initializes DAOs using the singleton pattern.
     *
     * Access Keyword Explanation: {@code public} - Standard constructor used
     * when the service is instantiated within the application context.
     */
    public NotificationsService() {
        // Initialize the DAO instance by retrieving the Singleton instance
        this.loanDAO = (LoanDAOMySQLImpl) LoanDAOMySQLImpl.getInstance();
        this.holdDAO = (HoldDAOMySQLImpl) HoldDAOMySQLImpl.getInstance();
    }

    /**
     * 2. Test Constructor: Allows dependency injection for mocking DAOs in unit tests.
     *
     * Access Keyword Explanation: {@code public} - Used by external test classes
     * to provide mock implementations.
     *
     * @param loanDAO The mock/real Loan DAO implementation.
     * @param holdDAO The mock/real Hold DAO implementation.
     */
    public NotificationsService(LoanDAOMySQLImpl loanDAO, HoldDAOMySQLImpl holdDAO) {
        this.loanDAO = loanDAO;
        this.holdDAO = holdDAO;
    }

    /**
     * Core Method: Retrieves all overdue loans and expiring holds for a user and
     * formats them into user-friendly notification messages.
     *
     * Access Keyword Explanation: {@code public} - This is the primary method
     * called by the {@code UserLandingController} to fetch and display notifications.
     *
     * @param userId The ID of the currently logged-in user.
     * @return A list of formatted notification strings, ready for UI display.
     * @throws DAOException if a database error occurs during retrieval.
     */
    public List<String> getFormattedNotifications(int userId) throws DAOException {
        List<String> notifications = new ArrayList<>();

        // Retrieve and format Overdue Loans
        List<OverdueLoan> overdueLoans = loanDAO.getOverdueLoansForUser(userId);
        notifications.addAll(
                // Use a stream to transform the list of DTOs into a list of formatted strings
                overdueLoans.stream()
                        .map(this::formatOverdueMessage)
                        .collect(Collectors.toList())
        );

        // Retrieve and format Expiring Holds ---
        List<ExpiringHoldInfo> expiringHolds = holdDAO.getExpiringHoldsForUser(userId);
        notifications.addAll(
                // Transform the list of ExpiringHoldInfo DTOs into a list of formatted strings
                expiringHolds.stream()
                        .map(this::formatExpiringHoldMessage)
                        .collect(Collectors.toList())
        );
        return notifications;
    }

    /**
     * Helper method to create a user-facing message from an OverdueLoan object.
     *
     * Access Keyword Explanation: {@code private} - This is an internal utility method
     * used only by {@code getFormattedNotifications} to maintain clean code and should
     * not be called directly from outside the service.
     *
     * @param item The OverdueLoan data transfer object.
     * @return The formatted overdue message string.
     */
    private String formatOverdueMessage(OverdueLoan item) {
        // Uses String.format for easy readability and substitution of data values
        return String.format(
                "OVERDUE: The material '%s' (Author: %s) was due on %s. Please return it immediately to your local library.",
                item.getMaterialTitle(),
                item.getMaterialAuthor(),
                item.getDueDate()
        );
    }

    /**
     * Helper method to create a user-facing message from an ExpiringHoldInfo object.
     * Tells the user to go to the library before the expiration date.
     *
     * Access Keyword Explanation: {@code private} - Internal utility method.
     *
     * @param item The ExpiringHoldInfo data transfer object.
     * @return The formatted expiring hold message string.
     */
    private String formatExpiringHoldMessage(ExpiringHoldInfo item) {
        // Uses String.format to construct the expiration notification message
        return String.format(
                "HOLD EXPIRING: The material '%s' (Author: %s) is ready for pickup and your hold expires on %s. Please loan your material at your local library before this date.",
                item.getMaterialTitle(),
                item.getMaterialAuthor(),
                item.getHoldExpirationDate()
        );
    }

    /**
     * Helper Method for UI Logic: Checks if the user has any pending notifications (overdue or expiring holds).
     * This is typically used by the controller to decide whether to display a notification icon/badge.
     *
     * Access Keyword Explanation: {@code public} - This method is called by the
     * {@code UserLandingController} to check the pre-condition for displaying the notification view.
     *
     * @param userId The ID of the currently logged-in user.
     * @return true if one or more notifications exist, false otherwise.
     */
    public boolean hasPendingNotifications(int userId) {
        try {
            // Check for overdue loans (existing logic)
            boolean hasOverdue = loanDAO.getOverdueLoansForUser(userId).size() > 0;
            if (hasOverdue) return true;

            // Check for expiring holds (new logic)
            return holdDAO.getExpiringHoldsForUser(userId).size() > 0;

        } catch (DAOException e) {
            System.err.println("Error checking for pending notifications: " + e.getMessage());
            // Fail-safe: if DAO fails (e.g., DB offline), assume no notifications
            // to prevent the UI from being blocked.
            return false;
        }
    }
}