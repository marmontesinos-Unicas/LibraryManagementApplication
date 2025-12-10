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

public class ReturnService {

    private final List<User> users;
    private final List<Material> materials;
    private final List<Loan> loans;

    public ReturnService() {
        this.users = new ArrayList<>();
        this.materials = new ArrayList<>();
        this.loans = new ArrayList<>();
    }

    // Constructor para tests
    public ReturnService(List<User> users, List<Material> materials, List<Loan> loans) {
        this.users = users;
        this.materials = materials;
        this.loans = loans;
    }

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

    public List<LoanRow> getDelayedLoans() {
        LocalDateTime now = LocalDateTime.now();
        return getActiveLoans().stream()
                .filter(r -> r.getDueDateAsLocalDate().isBefore(now))
                .collect(Collectors.toList());
    }

    public List<LoanRow> searchLoans(String text) {
        String lowerText = text.toLowerCase();
        return getActiveLoans().stream()
                .filter(r -> r.getTitle().toLowerCase().contains(lowerText) ||
                        r.getUser().toLowerCase().contains(lowerText))
                .collect(Collectors.toList());
    }

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

        // Marcar préstamo como devuelto
        loanReal.setReturn_date(LocalDateTime.now());

        // Marcar material como disponible
        Material matToUpdate = materials.stream()
                .filter(mat -> mat.getIdMaterial() == loanReal.getIdMaterial())
                .findFirst()
                .orElse(null);

        if (matToUpdate != null) {
            matToUpdate.setMaterial_status("available");
        }
    }

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
