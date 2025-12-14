package it.unicas.project.template.address.service;

import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.view.LoadReturnController.LoanRow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer responsible for managing loan returns, filtering active or delayed loans,
 * and converting internal data models into {@link LoanRow} objects for UI display.
 * <p>
 * This class simulates data access using in-memory lists, and is primarily
 * intended for testing or for controller logic that does not interact directly with DAOs.
 * </p>
 */
public class ReturnService {

    /** In-memory list of users involved in loan operations */
    private final List<User> users;

    /** In-memory list of materials that may be loaned or returned */
    private final List<Material> materials;

    /** In-memory list of loan records */
    private final List<Loan> loans;

    /**
     * Creates an empty ReturnService with internal lists initialized.
     * Typically used in production where real DAOs populate data.
     */
    public ReturnService() {
        this.users = new ArrayList<>();
        this.materials = new ArrayList<>();
        this.loans = new ArrayList<>();
    }

    /**
     * Creates a ReturnService instance using predefined lists.
     * Used primarily for unit testing.
     *
     * @param users     list of users
     * @param materials list of materials
     * @param loans     list of loans
     */
    public ReturnService(List<User> users, List<Material> materials, List<Loan> loans) {
        this.users = users;
        this.materials = materials;
        this.loans = loans;
    }

    /**
     * Retrieves all loans that have not been returned.
     * <p>
     * A loan is considered active if {@code return_date} is {@code null}.
     * </p>
     *
     * @return list of active loans represented as {@link LoanRow}
     */
    public List<LoanRow> getActiveLoans() {
        List<LoanRow> result = new ArrayList<>();
        for (Loan l : loans) {
            if (l.getReturn_date() == null) {
                LoanRow row = buildLoanRow(l);
                if (row != null) result.add(row);
            }
        }
        result.sort((r1, r2) -> r1.getDueDateAsLocalDate().compareTo(r2.getDueDateAsLocalDate()));
        return result;
    }

    /**
     * Retrieves all loans that are overdue.
     * <p>
     * A loan is considered delayed if its due date is before the current time.
     * </p>
     *
     * @return list of overdue active loans
     */
    public List<LoanRow> getDelayedLoans() {
        LocalDateTime now = LocalDateTime.now();
        return getActiveLoans().stream()
                .filter(r -> r.getDueDateAsLocalDate().isBefore(now))
                .collect(Collectors.toList());
    }

    /**
     * Searches active loans by user name or material title.
     * <p>
     * The search is case-insensitive.
     * </p>
     *
     * @param text search string to match against user names or material titles
     * @return filtered list of matching loan rows
     */
    public List<LoanRow> searchLoans(String text) {
        String lowerText = text.toLowerCase();
        return getActiveLoans().stream()
                .filter(r -> r.getTitle().toLowerCase().contains(lowerText) ||
                        r.getUser().toLowerCase().contains(lowerText))
                .collect(Collectors.toList());
    }

    /**
     * Marks a loan as returned and updates the corresponding material status.
     * <p>
     * The method locates the real loan by matching user, material title,
     * and due date information contained in the provided {@link LoanRow}.
     * </p>
     *
     * @param row the loan row representing the loan to be returned
     * @throws DAOException if the loan cannot be located
     */
    public void returnLoan(LoanRow row) throws DAOException {
        final Loan loanReal = loans.stream()
                .filter(l -> l.getReturn_date() == null)
                .filter(l -> {
                    Material m = materials.stream()
                            .filter(mat -> mat.getIdMaterial() == l.getIdMaterial())
                            .findFirst()
                            .orElse(null);
                    User u = users.stream()
                            .filter(user -> user.getIdUser() == l.getIdUser())
                            .findFirst()
                            .orElse(null);

                    if (m == null || u == null) return false;

                    String fullUser = (u.getName() != null ? u.getName() : "") + " " +
                            (u.getSurname() != null ? u.getSurname() : "");
                    String title = m.getTitle() != null ? m.getTitle() : "";

                    return title.equals(row.getTitle()) &&
                            fullUser.equals(row.getUser()) &&
                            l.getDue_date() != null &&
                            l.getDue_date().toLocalDate().toString().equals(
                                    row.getDueDateAsLocalDate().toLocalDate().toString()
                            );
                })
                .findFirst()
                .orElse(null);

        if (loanReal == null) {
            throw new DAOException("Loan not found");
        }

        // Mark loan as returned
        loanReal.setReturn_date(LocalDateTime.now());

        // Mark material as available
        Material matToUpdate = materials.stream()
                .filter(mat -> mat.getIdMaterial() == loanReal.getIdMaterial())
                .findFirst()
                .orElse(null);

        if (matToUpdate != null) {
            matToUpdate.setMaterial_status("available");
        }
    }

    /**
     * Converts a {@link Loan} object into a {@link LoanRow} for UI usage.
     * <p>
     * If the associated user or material cannot be found, the method returns {@code null}.
     * </p>
     *
     * @param loan the loan to convert
     * @return a LoanRow containing formatted loan information, or {@code null} if data is incomplete
     */
    private LoanRow buildLoanRow(Loan loan) {
        Material m = materials.stream()
                .filter(mat -> mat.getIdMaterial() == loan.getIdMaterial())
                .findFirst()
                .orElse(null);

        User u = users.stream()
                .filter(user -> user.getIdUser() == loan.getIdUser())
                .findFirst()
                .orElse(null);

        if (m == null || u == null) return null;

        String materialType = switch (m.getIdMaterialType() != null ? m.getIdMaterialType() : 0) {
            case 1 -> "Book";
            case 2 -> "CD";
            case 3 -> "Movie";
            case 4 -> "Magazine";
            default -> "Unknown";
        };

        String title = m.getTitle() != null ? m.getTitle() : "—";
        String userName = (u.getName() != null ? u.getName() : "") + " " +
                (u.getSurname() != null ? u.getSurname() : "");
        String due = loan.getDue_date() != null ? loan.getDue_date().toLocalDate().toString() : "—";
        boolean delayed = loan.getDue_date() != null && loan.getDue_date().isBefore(LocalDateTime.now());

        return new LoanRow(loan.getIdLoan(), materialType, title, userName, due, delayed ? "Yes" : "No");
    }
}
