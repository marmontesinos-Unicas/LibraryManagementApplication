package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Hold;
import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.view.LoanRow;
import it.unicas.project.template.address.view.HoldRow;

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
 */
public class UserLandingService {

    private final DAO<Loan> loanDao;
    private final DAO<Hold> holdDao;
    private final DAO<Material> materialDao;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Constructs the service with required DAO dependencies.
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
     *
     * @param user the logged-in user
     * @return list of LoanRow objects representing the user's loans
     * @throws DAOException if any database operation fails
     */
    public List<LoanRow> getUserLoans(User user) throws DAOException {
        if (user == null) return List.of();

        List<Loan> loans = loanDao.select(new Loan(null, user.getIdUser(), null, null, null, null));

        return loans.stream().map(loan -> {
            Material mat = new Material();
            mat.setIdMaterial(loan.getIdMaterial());

            try {
                mat = materialDao.select(mat).stream().findFirst().orElse(null);
            } catch (DAOException e) {
                e.printStackTrace();
            }

            String title = (mat != null && mat.getTitle() != null) ? mat.getTitle() : "Unknown";
            String returnDate = (loan.getReturn_date() != null) ? loan.getReturn_date().format(dateFormatter) : "Not Returned";

            String status;
            if (loan.getReturn_date() != null) {
                status = "Returned";
            } else if (loan.getDue_date() != null && loan.getDue_date().isBefore(LocalDateTime.now())) {
                status = "Delayed";
            } else {
                status = "Active";
            }

            return new LoanRow(loan.getIdLoan(), "", title, "", returnDate, status);
        }).collect(Collectors.toList());
    }

    /**
     * Retrieves all holds for a specific user and formats them for display.
     *
     * @param user the logged-in user
     * @return list of HoldRow objects representing the user's holds
     * @throws DAOException if any database operation fails
     */
    public List<HoldRow> getUserHolds(User user) throws DAOException {
        if (user == null) return List.of();

        List<Hold> holds = holdDao.select(new Hold(null, user.getIdUser(), null, null));

        return holds.stream().map(hold -> {
            Material mat = null;
            if (hold.getIdMaterial() != -1) {
                mat = new Material();
                mat.setIdMaterial(hold.getIdMaterial());
                try {
                    mat = materialDao.select(mat).stream().findFirst().orElse(null);
                } catch (DAOException e) {
                    e.printStackTrace();
                }
            }

            String title = (mat != null && mat.getTitle() != null) ? mat.getTitle() : "Unknown";
            String maxDate = (hold.getHold_date() != null) ? hold.getHold_date().format(dateFormatter) : "-";

            return new HoldRow(hold.getIdHold(), title, maxDate);
        }).collect(Collectors.toList());
    }
}
