package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.LoanDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.HoldDAOMySQLImpl;
import it.unicas.project.template.address.model.OverdueLoan; // For notifying about overdue loans
import it.unicas.project.template.address.model.ExpiringHoldInfo; // For notifying about holds

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationsService {

    // Instance of the DAO to fetch data
    private final LoanDAOMySQLImpl loanDAO;
    private final HoldDAOMySQLImpl holdDAO;

    // 1. Production Constructor (uses singleton/DI)
    public NotificationsService() {
        // Initialize the DAO instance
        this.loanDAO = (LoanDAOMySQLImpl) LoanDAOMySQLImpl.getInstance();
        this.holdDAO = (HoldDAOMySQLImpl) HoldDAOMySQLImpl.getInstance();
    }

    // 2. Test Constructor (Accepts the stubbing for the JUnit tests)
    public NotificationsService(LoanDAOMySQLImpl loanDAO, HoldDAOMySQLImpl holdDAO) {
        this.loanDAO = loanDAO;
        this.holdDAO = holdDAO;
    }

    // Overload for backward compatibility in existing tests that only mock LoanDAO
    public NotificationsService(LoanDAOMySQLImpl loanDAO) {
        this(loanDAO, (HoldDAOMySQLImpl) HoldDAOMySQLImpl.getInstance());
    }

    /**
     * Core Method: Retrieves all overdue loans and expiring holds for a user and
     * formats them into user-friendly notification messages.
     * @param userId The ID of the currently logged-in user.
     * @return A list of formatted notification strings, ready for UI display.
     * @throws DAOException if a database error occurs during retrieval.
     */
    public List<String> getFormattedNotifications(int userId) throws DAOException {
        List<String> notifications = new ArrayList<>();

        // 1. Retrieve and format Overdue Loans
        List<OverdueLoan> overdueLoans = loanDAO.getOverdueLoansForUser(userId);
        notifications.addAll(
                overdueLoans.stream()
                        .map(this::formatOverdueMessage)
                        .collect(Collectors.toList())
        );

        // --- NEW LOGIC: Retrieve and format Expiring Holds ---
        List<ExpiringHoldInfo> expiringHolds = holdDAO.getExpiringHoldsForUser(userId);
        notifications.addAll(
                expiringHolds.stream()
                        .map(this::formatExpiringHoldMessage)
                        .collect(Collectors.toList())
        );

        return notifications;
    }

    /**
     * Helper method to create a user-facing message from an OverdueLoan object.
     */
    private String formatOverdueMessage(OverdueLoan item) {
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
     */
    private String formatExpiringHoldMessage(ExpiringHoldInfo item) {
        return String.format(
                "HOLD EXPIRING: The material '%s' (Author: %s) is ready for pickup and your hold expires on %s. Please loan your material at your local library before this date.",
                item.getMaterialTitle(),
                item.getMaterialAuthor(),
                item.getHoldExpirationDate()
        );
    }

    /**
     * Helper Method for UI Logic: Checks if the user has any pending notifications (overdue or expiring holds).
     * @param userId The ID of the currently logged-in user.
     * @return true if one or more notifications exist, false otherwise.
     */
    public boolean hasPendingNotifications(int userId) {
        try {
            // Check for overdue loans (existing logic)
            boolean hasOverdue = loanDAO.getOverdueLoansForUser(userId).size() > 0;
            if (hasOverdue) return true;

            // --- NEW LOGIC: Check for expiring holds ---
            return holdDAO.getExpiringHoldsForUser(userId).size() > 0;

        } catch (DAOException e) {
            System.err.println("Error checking for pending notifications: " + e.getMessage());
            // Fail safe: if DAO fails, assume no notifications to avoid blocking UI
            return false;
        }
    }

    // Old method renamed/deprecated for proper usage:
    public boolean hasOverdueMaterials(int userId) {
        return hasPendingNotifications(userId);
    }
}