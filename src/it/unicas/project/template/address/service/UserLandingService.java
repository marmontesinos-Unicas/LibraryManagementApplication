package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Hold;
import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.LoanRow;
import it.unicas.project.template.address.model.HoldRow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class to handle all business logic for the User Landing Page.
 * <p>
 * This class retrieves and formats the user's loans, holds, and overdue status,
 * abstracting database operations from the controller.
 * </p>
 *
 * Access Keyword Explanation: {@code public} - This class is the dedicated
 * business layer logic provider for the user dashboard and must be accessible
 * by the {@code UserLandingController} in the view layer.
 */
public class UserLandingService {

    private final DAO<Loan> loanDao;
    private final DAO<Hold> holdDao;
    private final DAO<Material> materialDao;
    // Access Keyword Explanation: {@code private final} - These fields hold the
    // necessary Data Access Objects (DAOs). They are private to enforce encapsulation
    // and final because dependencies should not change after construction (Dependency Injection).

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // Access Keyword Explanation: {@code private final} - Internal utility field
    // used only for consistent date formatting within this service.

    /**
     * Constructs the service with required DAO dependencies.
     *
     * Access Keyword Explanation: {@code public} - This is the primary constructor,
     * allowing the controller or factory to inject the necessary DAO dependencies.
     *
     * @param loanDao DAO for Loan entity
     * @param holdDao DAO for Hold entity
     * @param materialDao DAO for Material entity
     */
    public UserLandingService(DAO<Loan> loanDao, DAO<Hold> holdDao, DAO<Material> materialDao) {
        this.loanDao = loanDao;
        this.holdDao = holdDao;
        this.materialDao = materialDao;
    }

    /**
     * Retrieves all active loans for a specific user and formats them for display.
     * This involves querying loans, then querying the related material for the title.
     *
     * Access Keyword Explanation: {@code public} - This is a core business method
     * that must be called by the {@code UserLandingController} to populate the UI.
     *
     * @param user the logged-in user
     * @return list of LoanRow objects representing the user's loans
     * @throws DAOException if any database operation fails
     */
    public List<LoanRow> getUserLoans(User user) throws DAOException {
        // Check if the user object is valid before proceeding
        if (user == null) return List.of();

        // Fetch all loans associated with the user ID from the database.
        // The Loan object acts as a filter (Loan ID is null, User ID is set, others null).
        List<Loan> loans = loanDao.select(new Loan(null, user.getIdUser(), null, null, null, null));

        // Map the list of raw Loan objects to presentation-friendly LoanRow objects
        return loans.stream().map(loan -> {
            Material mat = new Material();
            mat.setIdMaterial(loan.getIdMaterial());

            // Fetch the Material details (specifically the title) for the current loan.
            try {
                mat = materialDao.select(mat).stream().findFirst().orElse(null);
            } catch (DAOException e) {
                // If fetching material fails, log the error but allow the mapping to continue with defaults
                e.printStackTrace();
            }

            // Extract and format data
            String title = (mat != null && mat.getTitle() != null) ? mat.getTitle() : "Unknown";
            // Format the return date or set a default string
            String returnDate = (loan.getReturn_date() != null) ? loan.getReturn_date().format(dateFormatter) : "Not Returned";

            // Determine the loan status based on business rules (Delayed, Active, Returned)
            String status;
            if (loan.getReturn_date() != null) {
                status = "Returned"; // Loan has been closed
            } else if (loan.getDue_date() != null && loan.getDue_date().isBefore(LocalDateTime.now())) {
                status = "Delayed"; // Loan is overdue
            } else {
                status = "Active"; // Loan is current
            }

            // Create and return the LoanRow object for the UI
            // Fields like borrower name, copy ID, etc., are empty/default here as they are not needed on the User Dashboard
            return new LoanRow(loan.getIdLoan(), "", title, "", "", "", returnDate, status);
        }).collect(Collectors.toList()); // Collect the stream back into a List<LoanRow>
    }

    /**
     * Retrieves all holds (reservations) for a specific user and formats them for display.
     *
     * Access Keyword Explanation: {@code public} - This is a core business method
     * that must be called by the {@code UserLandingController} to populate the UI.
     *
     * @param user the logged-in user
     * @return list of HoldRow objects representing the user's holds
     * @throws DAOException if any database operation fails
     */
    public List<HoldRow> getUserHolds(User user) throws DAOException {
        // Check if the user object is valid
        if (user == null) return List.of();

        // Fetch all holds associated with the user ID from the database.
        List<Hold> holds = holdDao.select(new Hold(null, user.getIdUser(), null, null));

        // Map the list of raw Hold objects to presentation-friendly HoldRow objects
        return holds.stream().map(hold -> {
            Material mat = null;
            // Ensure material ID is valid before attempting a lookup
            if (hold.getIdMaterial() != -1) {
                mat = new Material();
                mat.setIdMaterial(hold.getIdMaterial());
                // Fetch the Material details (title)
                try {
                    mat = materialDao.select(mat).stream().findFirst().orElse(null);
                } catch (DAOException e) {
                    e.printStackTrace();
                }
            }

            // Extract and format data
            String title = (mat != null && mat.getTitle() != null) ? mat.getTitle() : "Unknown";
            // Format the hold date (which often represents the expiration or creation date)
            String maxDate = (hold.getHold_date() != null) ? hold.getHold_date().format(dateFormatter) : "-";

            // Create and return the HoldRow object for the UI
            return new HoldRow(hold.getIdHold(), title, maxDate);
        }).collect(Collectors.toList()); // Collect the stream back into a List<HoldRow>
    }
}
