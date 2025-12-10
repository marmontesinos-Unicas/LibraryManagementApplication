package it.unicas.project.template.address.model;

import java.time.LocalDate;

/**
 * A simple model to hold the essential details for an overdue notification,
 * combining data from the 'loans' and 'materials' tables.
 */
public class OverdueLoan {
    private final int loanId;
    private final String materialTitle;
    private final String materialAuthor;
    private final LocalDate dueDate; // LocalDate is sufficient for display

    /**
     * Constructor for an OverdueLoan.
     * @param loanId The ID of the overdue loan.
     * @param materialTitle The title of the overdue material.
     * @param materialAuthor The author of the overdue material.
     * @param dueDate The date the material was supposed to be returned.
     */
    public OverdueLoan(int loanId, String materialTitle, String materialAuthor, LocalDate dueDate) {
        this.loanId = loanId;
        this.materialTitle = materialTitle;
        this.materialAuthor = materialAuthor;
        this.dueDate = dueDate;
    }

    // Getters
    public int getLoanId() { return loanId; }
    public String getMaterialTitle() { return materialTitle; }
    public String getMaterialAuthor() { return materialAuthor; }
    public LocalDate getDueDate() { return dueDate; }
}
