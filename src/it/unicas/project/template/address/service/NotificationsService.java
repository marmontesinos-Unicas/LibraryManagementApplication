package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.LoanDAOMySQLImpl;
import it.unicas.project.template.address.model.OverdueLoan; // Use the new class name
import java.util.List;
import java.util.stream.Collectors;

public class NotificationsService {

    // Instance of the DAO to fetch data
    private final LoanDAOMySQLImpl loanDAO;

    // 1. Production Constructor (uses singleton/DI)
    public NotificationsService() {
        // Initialize the DAO instance
        this.loanDAO = (LoanDAOMySQLImpl) LoanDAOMySQLImpl.getInstance();
    }

    // 2. Test Constructor (Accepts the stubbing for the JUnit tests)
    public NotificationsService(LoanDAOMySQLImpl loanDAO) {
        this.loanDAO = loanDAO;
    }

    /**
     * Core Method: Retrieves all overdue loans for a user and
     * formats them into user-friendly notification messages.
     * * @param userId The ID of the currently logged-in user.
     * @return A list of formatted notification strings, ready for UI display.
     * @throws DAOException if a database error occurs during retrieval.
     */
    public List<String> getFormattedNotifications(int userId) throws DAOException {
        // 1. Retrieve the raw data (List<OverdueLoan>) from the DAO
        List<OverdueLoan> overdueLoans = loanDAO.getOverdueLoansForUser(userId);

        // 2. Map the OverdueLoan objects to formatted notification strings
        return overdueLoans.stream()
                .map(this::formatNotificationMessage)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to create a user-facing message from an OverdueLoan object.
     */
    private String formatNotificationMessage(OverdueLoan item) {
        return String.format(
                "OVERDUE: The material '%s' (Author: %s) was due on %s.",
                item.getMaterialTitle(),
                item.getMaterialAuthor(),
                item.getDueDate()
        );
    }

    /**
     * Helper Method for UI Logic (Task #156): Checks if the user has any overdue materials.
     * * @param userId The ID of the currently logged-in user.
     * @return true if one or more overdue loans exist, false otherwise.
     */
    public boolean hasOverdueMaterials(int userId) {
        try {
            // Simply check if the list returned by the DAO is non-empty
            return loanDAO.getOverdueLoansForUser(userId).size() > 0;
        } catch (DAOException e) {
            System.err.println("Error checking for overdue materials: " + e.getMessage());
            return false;
        }
    }
}